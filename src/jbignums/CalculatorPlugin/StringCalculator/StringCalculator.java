package jbignums.CalculatorPlugin.StringCalculator;

import jbignums.CalculatorPlugin.AsyncQueueCalculatorPlugin;
import jbignums.CalculatorPlugin.CalculatorPlugin;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
    /** DataTypes
     */
    public enum DataType {
        DEFAULT,
        EQUATION;
    }

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

    public static class Result extends AsyncQueueCalculatorPlugin.Result{
        public DataType dataType;
        public double dblResult;
        public String strResult;
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

    /** = = = = = == = = = = == = = = = == = = = = == = = = = = //
     * Private fields
     */
    protected Query lastQuery;
    protected volatile int calcMode = 0;

    protected Result lastResult;

    // The Parser
    StringExpressionParser currentParser;

    /** = = = = = == = = = = == = = = = == = = = = == = = = = = //
     * Constructors
     */
    public StringCalculator(){ super(); }
    public StringCalculator(BlockingQueue resultQue, List<AtomicBoolean> termVars, int calculationMode){
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
    public synchronized void assignQuery(CalculatorPlugin.Query query){
        if( query==null ? true : !(Query.class.isAssignableFrom( query.getClass() )) ){ // If wrong class, throw exception.
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
        if(resultQueue == null || lastQuery == null || this.isCalculating())
            throw new RuntimeException("Can't start calculation. Missing essential parameters or currently calculating.");

        System.out.println("[StringCalculator.startCalculation()]: Current query values: "+lastQuery);

        // Mark the start of calculation (Superclass-handled).
        setCalculationStart();

        // Perform actual calculation there.
        // TODO: Check the TERMINATING VARIABLES while doing lenghty tasks.
        // isTerminatorSet()...

        // Create a new Result object (Header and Payload)
        Result datLastRes = new Result( ResultType.END, currentID.get(), DataType.DEFAULT, 1.0, "1.0", null);

        // Push result to shared queue. Many calculator are using it, but instance is determined using ID field.
        // No syncing is needed, because BlockingQueue is thread-safe.
        System.out.println("[StringCalculator.startCalculation()]: pushing Result to queue: "+datLastRes);
        resultQueue.add( datLastRes );

        // At the end, perform the ending inter-thread jobs of setting specific variables and stuff.
        synchronized (this) {
            lastResult = datLastRes;
        }
        // Mark the end of calculation (Superclass-handled).
        // Notifies the waiters and sets isCalculating to false.
        System.out.print("[StringCalculator.startCalculation()]: Ending Calculation...\n");
        setCalculationEnd();

        return lastResult;
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
        return lastResult;
    }
}

