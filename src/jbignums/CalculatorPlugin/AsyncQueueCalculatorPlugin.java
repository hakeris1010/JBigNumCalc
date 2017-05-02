package jbignums.CalculatorPlugin;

import com.sun.org.apache.regexp.internal.RE;
import jbignums.testolator.Calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AsyncQueueCalculatorPlugin implements CalculatorPlugin {
    /**
     * Base class for Results, and class for types of the results.
     * - The inner workings of the Result class are plugin-defined.
     * - The ResultType provides basic types which may be needed.
     * - If more functionality is needed, it can be extended.
     */
    public static class ResultType {
        public static final int START             = 1;
        public static final int INTERMEDIATE_DATA = 2;
        public static final int END               = 4;
    }

    public static class Result extends CalculatorPlugin.Result{
        // Header Fields
        protected long taskID;
        protected int resultType;

        public int getResultType(){ return resultType; }
        public long getTaskID(){ return taskID; }

        public Result(){ }
        public Result(int resultType1, long taskID1){
            resultType = resultType1;
            taskID = taskID1;
        }

        @Override
        public String toString(){
            return "\n"+this.getClass().getName()+":\n TaskID: "+taskID+"\n resultType:"+resultType;
        }
    }

    /**
     * Query class and type.
     */
    public static class QueryType {
        public static final int WHOLE_QUERY = 1;
        public static final int INIT_QUERY  = 2;
        public static final int INTERMEDIATE_QUERY = 4;
        public static final int CONCLUDING_QUERY   = 8;
    }

    public static class Query extends CalculatorPlugin.Query{
        protected int queryType;
        protected long taskID;

        public int getQueryType(){ return queryType; }
        public long getTaskID(){ return taskID; }
        public void setTaskID(long id){ taskID = id; }

        public Query(){ }
        public Query(int qType, long taskID1){ queryType = qType; taskID = taskID1; }

        @Override
        public String toString(){
            return "\n"+this.getClass().getName()+":\n TaskID: "+taskID+"\n queryType:"+queryType;
        }
    }

    /** ===========================================================================
     * Private fields
     */
    // Signals if the calculation is currently performed.
    // Should be accessed only through the API protected functions, because
    // at the end of calculation the object must be notified to wake up waiters for final result.
    private AtomicBoolean stillCalculating = new AtomicBoolean( false );

    // Lists of Variables for communicating and notifying other threads
    private final List<AtomicBoolean> terminators = Collections.synchronizedList( new ArrayList<>() );

    /* TODO: The New model: "One Queue, Many Workers (0-Many Handlers)".
             This model will use One shared queue for all calculator tasks to push their results. For task
             identification, we'll use initially assigned available unique ID number, which will be added to
             Result class as another header.
             The shared queue will be assigned in the constructor or dynamically.
     */
    /**
     * The fields to be accessed from the implementing classes.
     */
    protected BlockingQueue<Result> resultQueue;

    // The ID of this task. Assigned with the INIT query, at the start of the job.
    protected AtomicLong currentID = new AtomicLong(0);

    /** ===========================================================================
     * Protected methods
     */
    protected boolean isTerminatorSet(){
        for(int i = 0; i<terminators.size(); i++){
            // If at least one terminating variable is set, we must quit a job.
            if(terminators.get(i).get())
                return true;
        }
        return false;
    }

    /**
     * Can be used from the implementing classes to signal the end of calculation by notifying this object.
     */
    protected synchronized void setCalculationEnd(){
        stillCalculating.set(false);
        this.notify();
    }
    protected void setCalculationStart(){
        stillCalculating.set(true);
    }

    /** ===========================================================================
     *  Constructors
     */
    public AsyncQueueCalculatorPlugin(){ }
    public AsyncQueueCalculatorPlugin(BlockingQueue<Result> resultQue, List<AtomicBoolean> terminatingVars){
        resultQueue = resultQue;
        for(int i=0; i<terminatingVars.size(); i++){
            terminators.add( terminatingVars.get(i) );
        }
    }

    /**
     * Gets the calculation result, and block if set and still calculating.
     * @param block
     */
    public Result getCalcEndResult(boolean block){
        if (block && stillCalculating.get()) // Block until calculation is complete.
        {
            synchronized (this) {
                while (stillCalculating.get()) {
                    try {
                        // Calling wait() will block this thread until another thread calls notify() on the object.
                        // wait() function atomically released the lock, so other threads can enter the wait loop too.
                        // After notification, wait() returns and thread acquires the lock again.
                        this.wait();

                    } catch (InterruptedException e) {
                        // Happens if someone interrupts your thread
                        e.printStackTrace();
                    }
                }
            }
            // If stillCalculating became false, it means the "lastResult" is assigned with the final result.
        }
        // Return current result or last result.
        return getLastResult();
    }
    /**
     * Getters for private fields
     */
    public boolean isCalculating(){
        return stillCalculating.get();
    }
    public long getCurrentID(){ return currentID.get(); }

    /**
     * Set the new Result Queue reference.
     * @param queue - the Blocking Queue object.
     */
    public synchronized void setResultQueue(BlockingQueue<Result> queue){
        this.resultQueue = queue;
    }

    /**
     * Get the next Intermediate Result. Must not be called from This!
     * Block the thread until new value is present on Queue.
     */
    public static Result getWaitResultFromQueue(AsyncQueueCalculatorPlugin calc){
        if(calc.isCalculating() || !calc.resultQueue.isEmpty()) {
            try {
                return calc.resultQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Result getIntermediateResultIfPending(AsyncQueueCalculatorPlugin calc){
        if( !calc.resultQueue.isEmpty() ) {
            try {
                return calc.resultQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public synchronized boolean isResultPending(){
        return !(resultQueue.isEmpty());
    }

    /**
     * Set the variable by which we'll signal the calculator to end work.
      * @param termvar - AtomicBoolean working as a terminator.
     */
    public synchronized void addTerminatingVariable(AtomicBoolean termvar){
        terminators.add(termvar);
    }

    /**
     * Start the calculation SYNCHRONOUSLY. Blocks the calling thread until completed.
     * This should be called not from active thread.
     *
     * Polymorphism:
     * - Assign query and launch calculation on one function
     * - Assign earlier, launch later.
     * @param q - contains all data to be passed to the implementing class.
     */
    @Override
    public final Result startCalculation(CalculatorPlugin.Query q){
        assignQuery((Query)q);
        return startCalculation();
    }

    /** =========================================================================
     * Overridable methods.
     *
     * @return - the end result.
     */
    public abstract Result startCalculation();

    @Override
    public abstract void assignQuery(CalculatorPlugin.Query q);

    @Override
    public abstract Query getLastQuery();

    @Override
    public abstract Result getLastResult();

}
