package jbignums.StringCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
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
        public static final int START   = 1 << 1;
        public static final int INTERMEDIATE_DATA       = 1 << 2;
        public static final int INTERMEDIATE_PERCENTAGE = 1 << 4;
    }

    public class Result {
        public double dResult;
        public ArrayList<String> data;
        public DataType dataType;
        public int resultType;

        public Result(double dres, ArrayList<String> sres, DataType dtp, int rtp){
            dResult = dres;
            data = sres;
            dataType = dtp;
            resultType = rtp;
        }
    }

    /**
     * Private fields
     */
    protected String calcExpression;
    protected volatile int calcMode;
    protected Result lastResult;
    protected volatile boolean stillCalculating = false;

    // Lists of Variables for communicating and notifying other threads
    protected final List<AtomicBoolean> terminators = Collections.synchronizedList( new ArrayList<>() );
    protected final List<AtomicBoolean> queueSignalers = Collections.synchronizedList( new ArrayList<>() );

    // TODO: Use this to check for deadlocks, so that blocking functions could not be called from Worker threads.
    protected long[] workerThreadIDS;

    /**
     * Use this queue to get and wait for intermediate results async'ly.
     */
    protected final static int QUEUE_MAX = 16;
    protected final LinkedBlockingQueue<Result> resultQueue = new LinkedBlockingQueue<>(QUEUE_MAX);

    /**
     * Constructors
     */
    public StringCalculator(){ }
    public StringCalculator(AtomicBoolean terminatingVariable, AtomicBoolean queueSignalVariable, int calculationMode){
        terminators.add( terminatingVariable );
        queueSignalers.add( queueSignalVariable );
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
     * @param mode - Not yet implemented. Pass 0.
     */
    public void startCalculation(String expression, int mode){
        assignExpression(expression, mode);
        startCalculation();
    }
    public synchronized void assignExpression(String expression, int mode){
        calcExpression = expression;
        calcMode = mode;
    }

    // Actual implementation here
    public void startCalculation(){
        /* TODO */
        synchronized (this) {
            stillCalculating = true;
        }

        // Perform actual calculation there.

        Result datLastRes = new Result(1.0, null, DataType.DEFAULT, ResultType.END);

        // Push end result to queue. When taken and checked and determined END, the caller will exit it's LooP.
        // No syncing is needed, because BlockingQueue is thread-safe.
        resultQueue.add( datLastRes );

        synchronized (this) {
            lastResult = datLastRes;
            stillCalculating = false;
            this.notifyAll(); // Notify all waiters that calculatione is done.
        }
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
     * Checks if still calc'ing and returns if so.
     */
    public boolean isCalculating(){
        return stillCalculating;
    }

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
    public synchronized void addTerminatingVariable(AtomicBoolean termvar){ terminators.add(termvar); }
    /**
     * Set the variable which acts as a Condition Variable for notifying if data is pending on Results Queue.
     * @param signalvar
     */
    public synchronized void addQueueSignalVariable(AtomicBoolean signalvar){ queueSignalers.add(signalvar); }

}

