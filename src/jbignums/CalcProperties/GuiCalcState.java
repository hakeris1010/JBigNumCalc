package jbignums.CalcProperties;

import jbignums.StringCalculator.StringCalculator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Queue;

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
    public static final String CALC_PROGRESS_VALUE = "GCS_Calc_Progress";
    public static final String CALC_DONE_GOOD = "GCS_Calc_Done_Good";
    public static final String CALC_DONE_ERROR = "GCS_Calc_Done_Error";
    public static final String GUI_LAYOUT_CHANGED = "GCS_Gui_LayoutChange";

    /**
     * Private properties defining current state and other stuff.
     */

    //=========    Core   ==========//
    // Event listeners, listening on EDT.
    private ArrayList<ActionListener> EDTlisteners;
    //private ArrayList<ActionListener> otherListeners;

    //========= Calculator =========//
    // Added Calculators
    private ArrayList<StringCalculator> calculators;

    // Calculator-specific states.
    private String currentQuery;
    private ArrayList<String> queryHistory;
    private Queue<String> pendingQueries;

    //========= GUI Layouts ==========//
    // GUI Layout and CalcModes
    private GuiCalcProps.CalcLayout currentGuiLayout;

    /** ============= Core Methods ============
     *
     * Add a new event listener to be executed on the EDT thread.
     * @param list
     */
    public synchronized void addEDTListener(ActionListener list) {
        EDTlisteners.add(list);
    }

    /**
     * Function fires event "event", and handlers are invoked on AWT Event Dispatch Thread,
     * because these events are being listened on the GUI.
     * @param event  - customly crafted ActionEvent.
     */
    public synchronized void raiseEvent_OnEventDispatchThread(ActionEvent event){
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

    /** ============ Property Manipulation Methods ============
     * Sets the new GUI Layout, and fires "LayoutChanged" event to all attached listeners.
     * @param newLayout - new GUI layout object
     */
    public synchronized void setCalcLayout(GuiCalcProps.CalcLayout newLayout){
        currentGuiLayout = newLayout;
        // Fire a "Layout Changed" event.
        raiseEvent_OnEventDispatchThread( new ActionEvent(this, ActionEvent.ACTION_PERFORMED, GUI_LAYOUT_CHANGED) );
    }
    public GuiCalcProps.CalcLayout getCalcLayout(){
        return currentGuiLayout;
    }

    // Calculator manipulation functions

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
        if(calcID < 0 || calcID >= calculators.size())
            throw new RuntimeException("Wrong index specified on launchNewCalculationTask()");

        StringCalculator calc = calculators.get(calcID);

        // Launch calculation in new separate thread, and get intermediate results on this thread.
        new Thread( () -> {
            // The calculation spins in this worker, no other thread are directly affected.
            calc.startCalculation( calcExpr, 0 );
        } ).start();

        // Get intermediate results in a loop.
        while(true){
            StringCalculator.Result res = calc.getWaitIntermediateResult();
            // Check if End Result
            if( (res.resultType & StringCalculator.ResultType.END) == StringCalculator.ResultType.END){
                raiseEvent_OnEventDispatchThread( new ActionEvent(this, ActionEvent.ACTION_PERFORMED, CALC_DONE) );
            }
        }
    }

    /*public synchronized String getQuery(){ return query; }
    public synchronized void setQuery(String qu){ query = qu; }

    public synchronized ArrayList<String> getQueryHistory(){ return queryHistory; }
    public synchronized void setQueryHistory(ArrayList<String> qu){ queryHistory = qu; }*/
}
