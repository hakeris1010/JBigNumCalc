package jbignums.CalculatorPlugin.StringCalculator;

import com.sun.istack.internal.NotNull;
import javafx.util.Pair;
import jbignums.CalculatorPlugin.AsyncQueueCalculatorPlugin;
import jbignums.CalculatorPlugin.CalculatorPlugin;
import jbignums.CalculatorPlugin.StringCalculator.StringExpressionParser.CalcNode;
import jbignums.Helpers.Checkers;
import jbignums.Helpers.OutF;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Iterator;
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

    // Default scale for BigDecimals
    public static final int DECIMAL_SCALE = 32;

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
        ERROR,
        EQUATION;
    }

    public static class Result extends AsyncQueueCalculatorPlugin.Result{
        public DataType dataType;
        public double dblResult;
        public String strResult = "No Data";
        public BigDecimal bigResult;

        public Result(){ super(); }
        public Result(ResultType resType, long taskID, DataType dType, double dRes, String sRes, BigDecimal bigRes){
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
    public static class NodeProcessException extends Exception{
        public static final int DEFAULT                  = 0;
        public static final int CONSEQUENT_OPERATORS     = 1 << 1;
        public static final int CONSEQUENT_NUMBERS       = 1 << 2;
        public static final int WRONG_FUNCTION_ARGUMENTS = 1 << 3;
        public static final int UNKNOWN_FUNCTION         = 1 << 4;
        public static final int UNKNOWN_CONSTANT         = 1 << 5;
        public static final int UNIMPLEMENTED_FEATURE    = 1 << 6;
        public static final int INVALID_NODE             = 1 << 7;
        public static final int WRONG_FIRST_OPERATOR     = 1 << 8;

        public enum Type{
            TERMINATION_REQUESTED,
            SYNTAX_ERROR,
            CALCULATION_ERROR,
            INTERNAL_ERROR,
            UNKNOWN_ERROR
        }

        Type type;
        private final int errCode;
        private final List<CalcNode> errNodes;

        public NodeProcessException(int errc, Type tap, List<CalcNode> nodelist){
            type = tap; errCode = errc; errNodes = nodelist;
        }
        public NodeProcessException(Type tap){
            this(tap, 0);
        }
        public NodeProcessException(Type tap, int errc, CalcNode... nodes){
            this(errc, tap, Arrays.asList(nodes));
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
        long wholeTime = System.nanoTime();

        synchronized (this){
            // Create a new parser object, and bind it with the terminating vars and the nodequeue.
            currentParser = new StringExpressionParser(this.terminators, nodeQueue);
        }

        final AtomicReference<CalcNode> rootNode = new AtomicReference<>();

        // Start the parser thread, and after that start the NodeGetter on current thread.
        new Thread( () -> {
            long start1 = System.nanoTime();
            System.out.println("\n[ParseThread]: Starting Parsing the expression... Time from the start of "+
                    "startCalculation : " + (double) (System.nanoTime() - wholeTime) / 1000.0 + " us\n");
            // Parse the expression, and when done, assign rootNode.
            rootNode.set( currentParser.parseString( lastQuery.expr ) );
            System.out.println("\n[ParseThread]: Parsing complete! Time: " + (double) (System.nanoTime() - start1) / 1000.0 + " us\n");
        }).start();

        // On current thread, start the Calculation Processor, which takes nodes from queue and processes them.
        Result procRes;
        try {
            System.out.println("\n[StringCalculator.startCalculation()]: Calling the recursive calculation processor."+
                    " Time from the start: " + (double) (System.nanoTime() - wholeTime) / 1000.0 + " us\n");

            // After the processing is complete, assign the result.
            long start1 = System.nanoTime();
            RecursiveResult ros = startQueueProcessing(new CalcNode(CalcNode.Types.BLOCK, null, null), 0, calcMode);

            System.out.println("\n[StringCalculator.startCalculation()]: Calculation complete. startQueueProcessing " +
                    "completed in time: " + (double) (System.nanoTime() - start1) / 1000.0 + " us\n");
            if(ros.blockRes==null) {
                throw new NodeProcessException(NodeProcessException.Type.UNKNOWN_ERROR, 0, null);
            }

            System.out.println("Recursive result complete! Value:\n"+ros);

            // If everything is successful, the end result representation is set like this.
            procRes = new Result( ResultType.END, currentID.get(), DataType.DEFAULT,
                     ros.blockRes.number.doubleValue(), ros.blockRes.data, ros.blockRes.number );

        }
        catch (NodeProcessException e) {
            System.out.println("Wow! Exception occured while processing input! ErrType:" +e.getType().name()+ " ("+e.getErrCode()+")");
            e.printStackTrace();

            // Exception occured - it means if parsing is still going, we must stop it.
            // Add a new teminating variable to signal quitting. The parser is bound, so it will affect.
            currentParser.addTerminator( new AtomicBoolean(true) );
            procRes = new Result(ResultType.END, currentID.get(), DataType.ERROR,
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
        System.out.println("[StringCalculator.startCalculation()]: Ending Calculation. Whole time: "+
                            (double) (System.nanoTime() - wholeTime) / 1000.0 + " us\n");
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
    private @NotNull RecursiveResult startQueueProcessing(CalcNode rootNode, int level, int flags) throws NodeProcessException {
        boolean print = OutF.LoggedMethods.StringCalculator_startQueueProcessing;

        RecursiveResult thisBlockRes = new RecursiveResult(RecursiveResult.Code.NORMAL, null);
        // Bools used for checking the last node status in the basic operation chain.
        boolean timeToQuit = false;

        CalcNode basicOperationChain = new CalcNode(CalcNode.Types.BLOCK, null, null);
        CalcNode nextNode = null;

        while ( !Checkers.isOneAtomicBooleanSet(terminators) && (!timeToQuit || nextNode!=null) ) {
            if(nextNode == null) {
                try {
                    nextNode = nodeQueue.take();
                    OutF.logfn(print, level * 2, "[NodeProcessThread]: Taken node from QueUe: " + CalcNode.toBasicStaring(nextNode));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new NodeProcessException(NodeProcessException.Type.TERMINATION_REQUESTED, 0);
                }
            }
            else{
                OutF.logfn(print, level * 2, "[NodeProcessThread]: nextNode posted on last iteration: " + CalcNode.toBasicStaring(nextNode));
            }
            thisBlockRes.iterations++;

            // Check if type is Interrupted or null - error occured while parsing expression.
            if (nextNode==null || nextNode.type==CalcNode.Types.PARSE_INTERRUPTED){
                thisBlockRes.ctlNodeCount++;
                throw new NodeProcessException(NodeProcessException.Type.TERMINATION_REQUESTED, 0);
            }

            switch (nextNode.type) {
            //=========================================//
            // Check Control Command Nodes
            case PARSE_START:
                OutF.logfn(print, "Parse has been started.");
                thisBlockRes.ctlNodeCount++;
                break;
            // Check if end node, if yes, parsing has ended, return value signaling the end of recursion.
            case PARSE_END:
                thisBlockRes.ctlNodeCount++;
                thisBlockRes.code = RecursiveResult.Code.EXPR_END;
                timeToQuit = true;
                break;

            // Block ended - just quit loop.
            case BLOCK_END:
                thisBlockRes.ctlNodeCount++;
                timeToQuit = true;
                break;

            // Block start control node - indicates that following nodes will be pushed recursively from the next block.
            case BLOCK:
                thisBlockRes.ctlNodeCount++;
                thisBlockRes.blockCount++;

                if(nextNode.data!=null && nextNode.data.equals(CalcNode.BlockTypesData.FUNCTION_BLOCK))
                    thisBlockRes.funCount++;

                // The nextNode already contains all needed next block's information in it's attributes -
                // the BlockType (simple, function, set), and the function name if it's a function.
                // That information added on the parser. Now process new block recursively.
                RecursiveResult temp = startQueueProcessing(nextNode, level + 1, flags);

                thisBlockRes.addStats(temp);
                // Check if End of Expression node occured. Set the exit variables if yes.
                if (temp.code == RecursiveResult.Code.EXPR_END) {
                    thisBlockRes.code = RecursiveResult.Code.EXPR_END;
                    timeToQuit = true;
                }

                if(temp.blockRes != null){
                    // Set the resulting node as a new NextNode, and process it in next iteration.
                    nextNode = temp.blockRes;
                    continue;
                }
                break;

            /* =========================================
             * Check actual calculation nodes.
             * There is where chain comes into play: the ultimate goal is just a chain of Basic Operators
             * and Numbers, like 2+6*4/5.
             * All blocks and functions must be converted into this type.
             * Then ultimate calculation is done in the chain like this.
             *
             * - NO consequency errors are checked at this stage. Consequent same-type nodes like (+++222) are allowed.
             * - Errors like this are checked on basic chain processors.
             */
            // Basic operator (+,-,*,/)
            case BASIC_OP:
                thisBlockRes.bopCount++;
                basicOperationChain.addChild( nextNode );
                break;
            // Basic Number or Constant
            case CONSTANT:
                thisBlockRes.conCount++;
            case NUMBER:
                thisBlockRes.numCount++;
                basicOperationChain.addChild( nextNode );
                break;

            // Check unimplemented features (variables, lists, and all other node types which are not supported).
            default:
                OutF.logfn(print, level*2, "[NodeProcessThread]: ERROR: Unimplemented feature found! ("+nextNode.type.name()+")");
                throw new NodeProcessException(NodeProcessException.Type.UNKNOWN_ERROR,
                              NodeProcessException.UNIMPLEMENTED_FEATURE, nextNode);
            }

            // After checking variations, nullify NextNode to get ready for taking new one from Queue
            nextNode = null;
        }


        // Set the basic calculation node statistic.
        thisBlockRes.calcNodeCount += basicOperationChain.getChilds().size();

        // If the block we're working on is a Function, launch the Function Calculator on this node.
        if(rootNode.data != null && rootNode.data.equals(CalcNode.BlockTypesData.FUNCTION_BLOCK)){
            thisBlockRes.blockRes = calculateFunction(basicOperationChain);
        }
        // Perform Basic Chain calculations
        else if(rootNode.data == null || rootNode.data.equals(CalcNode.BlockTypesData.CALC_BLOCK)){
            thisBlockRes.blockRes = calculateBasicChain(basicOperationChain);
            //thisBlockRes.blockRes = new CalcNode(CalcNode.Types.NUMBER, "numb", new BigDecimal(1337));
        }
        else{
            thisBlockRes.blockRes = basicOperationChain;
        }

        return thisBlockRes;
    }

    /**
     * Function calculates a chain of simple operations, like 2+3*8/5.
     * - Valid supported operations:
     *   - Simple: + - * / %
     *   - Functional: ^
     *   - Bitwise: &
     *
     * - Processed Node Types:
     *   - Basic Operator
     *   - Number
     * All other node types throw an error.
     *
     * @param rootNode - block-type node, containing one layer of childs, which are basic operators and numbers.
     * @return Number representation of the chain's result.
     */
    private CalcNode calculateBasicChain(CalcNode rootNode) throws NodeProcessException {
        boolean print = OutF.LoggedMethods.StringCalculator_simpleOperationChain;

        // No error checking is performed because we assume it is called from startQueueProcessing(),
        // with all error checking already done, and the chain is of perfectly correct state.
        boolean onMulDiv = false, onExp = false, onOther = false;

        Pair<CalcNode, CalcNode> lastAS, lastMD, lastEXP, currPair = null, lastPair = null;

        List<CalcNode> nodeList = rootNode.getChilds();
        Iterator<CalcNode> iter = nodeList.iterator();

        CalcNode currNode;

        // Print the basic node chain
        OutF.logfn(print, "[ NodeProcessThread]: calculateBasicChain(). Basic OP chain:"+rootNode.toFullRecursiveString());

        lastAS = new Pair<>( new CalcNode(CalcNode.Types.NUMBER, null, new BigDecimal(0)),
                               new CalcNode(CalcNode.Types.BASIC_OP, "+", null) );

        lastMD = new Pair<>( new CalcNode(CalcNode.Types.NUMBER, null, new BigDecimal(1)),
                               new CalcNode(CalcNode.Types.BASIC_OP, "*", null) );

        lastEXP = new Pair<>( new CalcNode(CalcNode.Types.NUMBER, null, new BigDecimal(0)),
                               new CalcNode(CalcNode.Types.BASIC_OP, "^", null) );

        lastPair = new Pair<>( new CalcNode(CalcNode.Types.NUMBER, null, new BigDecimal(0)),
                               new CalcNode(CalcNode.Types.BASIC_OP, "+", null) );

        if( iter.hasNext() ){
            currNode = nodeList.get(0);
            if(currNode.type==CalcNode.Types.BASIC_OP && (currNode.data.equals("+") || currNode.data.equals("-"))) {
                currPair = new Pair<>( new CalcNode(CalcNode.Types.NUMBER, null, new BigDecimal(0)), currNode);
                iter.next();
            }
            else if(currNode.type==CalcNode.Types.NUMBER){
                currPair = null;
            }
            else throw new NodeProcessException(NodeProcessException.Type.INTERNAL_ERROR, 0, currNode);
        }

        while( iter.hasNext() ){
            if(currPair == null) {
                currPair = new Pair<>(iter.next(), (iter.hasNext() ? iter.next() :
                        new CalcNode(CalcNode.Types.BASIC_OP, "+", null)));
            }

            OutF.logfn(print, " calculateBasicChain(): got Pair: "+currPair.getKey()+" , "+currPair.getValue());

            // check errors
            if(currPair.getKey().type != CalcNode.Types.NUMBER ||
               currPair.getValue().type != CalcNode.Types.BASIC_OP){
                throw new NodeProcessException(NodeProcessException.Type.SYNTAX_ERROR,
                                NodeProcessException.CONSEQUENT_OPERATORS);
            }

            // 2+ 2* 2+ 2^ 2+

            // At this point it is known that key=NUMBER, value=BASIC_OP
            try {
                BigDecimal curNu = currPair.getKey().number;
                String curOp = currPair.getValue().data;
                String lastOp = lastPair.getValue().data;

                // Check current pair's operator
                switch( curOp ) {
                    case "+":
                    case "-":
                        // Make calculation based on last nodes, if operator change occurred.
                        if (onExp) {
                            if (lastOp.equals("^"))
                                lastEXP.getKey().number = lastEXP.getKey().number.pow(curNu.intValue());
                            else throw new NodeProcessException(NodeProcessException.Type.INTERNAL_ERROR, 0);

                            if (!onMulDiv)
                                curNu = lastEXP.getKey().number;
                            else {
                                if (lastMD.getValue().data.equals("*"))
                                    lastMD.getKey().number = lastMD.getKey().number.multiply(lastEXP.getKey().number);
                                else if (lastMD.getValue().data.equals("/"))
                                    lastMD.getKey().number = lastMD.getKey().number.divide(lastEXP.getKey().number, DECIMAL_SCALE, BigDecimal.ROUND_UP);
                                curNu = lastMD.getKey().number;
                            }
                        }
                        else if (onMulDiv && !onExp) {
                            if (lastOp.equals("*"))
                                lastMD.getKey().number = lastMD.getKey().number.multiply(curNu);
                            else if (lastOp.equals("/"))
                                lastMD.getKey().number = lastMD.getKey().number.divide(curNu, DECIMAL_SCALE, BigDecimal.ROUND_UP);
                            else throw new NodeProcessException(NodeProcessException.Type.INTERNAL_ERROR, 0);

                            curNu = lastMD.getKey().number;
                        }

                        // Make the calculations with the new curNu.
                        if(lastAS.getValue().data.equals("+"))
                            lastAS.getKey().number = lastAS.getKey().number.add(curNu);
                        else if(lastAS.getValue().data.equals("-"))
                            lastAS.getKey().number = lastAS.getKey().number.subtract(curNu);
                        else throw new NodeProcessException(NodeProcessException.Type.INTERNAL_ERROR, 0);

                        // Create a new lastAS: key is the result as-is, value (operator) is current operator we're sitting at.
                        lastAS.getValue().data = curOp;
                        // The last pair now will be the Add-Sub pair, because we are on Add-Sub.
                        lastPair = lastAS;

                        // Nullify the Exp and Mul pairs
                        lastMD = new Pair<>( new CalcNode(CalcNode.Types.NUMBER, null, new BigDecimal(1)),
                                             new CalcNode(CalcNode.Types.BASIC_OP, "*", null) );
                        lastEXP = new Pair<>( new CalcNode(CalcNode.Types.NUMBER, null, new BigDecimal(0)),
                                              new CalcNode(CalcNode.Types.BASIC_OP, "^", null) );

                        onMulDiv = false;
                        onExp = false;
                        break;

                    case "*":
                    case "/":
                        if (onExp) {
                            if (lastOp.equals("^"))
                                lastEXP.getKey().number = lastEXP.getKey().number.pow(curNu.intValue());
                            else throw new NodeProcessException(NodeProcessException.Type.INTERNAL_ERROR, 0);

                            if (!onMulDiv)
                                curNu = lastEXP.getKey().number;
                            else {
                                OutF.logfn(print, "onExp && onMulDiv: lastMD: "+lastMD+" , lastEXP: "+lastEXP);

                                if (lastMD.getValue().data.equals("*"))
                                    lastMD.getKey().number = lastMD.getKey().number.multiply(lastEXP.getKey().number);
                                else if (lastMD.getValue().data.equals("/"))
                                    lastMD.getKey().number = lastMD.getKey().number.divide(lastEXP.getKey().number, DECIMAL_SCALE, BigDecimal.ROUND_UP);
                                //curNu = lastMD.getKey().number;
                            }
                        }

                        // Make the calculations with the new curNu.
                        if(lastMD.getValue().data.equals("*"))
                            lastMD.getKey().number = lastMD.getKey().number.multiply(curNu);
                        else if(lastMD.getValue().data.equals("/"))
                            lastMD.getKey().number = lastMD.getKey().number.divide(curNu, DECIMAL_SCALE, BigDecimal.ROUND_UP);
                        else throw new NodeProcessException(NodeProcessException.Type.INTERNAL_ERROR, 0);

                        lastMD.getValue().data = curOp;
                        lastPair = lastMD;

                        // Nullify the Exp pair
                        lastEXP = new Pair<>( new CalcNode(CalcNode.Types.NUMBER, null, new BigDecimal(0)),
                                              new CalcNode(CalcNode.Types.BASIC_OP, "^", null) );
                        onMulDiv = true;
                        onExp = false;
                        break;

                    case "^":
                        if(!onExp){
                            lastEXP.getKey().number = curNu;
                            lastEXP.getValue().data = curOp;
                        }
                        else{
                            if (lastOp.equals("^"))
                                lastEXP.getKey().number = lastEXP.getKey().number.pow(curNu.intValue());
                            else throw new NodeProcessException(NodeProcessException.Type.INTERNAL_ERROR, 0);
                        }
                        lastPair = lastEXP;

                        onExp = true;
                        break;

                    default:
                        throw new NodeProcessException(NodeProcessException.Type.SYNTAX_ERROR,
                                NodeProcessException.UNIMPLEMENTED_FEATURE, currPair.getValue());
                }

            }
            catch (ArithmeticException e) {
                throw new NodeProcessException(NodeProcessException.Type.CALCULATION_ERROR);
            } catch (NullPointerException e) {
                throw new NodeProcessException(NodeProcessException.Type.SYNTAX_ERROR, 0);
            }

            currPair = null;
        }

        return new CalcNode(CalcNode.Types.NUMBER, "calculateBasicChain", lastAS.getKey().number);
    }

    private CalcNode calculateFunction(CalcNode funcNode){
        return new CalcNode(CalcNode.Types.NUMBER, "func", new BigDecimal(1337));
    }

    private static class RecursiveResult{
        public enum Code{
            EXPR_END,
            NORMAL
        }

        Code code;
        CalcNode blockRes;

        // Statistics of the block.
        int iterations = 0, bopCount = 0, numCount = 0, funCount = 0, conCount = 0;
        int specCount = 0, blockCount = 0, ctlNodeCount = 0, calcNodeCount = 0;

        //Result res;
        RecursiveResult(Code cod, BigDecimal bigRes){
            code = cod; blockRes = new CalcNode( CalcNode.Types.NUMBER, null, bigRes );
        }

        void addStats(RecursiveResult res){
            iterations += res.iterations;
            bopCount += res.bopCount;
            numCount += res.numCount;
            funCount += res.funCount;
            conCount += res.conCount;
            specCount += res.specCount;
            blockCount += res.blockCount;
            ctlNodeCount += res.ctlNodeCount;
            calcNodeCount += res.calcNodeCount;
        }

        @Override
        public String toString(){
            return "RecursiveResult: "+this.getClass()+"\n Code: "+code.name()+"\n BlockRes: "+blockRes+
                   "\n Stats: iterations:"+iterations+", bops:"+bopCount+", nums:"+numCount+", funs:"+funCount+
                   ", cons:"+conCount+"\n blocks:"+blockCount+", specs:"+specCount+", ctlNodes:"+ctlNodeCount+
                   ", calcNodes:"+calcNodeCount+"\n";
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
        String calcExpr;
        calcExpr = "2*3^2*2";
        /*calcExpr = "2*892 +    333.5+5^8 +log258(5)+(875+4-sin(pi)*2(opa())) +" +
        " 3*8781.122 ((0) (((2.654*2))    ((33453.54+5^2.58) +laog258(pi-(2*42.5446)))+(875+4.45654-sin(pi))*2(opa()))+(2))";
        */

        OutF.setOpened(true);

        System.out.println("[MainTester]: Starting StringCalculator Test. Expression:" +
                           (calcExpr.length() > 40 ? "(too long)": calcExpr));

        long start1 = System.nanoTime();
        BlockingQueue<AsyncQueueCalculatorPlugin.Result> resQue = new LinkedBlockingQueue<>();
        StringCalculator calc = new StringCalculator(resQue, null, 0);

        AsyncQueueCalculatorPlugin.Result res = calc.startCalculation(new Query(calcExpr, 0, QueryType.WHOLE_QUERY, 1));

        System.out.println("[MainTester]: Got result! Value: "+res+ "\n\nTime: " + (double) (System.nanoTime() - start1) / 1000.0 + " us\n");
    }
}

