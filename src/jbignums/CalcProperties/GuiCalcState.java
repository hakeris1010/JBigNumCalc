package jbignums.CalcProperties;

import jbignums.GuiCalc.Swing.GuiDesigns.GUIDesignLayout;
import jbignums.GuiCalc.Swing.GUIMenu;
import jbignums.GuiCalc.Swing.GuiState;
import jbignums.StringCalculator.StringCalculator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main state class API. The connection between GuiLand and MainLand is managed through this object.
 * - Working prinicple: Each interested thread registers Evt. Listeners, listening for specific
 *   CalStateEvent actionCommands, informing about state changes.
 * - Internally, GuiCalcState uses SwingWorker API to launch worker threads when EDT thread issues a
 *   specific command by calling the appropriate method.
 */

public class GuiCalcState  //Thread-safe.
{
    /** = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * Constants
     *
     * The known ActionCommand strings
     */
    public static class Commands {
        public static final String CALC_PROGRESS_VALUE = "GCS_Calc_Progress";
        public static final String CALC_DONE = "GCS_Calc_Done";
        public static final String GUI_QUIT_POSTED = "GCS_Gui_QuitMsgPosted";
    }

    // Specifies how many active calculation worker threads in a pool.
    private static final int MAX_CALC_THREADS = 4;
    // How many results can be placed into the result queue.
    private static final int CALC_MAX_QUEUE = 128;

    /** = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * Inner Classes
     *
     * Calculating thread runner.
     * Used in a ThreadPooL when submitting new task.
     * The passed State must be Already FULLY INITIALIZED using a constructor!
     */
    private class CalculatorInstance implements Runnable {
        private final CalculatorInstanceState state;
        private final String calcExpression;

        public CalculatorInstance(CalculatorInstanceState st, String cExpression){
            state = st;
            calcExpression = cExpression;
        }

        @Override
        public void run() {
            // Launch the calculation on this worker thread.
            state.getCalc().startCalculation(calcExpression, state.getID(), 0);
        }
    }
    /**
     * State of the CalculatorInstance. Passed on a constructor. The EventHandler thread keeps track on it.
     */
    private class CalculatorInstanceState {
        public final AtomicBoolean needToStop = new AtomicBoolean(false);

        private final long taskID;
        private final StringCalculator calc;

        public long getID(){ return taskID; }
        public StringCalculator getCalc(){ return calc; }

        public CalculatorInstanceState(StringCalculator cal){
            // Set the Unique Job ID - The hash of calculator's instance.
            taskID = cal.hashCode();
            calc = cal;

            // Set the calculator's inter-thread communication parameters.
            calc.addTerminatingVariable(needToStop);
            calc.addTerminatingVariable(needToShutDown);
            calc.setResultQueue(calcResultQueue);
        }
    }

    /** = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * Private Fields
     */
    // Event listeners, listening on EDT. Now it's SYNCHRONIZED - No more worries about threading!
    //private final List<ActionListener> EDTlisteners = Collections.synchronizedList( new ArrayList<ActionListener>() );
    private final List<XEventListener> eventListeners = Collections.synchronizedList( new ArrayList<>() );

    // Master shutdown variable. If set, then all working threads must shut down.
    private final AtomicBoolean needToShutDown = new AtomicBoolean(false);

    // Externally Added Gui-Compatible Calculator Objects.
    private ArrayList<Class> calculatorClasses = new ArrayList<>();

    // The Thread Pool in which all calculation tasks will be done.
    private final ExecutorService calcThreadPool = Executors.newFixedThreadPool(MAX_CALC_THREADS);
    // The control thread which gets results from workers, and notifies them to stop on conditions.
    private final Thread ControlThread;

    // Currently working states contained in a Set - no duplicates.
    private final SortedSet<CalculatorInstanceState> runningCalcStates = Collections.synchronizedSortedSet( new TreeSet<>() );

    /** The Shared Result queue.
     *  - This is the core of the new "One Queue, Many Workers" model. Each working Calculator Task will
     *    push it's results to this one shared queue.
     *  - The separate tasks will be identified by unique ID number (hashCode()) of calc.object.
     *  - This eliminated the need for overhead using notification by condition variables.
     *    We just get the result when it's available, fire an event, and the event listeners do their
     *    job based on task ID of the result.
     */
    private LinkedBlockingQueue<StringCalculator.Result> calcResultQueue = new LinkedBlockingQueue<>(CALC_MAX_QUEUE);

    // Calculator-specific states.
    private ArrayList<String> queryHistory = new ArrayList<>();

    // UPDATE: GUI and Layouts are no longer managed in GuiCalcState.

    /** = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * Private Methods
     *
     * ============ Initialization ===========
     * Launch control thread and initialize variables in a constructor.
     */
    //public GuiCalcState()
    {
        // At first, add the basic ID=0 calculator to the array.
        calculatorClasses.add(StringCalculator.class.getClass());

        /**
         * Create and Launch the Task Control thread.
         * This thread polls all of the tasks in ThreadPool for intermediate (or final) results,
         * takes them from queues, and fires appropriate events to the attached listeners on EDT.
         */
        ControlThread = new Thread( () -> {
            while(!needToShutDown.get()){
                // Check for shutdown at every iteration.
                if(needToShutDown.get()) break;

                StringCalculator.Result res = null;
                try {
                    // Wait until the result appears at the queue.
                    // If terminate request is passed on this object, the queue will be interrupted.
                    res = calcResultQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(res != null){ // Got result --> invoke all listeners.
                    XEvent ae = null;
                    // Check the Result Type to determine an event code.
                    if((res.resultType & StringCalculator.ResultType.END) == StringCalculator.ResultType.END)
                        ae = new XEvent(res, XEvent.ACTION_PERFORMED, Commands.CALC_DONE);
                    else if((res.resultType & StringCalculator.ResultType.INTERMEDIATE_DATA) == StringCalculator.ResultType.INTERMEDIATE_DATA){
                        ae = new XEvent(res, XEvent.ACTION_PERFORMED, Commands.CALC_PROGRESS_VALUE);
                    }
                    // Fire the event!
                    if(ae != null) {
                        //this.raiseEvent_OnEventDispatchThread( ae );
                        this.dispatchXEvent( ae );
                    }
                }
            }
        } );
        ControlThread.start();
    }

    /** = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * Public Methods
     *
     * ============= Core Methods ============
     * Add a new event listener to be executed on the EDT thread.
     * @param list
     */
    public void addXEventListener(XEventListener list) {
        eventListeners.add(list);
    }

    /**
     * Function fires event "event", and handlers are invoked on AWT Event Dispatch Thread,
     * because these events are being listened on the GUI.
     * @param event  - customly crafted ActionEvent.
     */
    public void dispatchXEvent(XEvent event){
        // invokeLater() causes the Runnable to execute on EDT. We also use Lambdas.
        // If wr're on EDT, we call handlers directly.
        for (int a=0; a < eventListeners.size(); a++) {
            eventListeners.get(a).actionPerformed(event);
        }
    }

    /** =============== Calculator Management =============
     * Calculator array modification methods.
     * Basics: add, get, getCount.
     * @param calc - calculator to add.
     */
    public synchronized void addCalculator(Class calc){
        if( StringCalculator.class.isAssignableFrom(calc) )
            calculatorClasses.add(calc);
    }
    public synchronized int getCalculatorCount(){ return calculatorClasses.size(); }
    public synchronized Class getCalculator(int pos){ return calculatorClasses.get(pos); }

    /**
     * Function starts a new calculation on a specified calculator with a specified expression.
     * - A job could be done on a SwingWorker, but it seems it's easier to do it using standard Runnables and
     *   a BlockingQueue for checking the intermediate results.
     * - This way allows the StringCalculator to be framework-independent, otherwise if we used SwingWorker,
     *   to be efficient, the StringCalculator would have to extend SwingWorker to use the publish() for
     *   intermediate results.
     * @param calcExpr - expression written in the GrylCalc language.
     * @return The unique Task ID number, identifying the task which has been stated.
     *         Will later be used for result identification on event handlers.
     */
    public long launchNewCalculationTask(String calcExpr){
        // Launch with default ID: 0
        return launchNewCalculationTask(calcExpr, 0);
    }
    public synchronized long launchNewCalculationTask(String calcExpr, int calcID){
        if(needToShutDown.get())
            return 0;
        if(calcID < 0 || calcID >= calculatorClasses.size())
            throw new RuntimeException("Wrong index specified on launchNewCalculationTask()");

        // Create a new instance of a Calculator of given class.
        StringCalculator newCalc;
        try {
            newCalc = (StringCalculator)(calculatorClasses.get(calcID).newInstance());
        } catch (Exception e) {
            System.out.println("Can't cast new object to StringCalculator.");
            e.printStackTrace();
            return 0;
        }

        // Add current query to history.
        queryHistory.add(calcExpr);

        // Create a new CalculationInstanceState and corresponding CalculationInstance.
        // When the calculation starts executing in a Pool, this State will be added to the Currently Active ones.
        CalculatorInstanceState cs = new CalculatorInstanceState( newCalc );
        // Put the new Instance to execution! When it starts executing, new State will be added to list,
        // And the Control Thread will be able to check on it.
        calcThreadPool.execute( new CalculatorInstance(cs, calcExpr) );
        // Return the assigned Task ID.
        return cs.getID();
    }

    /**
     * Function sets the Shutdown flag to signal all threads to start termination,
     * and sets the ThreadPool to not accept any new tasks.
     */
    public synchronized void postQuitMessage(){
        // Firstly, shutdown background threads by setting appropriate vars asynchronously.
        needToShutDown.set(true);
        // Wake up the control thread if it is waiting on the queue, by adding a dummy result to queue.
        calcResultQueue.add( new StringCalculator.Result() );

        calcThreadPool.shutdown(); // Signal the WorkerPool to not accept any new tasks.
        // Finally, raise event that QUIT has been posted.
        //raiseEvent_OnEventDispatchThread( new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Commands.GUI_QUIT_POSTED) );
    }

    /**
     * Function waits for all tasks to finish.
     * @param postQuitMsg - if set, posts Quit message before waiting.
     */
    public void waitForTasksEnd(boolean postQuitMsg){
        if(postQuitMsg)
            postQuitMessage();
        // Post a shutdown request to the worker pool, and wait.
        try {
            calcThreadPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Join the control thread.
        try {
            ControlThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<String> getQueryHistory(){ return queryHistory; }
}
