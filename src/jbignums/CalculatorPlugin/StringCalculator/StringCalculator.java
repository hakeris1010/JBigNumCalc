package jbignums.CalculatorPlugin.StringCalculator;

import com.sun.istack.internal.NotNull;
import com.sun.org.apache.regexp.internal.RE;
import jbignums.CalculatorPlugin.AsyncQueueCalculatorPlugin;
import jbignums.CalculatorPlugin.CalculatorPlugin;
import jbignums.CalculatorPlugin.StringCalculator.StringExpressionParser.CalcNode;
import jbignums.Helpers.Checkers;
import jbignums.Helpers.OutF;
import jbignums.Helpers.StrF;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *  A Calculator designed to work concurrently.
 *  Extends the functionality of a SwingWorker.
 *  Principle:
 *  - A new job is started by calling startNewJob(), passing a String representation of CalcExpression.
 *  - While the job is being Performed, intermediate data is appended to the private Intermediate CalcResults queue.
 *  - The user can get the Intermediate Results by calling the getIntermediateResults() function from any thread.
 *  - When the job is done, the end result is published as a specific format.
 */
public class StringCalculator extends AsyncQueueCalculatorPlugin{
    /**
     * StringCalculator-specific Query and Result.
     */
    public static class Query extends AsyncQueueCalculatorPlugin.Query{
        public String expr;
        public int calcMode;

        public Query(){ super(); }
        public Query(String expression, int mode, int qType, long taskID1){
            super(qType, taskID1);
            expr = expression;
            calcMode = mode;
        }

        @Override
        public String toString(){
            return super.toString()+"\n Expr: "+expr+"\n calcMode: "+calcMode;
        }
    }

    public enum DataType {
        DEFAULT,
        EQUATION;
    }

    public static class Result extends AsyncQueueCalculatorPlugin.Result{
        public DataType dataType;
        public double dblResult;
        public String strResult = "No Data";
        public BigDecimal bigResult;

        public Result(){ super(); }
        public Result(int resType, long taskID, DataType dType, double dRes, String sRes, BigDecimal bigRes){
            super(resType, taskID);
            dataType = dType;
            dblResult = dRes;
            strResult = sRes;
            bigResult = bigRes;
        }

        @Override
        public String toString(){
            return super.toString()+"\n DataType: "+dataType.name()+"\n dblResult: "+dblResult+"\n strResult: "+strResult;
        }
    }

    /**
     * Exception class for Calculator Processor
     */
    public static class CalculationNodeProcessException extends Exception{
        public static final int WRONG_OPERATOR_POSITIONS = 1 << 0;
        public static final int WRONG_FUNCTION_ARGUMENTS = 1 << 1;
        public static final int UNKNOWN_FUNCTION         = 1 << 2;
        public static final int UNKNOWN_CONSTANT         = 1 << 3;
        public static final int UNIMPLEMENTED_FEATURE    = 1 << 4;

        public enum Type{
            TERMINATION_REQUESTED,
            SYNTAX_ERROR,
            CALCULATION_ERROR,
            UNKNOWN_ERROR
        }

        Type type;
        private final int errCode;
        private final List<CalcNode> errNodes;

        public CalculationNodeProcessException(Type tap, int errc, List<CalcNode> nodelist){
            type = tap; errCode = errc; errNodes = nodelist;
        }

        public int getErrCode() { return errCode; }
        public List<CalcNode> getErrNode() { return errNodes; }
        public Type getType(){ return type; }
    }

    private static class Defaults{
        public static boolean useSimulataneousCalcParse = true;

    }

    /** = = = = = == = = = = == = = = = == = = = = == = = = = = //
     * Private fields
     */
    private Query lastQuery;
    private volatile int calcMode = 0;

    private Result lastResult;

    // The Parser object.
    private StringExpressionParser currentParser;

    // The CalcNode queue to be bound to parser, so parser can push the parsed nodes to it.
    // Parallel processing is used: parser works on a different thread.
    // The calculator processes output from the queue on thread startCalculation() was called on.
    private final BlockingQueue<CalcNode> nodeQueue = new LinkedBlockingQueue<>();

    /** = = = = = == = = = = == = = = = == = = = = == = = = = = //
     * Constructors
     */
    public StringCalculator(){ super(); }
    public StringCalculator(BlockingQueue<AsyncQueueCalculatorPlugin.Result> resultQue, List<AtomicBoolean> termVars, int calculationMode){
        super(resultQue, termVars);
        calcMode = calculationMode;
    }

    /**
     * Assign query for using later, in startCalculation()
     * @param query - a Query object, which contains data to be passed to the startCalculation(). Contains:
     *   - expression - String representation of CalcExpression, e.g. "2+2" or "sqrt(1)"
     *   - ID - The unique ID of the job being started. Usually equals the hash code of current
     *          calculator object, because 1 calculator object can perform 1 job at a time.
     *   - mode - Not yet implemented. Pass 0.
     */
    @Override
    public synchronized void assignQuery(@NotNull CalculatorPlugin.Query query){
        if( !(StringCalculator.Query.class.isAssignableFrom( query.getClass() )) ){ // If wrong class, throw exception.
            throw new RuntimeException("Wrong class passed to assignQuery()");
        }
        if(((Query)query).expr==null) {
            System.out.println("Expression can't be null!!!");
            return;
        }

        lastQuery = (Query)query;
        currentID.set( lastQuery.getTaskID() );
    }

    /**
     * The Calculation function. It's not Synchronized because it's a main task, which will be running all the
     * time when checking functions will be called.
     *
     * @return The result object, which is also pushed into the queue and assigned as a lastResult.
     * @throws RuntimeException
     */
    @Override
    public Result startCalculation() throws RuntimeException {
        // Perform validation check on the essential variables.
        if(lastQuery == null || this.isCalculating())
            throw new RuntimeException("Can't start calculation. Missing essential parameters or currently calculating.");

        System.out.println("[StringCalculator.startCalculation()]: Current query values: "+lastQuery);

        // Mark the start of calculation (Superclass-handled).
        setCalculationStart();

        synchronized (this){
            // Create a new parser object, and bind it with the terminating vars and the nodequeue.
            currentParser = new StringExpressionParser(this.terminators, nodeQueue);
        }

        final AtomicReference<CalcNode> rootNode = new AtomicReference<>();

        // Start the parser thread, and after that start the NodeGetter on current thread.
        new Thread( () -> {
            long start1 = System.nanoTime();
            System.out.println("\n[ParseThread]: Parsing the expression...\n");
            // Parse the expression, and when done, assign rootNode.
            rootNode.set( currentParser.parseString( lastQuery.expr ) );
            System.out.println("\n[ParseThread]: Parsing complete! Time: " + (double) (System.nanoTime() - start1) / 1000.0 + " us\n");
        }).start();

        // On current thread, start the Calculation Processor, which takes nodes from queue and processes them.
        Result procRes;
        try {
            // After the processing is complete, assign the result.
            RecursiveResult ros = startQueueProcessing(0, calcMode);
            if(ros.blockRes==null) {
                //  throw new CalculationNodeProcessException(CalculationNodeProcessException.Type.UNKNOWN_ERROR, 0, null);
                ros.blockRes = new BigDecimal(0); //DEBUG
            }

            System.out.println("Recursive result complete! Value:\n"+ros);

            // If everything is successful, the end result representation is set like this.
            procRes = new Result( ResultType.END, currentID.get(), DataType.DEFAULT,
                     ros.blockRes.doubleValue(), ros.blockRes.toString(), ros.blockRes );

        }
        catch (CalculationNodeProcessException e) {
            System.out.println("Wow! Exception occured while processing input! ErrType:" +e.getType().name()+ " ("+e.getErrCode()+")");
            e.printStackTrace();

            // Exception occured - it means if parsing is still going, we must stop it.
            // Add a new teminating variable to signal quitting. The parser is bound, so it will affect.
            currentParser.addTerminator( new AtomicBoolean(true) );
            procRes = new Result(ResultType.END, currentID.get(), DataType.DEFAULT,
                              0, "Error: " +e.getType().name()+ " ("+e.getErrCode()+")",  null );
        }

        // Push the End Result is into the Queue.
        pushResultToQueue(procRes);

        // At the end, perform the ending inter-thread jobs of setting specific variables and stuff.
        synchronized (this) {
            lastResult = procRes;
        }
        // Mark the end of calculation (Superclass-handled).
        // Notifies the waiters and sets isCalculating to false.
        System.out.print("[StringCalculator.startCalculation()]: Ending Calculation...\n");
        setCalculationEnd();

        return lastResult;
    }

    /** Recursive method for performing calculation CalcNode processing, taking data from BlockingQueue.
     *  - This function is designed to work in parallel with QueueParser, when the are 2 main threads:
     *    - Parser parses the expression, pushing identified CalcNodes to Queue.
     *    - Processor (this method) takes data from the Queue and performs calculations.
     *
     *  - The results (StringCalculator.Result type) are pushed to this object's ResultQueue when ready.
     *  - Function performs all calculation here.
     *  - TODO (Far Future): Support for CalcFunction plugins for StringCalculator.
     *
     * @param level - recursion level. Initially called, must be 0.
     * @param flags - specific flags.
     * @return - the StringCalculator.Result object representing a final calculation result.
     */
    private @NotNull RecursiveResult startQueueProcessing(int level, int flags) throws CalculationNodeProcessException {
        StringExpressionParser.CalcNode nextNode = null;
        RecursiveResult thisBlockRes = new RecursiveResult(RecursiveResult.Code.NORMAL, null);
        boolean afterOperator = false, afterNumber = false;

        while ( !Checkers.isOneAtomicBooleanSet(terminators) ) {
            // Take a node when available.
            try {
                nextNode = nodeQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new CalculationNodeProcessException(CalculationNodeProcessException.Type.TERMINATION_REQUESTED, 0, null);
            }
            OutF.logfn(level * 2 ,"[NodeProcessThread]: Got node: " +
                    (nextNode == null ? "(null)" : "Type: \"" + nextNode.type.name() + "\"" +
                            (nextNode.data != null ? ", Data: " + nextNode.data : "") +
                            (nextNode.number != null ? ", Number: " + nextNode.number : "")
                    )
            );

            // Check if type is Interrupted or null - error occured while parsing expression.
            if (nextNode.type==CalcNode.Types.PARSE_INTERRUPTED){
                throw new CalculationNodeProcessException(CalculationNodeProcessException.Type.TERMINATION_REQUESTED, 0, null);
            }

            // Check if end node, if yes, parsing has ended, return value signaling the end of recursion.
            if (nextNode.type == CalcNode.Types.PARSE_END ){
                thisBlockRes.code = RecursiveResult.Code.EXPR_END;
                break;
            }
            // Block ended - just quit loop.
            if (nextNode.type == CalcNode.Types.BLOCK_END)
                break;

            // Check other possibilities. Here starts the Calculation and Error Checking!!!
            if(nextNode.type == CalcNode.Types.BLOCK_START){
                RecursiveResult temp = startQueueProcessing(level+1, flags);
                thisBlockRes.addStats(temp);
                if(temp.code == RecursiveResult.Code.EXPR_END){ // End of expression node occured --> time to end.
                    thisBlockRes.code = RecursiveResult.Code.EXPR_END;
                    break;
                }
            }
        }
        return thisBlockRes;
    }

    private static class RecursiveResult{
        public enum Code{
            EXPR_END,
            NORMAL
        }

        public Code code;
        public BigDecimal blockRes;

        // Statistics of the block.
        public int iterations = 0, bopCount = 0, numCount = 0, funCount = 0, conCount = 0;
        public int specCount = 0, blockCount = 0;

        //public Result res;
        public RecursiveResult(Code cod, BigDecimal bigRes){
            code = cod; blockRes = bigRes;
        }

        public void addStats(RecursiveResult res){
            iterations += res.iterations;
            bopCount += res.bopCount;
            numCount += res.numCount;
            funCount += res.funCount;
            conCount += res.conCount;
            specCount += res.specCount;
            blockCount += res.blockCount;
        }

        @Override
        public String toString(){
            return "RecursiveResult: "+this.getClass()+"\n Code: "+code.name()+"\n BlockRes: "+blockRes+
                   "\n Stats: iter="+iterations+", bops="+bopCount+", nums="+numCount+", funs="+funCount+
                   ", cons="+conCount+", blocks="+blockCount+", specs:"+specCount+"\n";
        }
    }

    /**
     * Implementations of the query and result getters, specific for StringCalculator.
     * @return - StringCalculator.Query and Result object.
     */
    @Override
    public synchronized Query getLastQuery(){
        return lastQuery;
    }

    @Override
    public Result getLastResult(){
        if(isCalculating()){
            return (Result) getWaitResultFromQueue(this);
        }
        synchronized (this) {
            return lastResult;
        }
    }

    /**
     * ===================================================================
     * TESTING-DEBUGGING.
     */
    public static void TestDEBUG1(){
        String calcExpr = "9+5+(4789.54+log2(pi)+++87)*2+x";

        System.out.println("[MainTester]: Starting StringCalculator Test. Expression:" +
                           (calcExpr.length() > 40 ? "(too long)": calcExpr));

        long start1 = System.nanoTime();
        BlockingQueue<AsyncQueueCalculatorPlugin.Result> resQue = new LinkedBlockingQueue<>();
        StringCalculator calc = new StringCalculator(resQue, null, 0);

        AsyncQueueCalculatorPlugin.Result res = calc.startCalculation(new Query(calcExpr, 0, QueryType.WHOLE_QUERY, 1));

        System.out.println("[MainTester]: Got result! Value: "+res+ "\nTime: " + (double) (System.nanoTime() - start1) / 1000.0 + " us\n");
    }
}

