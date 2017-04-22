package jbignums.StringCalculator;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

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
    public enum Type{
        DEFAULT,
        NUMBER,
        MATRIX,
        EXPRESSION,
        EQUATION
    }

    public class Result {
        public double dResult;
        public ArrayList<String> data;
        Type type;
    }
    public class IntermediateResult extends Result{ }

    /** Properties.
     */
    protected BlockingQueue<IntermediateResult> intermResults;

    /**
     * Start the calculation SYNCHRONOUSLY. Blocks the calling thread until completed.
     * This should be called from
     *
     * @param expression - String representation of CalcExpression, e.g. "2+2" or "sqrt(1)"
     * @param mode - Not yet implemented. Pass 0.
     */
    public synchronized void startCalculation(String expression, int mode) {

    }

    /**
     * Get intermediate results list;
     * @return - e.g. when calculation equation system, returns one result, OR returns Progress Percentage.
     */
    public synchronized List<IntermediateResult> getIntermediateDataList(){
        return intRes;
    }

}
