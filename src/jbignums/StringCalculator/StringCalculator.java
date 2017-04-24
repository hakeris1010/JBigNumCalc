package jbignums.StringCalculator;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

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
    public class IntermediateResult extends Result {
        public IntermediateResult(double d, ArrayList<String> ar, DataType dtp, int rtp){
            super(d, ar, dtp, rtp);
        }
    }

    /**
     * Private fields
     */
    protected Result lastResult;
    protected boolean stillCalculating = false;

    protected String calcExpression;
    protected int calcMode;

    // TODO: Use this to check for deadlocks, so that blocking functions could not be called from Worker threads.
    protected long[] workerThreadIDS;

    /**
     * Use this queue to get and wait for intermediate results async'ly.
     */
    protected final static int QUEUE_MAX = 16;
    protected final LinkedBlockingQueue<Result> resultQueue = new LinkedBlockingQueue<>(QUEUE_MAX);

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
    public synchronized void startCalculation(String expression, int mode){
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

        // Push end result to queue. When taken and checked and determined END, the caller will exit it's LooP.
        // No syncing is needed, because BlockingQueue is thread-safe.
        resultQueue.add(new Result(1.0, null, DataType.DEFAULT, ResultType.END));

        synchronized (this) {
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
                /*
                // Get result from Results Queue, blocking until present. As we're in a While Loop, we check
                // if calculation is still going.
                Result res = getWaitIntermediateResult();
                // Check the End Flag Bit
                if((res.resultType & ResultType.END) == ResultType.END){
                    return res;
                }*/
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
        // TODO: Check if caller is not "this" to prevent DeadLock.
        // Return the top of queue, and check for exception if interrupted.
        try {
            return resultQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized boolean isResultPending(){
        return !(resultQueue.isEmpty());
    }
}
