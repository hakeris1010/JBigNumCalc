package jbignums.CalcProperties;

import jbignums.StringCalculator.StringCalculator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main state class API. The connection between GuiLand and MainLand is managed through this object.
 * - Working prinicple: Each interested thread registers Evt. Listeners, listening for specific
 *   CalStateEvent actionCommands, informing about state changes.
 * - Internally, GuiCalcState uses SwingWorker API to launch worker threads when EDT thread issues a
 *   specific command by calling the appropriate method.
 */

public class GuiCalcState //Thread-safe.
{
    /**
     *  The known ActionCommand strings
     */
    public static class Commands {
        public static final String CALC_PROGRESS_VALUE = "GCS_Calc_Progress";
        public static final String CALC_DONE = "GCS_Calc_Done";
        public static final String GUI_LAYOUT_CHANGED = "GCS_Gui_LayoutChange";
    }

    // Specifies how many active calculation worker threads in a pool.
    private static final int MAX_CALC_THREADS = 16;
    /**
     * Private properties defining current state and other stuff.
     */

    //=========    Core   ==========//
    // Event listeners, listening on EDT. Now it's SYNCHRONIZED - No more worries about threading!
    private final List<ActionListener> EDTlisteners = Collections.synchronizedList( new ArrayList<ActionListener>() );

    /**
     * The conditional AtomicBoolean variable which informs about Having Data in Queues.
     * - This variable is THE SAME for all CalculatorInstances - if set,
     *   it means one of the working calculator's result queue has pending data.
     * - We use the set() and notify() methods, to work as a condition variable.
     */
    private final AtomicBoolean queueDataPendingCondVar = new AtomicBoolean(false);
    /**
     * Master shutdown variable. If set, then all working threads must shut down.
     */
    private final AtomicBoolean needToShutDown = new AtomicBoolean(false);

    /**
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
            state.getCalc().startCalculation(calcExpression, 0);
        }
    }
    /**
     * State of the CalculatorInstance. Passed on a constructor. The EventHandler thread keeps track on it.
     */
    private class CalculatorInstanceState{
        public final AtomicBoolean needToStop = new AtomicBoolean(false);

        private final int ID;
        private final StringCalculator calc;

        public int getID(){ return ID; }
        public StringCalculator getCalc(){ return calc; }

        public CalculatorInstanceState(int id, StringCalculator cal){
            ID = id;
            calc = cal;
            // Set the variable by which we will signal the end of the job.
            calc.addTerminatingVariable(needToStop);
            calc.addTerminatingVariable(needToShutDown);
            calc.addQueueSignalVariable(queueDataPendingCondVar);
        }
    }
    /**
     * Added Gui-Compatible Calculator Objects.
     */
    private ArrayList<Class> calculatorClasses = new ArrayList<>();

    /**
     * The Thread Pool in which all calculation tasks will be done.
     */
    private final ExecutorService calcThreadPool = Executors.newFixedThreadPool(MAX_CALC_THREADS);
    /**
     * Currently working states contained in a Set - no duplicates.
     */
    private final SortedSet<CalculatorInstanceState> runningCalcStates = Collections.synchronizedSortedSet( new TreeSet<>() );

    // Calculator-specific states.
    private String currentQuery;
    private ArrayList<String> queryHistory;
    private Queue<String> pendingQueries;

    //========= GUI Layouts ==========//
    // GUI Layout and CalcModes
    private GuiCalcProps.CalcLayout currentGuiLayout;

    /** ============ Initialization ===========
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
        new Thread( () -> {
            while(!needToShutDown.get()){
                // If no data available on queues, wait until notified.
                synchronized (queueDataPendingCondVar) {
                    while (!queueDataPendingCondVar.get() && !needToShutDown.get()) {
                        try {
                            queueDataPendingCondVar.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(needToShutDown.get()) break;

                // At this point the variable is set --> data available. Check all queues for data.
                for(CalculatorInstanceState st : runningCalcStates){
                    if(needToShutDown.get()) // We must check for ShutDown at every iteration!
                       break;                // If shutdown is set, it is set in all StringCalculators too.
                    StringCalculator.Result res = st.getCalc().getIntermediateResultIfPending();
                    if(res != null) // Got result --> invoke all listeners.
                    {
                        ActionEvent ae = null;
                        // Check the Result Type to determine an event code.
                        if((res.resultType & StringCalculator.ResultType.END) == StringCalculator.ResultType.END)
                            ae = new ActionEvent(res, ActionEvent.ACTION_PERFORMED, Commands.CALC_DONE);
                        else if((res.resultType & StringCalculator.ResultType.INTERMEDIATE_DATA) == StringCalculator.ResultType.INTERMEDIATE_DATA){
                            ae = new ActionEvent(res, ActionEvent.ACTION_PERFORMED, Commands.CALC_PROGRESS_VALUE);
                        }
                        // Fire the event!
                        if(ae != null)
                            this.raiseEvent_OnEventDispatchThread( ae );
                    }
                }
            }
        } ).start();
    }

    /** ============= Core Methods ============
     * Add a new event listener to be executed on the EDT thread.
     * @param list
     */
    public void addEDTListener(ActionListener list) {
        EDTlisteners.add(list);
    }

    /**
     * Function fires event "event", and handlers are invoked on AWT Event Dispatch Thread,
     * because these events are being listened on the GUI.
     * @param event  - customly crafted ActionEvent.
     */
    public void raiseEvent_OnEventDispatchThread(ActionEvent event){
        // invokeLater() causes the Runnable to execute on EDT. We also use Lambdas.
        // If wr're on EDT, we call handlers directly.
        Runnable action = () -> {
            for (ActionListener a : EDTlisteners) {
                a.actionPerformed(event);
            }
        };

        if( javax.swing.SwingUtilities.isEventDispatchThread() ) {
            action.run();
        }
        else { // If not EDT, use the "invokeLater()".
            SwingUtilities.invokeLater( action );
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

    /** ============ Property Manipulation Methods ============
     * Sets the new GUI Layout, and fires "LayoutChanged" event to all attached listeners.
     * @param newLayout - new GUI layout object
     */
    public synchronized void setCalcLayout(GuiCalcProps.CalcLayout newLayout){
        currentGuiLayout = newLayout;
        // Fire a "Layout Changed" event.
        raiseEvent_OnEventDispatchThread( new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Commands.GUI_LAYOUT_CHANGED) );
    }
    public synchronized GuiCalcProps.CalcLayout getCalcLayout(){
        return currentGuiLayout;
    }

    /**
     * Function starts a new calculation on a specified calculator with a specified expression.
     * - A job could be done on a SwingWorker, but it seems it's easier to do it using standard Runnables and
     *   a BlockingQueue for checking the intermediate results.
     * - This way allows the StringCalculator to be framework-independent, otherwise if we used SwingWorker,
     *   to be efficient, the StringCalculator would have to extend SwingWorker to use the publish() for
     *   intermediate results.
     * @param calcExpr - expression written in the GrylCalc language.
     */
    public synchronized void launchNewCalculationTask(String calcExpr){
        // Launch with default ID: 0
        launchNewCalculationTask(calcExpr, 0);
    }
    public synchronized void launchNewCalculationTask(String calcExpr, int calcID){
        if(calcID < 0 || calcID >= calculatorClasses.size())
            throw new RuntimeException("Wrong index specified on launchNewCalculationTask()");
        // Create a new instance of a Calculator of given class.
        StringCalculator newCalc;
        try {
            newCalc = (StringCalculator)(calculatorClasses.get(calcID).newInstance());
        } catch (Exception e) {
            System.out.println("Can't cast new object to StringCalculator.");
            e.printStackTrace();
            return;
        }
        // Create a new CalculationInstanceState and corresponding CalculationInstance.
        // When the calculation starts executing in a Pool, this State will be added to the Currently Active ones.
        CalculatorInstanceState cs = new CalculatorInstanceState( calcID, newCalc );
        // Put the new Instance to execution! When it starts executing, new State will be added to list,
        // And the Control Thread will be able to check on it.
        calcThreadPool.execute( new CalculatorInstance(cs, calcExpr) );
    }

    /*public synchronized String getQuery(){ return query; }
    public synchronized void setQuery(String qu){ query = qu; }

    public synchronized ArrayList<String> getQueryHistory(){ return queryHistory; }
    public synchronized void setQueryHistory(ArrayList<String> qu){ queryHistory = qu; }*/
}
