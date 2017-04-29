package jbignums.StringCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  A Calculator designed to work concurrently.
 *  Extends the functionality of a SwingWorker.
 *  Principle:
 *  - A new job is started by calling startNewJob(), passing a String representation of CalcExpression.
 *  - While the job is being Performed, intermediate data is appended to the private Intermediate CalcResults queue.
 *  - The user can get the Intermediate Results by calling the getIntermediateResults() function from any thread.
 *  - When the job is done, the end result is published as a specific format.
 */
public abstract class StringCalculator{
    /** DataTypes
     */
    public enum DataType {
        DEFAULT,
        NUMBER,
        MATRIX,
        EXPRESSION,
        EQUATION
    }
    public static class ResultType{
        public static final int END     = 1 << 0;
        public static final int INTERMEDIATE_DATA       = 1 << 2;
        public static final int INTERMEDIATE_PERCENTAGE = 1 << 4;
    }

    public static class Result {
        // Header Fields
        public long taskID;
        public DataType dataType;
        public int resultType;
        // Data Payload
        public double dblResult;
        public String strResult;
        public BigDecimal bigResult;

        public Result(){ }
        public Result(long tid, DataType dataType1, int resultType1, double dres, String sres, BigDecimal bigres){
            taskID = tid;
            dataType = dataType1;
            resultType = resultType1;

            dblResult = dres;
            strResult = sres;
            bigResult = bigres;
        }
    }

    /** = = = = = == = = = = == = = = = == = = = = == = = = = = //
     * Private fields
     */
    protected String calcExpression;
    protected volatile int calcMode = 0;

    protected AtomicLong currentID = new AtomicLong(0);
    protected volatile boolean stillCalculating = false;

    protected Result lastResult;

    // Lists of Variables for communicating and notifying other threads
    protected final List<AtomicBoolean> terminators = Collections.synchronizedList( new ArrayList<>() );

    /* DEPRECATED: The following queueSignaler list is needed for the "One Handler, Many Queues" model,
             so that all separate calculators could have their own respective queues, and signal the waiter
             using a condition variable, which is the following. But this is inefficient, because we
             already use a BlockingQueue with waiting enabled so handlers can wait.
     */
    //protected final List<AtomicBoolean> queueSignalers = Collections.synchronizedList( new ArrayList<>() );

    /* TODO: The New model: "One Queue, Many Workers (0-Many Handlers)".
             This model will use One shared queue for all calculator tasks to push their results. For task
             identification, we'll use initially assigned available unique ID number, which will be added to
             Result class as another header.
             The shared queue will be assigned in the constructor or dynamically.
     */
    protected BlockingQueue<Result> resultQueue;

    // TODO: Use this to check for deadlocks, so that blocking functions could not be called from Worker threads.
    protected long[] workerThreadIDS;

    /** = = = = = == = = = = == = = = = == = = = = == = = = = = //
     * Private  Methods
     */
    private boolean isTerminatorSet(){
        for(int i = 0; i<terminators.size(); i++){
            // If at least one terminating variable is set, we must quit a job.
            if(terminators.get(i).get())
                return true;
        }
        return false;
    }

    /** = = = = = == = = = = == = = = = == = = = = == = = = = = //
     * Constructors
     */
    public StringCalculator(){ }
    public StringCalculator(BlockingQueue<Result> resultQue, AtomicBoolean terminatingVariable, int calculationMode){
        terminators.add( terminatingVariable );
        resultQueue = resultQue;
        calcMode = calculationMode;
    }

    /**
     * Start the calculation SYNCHRONOUSLY. Blocks the calling thread until completed.
     * This should be called not from active thread.
     *
     * Polymorphism:
     * - Assign expr and launch calculation on 1 function
     * - Assign earlier, launch later.
     *
     * @param expression - String representation of CalcExpression, e.g. "2+2" or "sqrt(1)"
     * @param ID - The unique ID of the job being started. Usually equals the hash code of current
     *             calculator object, because 1 calculator object can perform 1 job at a time.
     * @param mode - Not yet implemented. Pass 0.
     */
    public Result startCalculation(String expression, long ID, int mode){
        assignExpression(expression, ID, mode);
        return startCalculation();
    }
    public synchronized void assignExpression(String expression, long ID, int mode){
        currentID.set( ID );
        calcExpression = expression;
        calcMode = mode;
    }

    // Actual implementation here
    public Result startCalculation() throws RuntimeException {
        // Perform validation check on the essential variables.
        if(calcExpression == null || resultQueue == null)
            throw new RuntimeException("Can't start calculation. Missing essential parameters.");

        // Before starting calculation, inter-thread starting operations.
        synchronized (this) {
            stillCalculating = true;
        }

        // Perform actual calculation there.
        // TODO: Check the TERMINATING VARIABLES while doing lenghty tasks.
        // isTerminatorSet()...

        // Create a new Result object (Header and Payload)
        Result datLastRes = new Result( currentID.get(), DataType.DEFAULT, ResultType.END,
                                        1.0, "1.0", null);

        // Push result to shared queue. Many calculator are using it, but instance is determined using ID field.
        // No syncing is needed, because BlockingQueue is thread-safe.
        resultQueue.add( datLastRes );

        // At the end, perform the ending inter-thread jobs of setting specific variables and stuff.
        synchronized (this) {
            lastResult = datLastRes;
            stillCalculating = false;
            this.notifyAll(); // Notify all waiters that calculatione is done.
        }

        return lastResult;
    }

    /**
     * Gets the calculation result, and block if set and still calculating.
     * @param block
     */
    public Result getCalcEndResult(boolean block){
        if (block && stillCalculating) // Block until calculation is complete.
        {
            synchronized (this) {
                while (stillCalculating) {
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
        return lastResult;
    }
    /**
     * Getters for private fields
     */
    public boolean isCalculating(){
        return stillCalculating;
    }
    public long getCurrentID(){ return currentID.get(); }
    public synchronized String getLastExpression(){ return calcExpression; }

    /**
     * Set the new Result Queue reference.
     * @param queue - the Blocking Queue object.
     */
    public synchronized void setResultQueue(BlockingQueue<Result> queue){
        this.resultQueue = queue;
    }

    public synchronized BlockingQueue<Result> getResultQueue(){ return resultQueue; }

    /**
     * Get the next Intermediate Result. Must not be called from This!
     * Block the thread until new value is present on Queue.
     */
    public synchronized Result getWaitIntermediateResult(){
        if(!stillCalculating) // Calculation is not happening at this moment.
            return lastResult;

        // TODO: Also check if caller is not "this" to prevent DeadLock.
        // Return the top of queue, and check for exception if interrupted.
        try {
            return resultQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public synchronized Result getIntermediateResultIfPending(){
        if( !resultQueue.isEmpty() ) {
            try {
                return resultQueue.take();
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

}

