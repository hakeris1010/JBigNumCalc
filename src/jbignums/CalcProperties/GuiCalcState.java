package jbignums.CalcProperties;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Queue;

// Calc State Events

public class GuiCalcState //Thread-safe.
{
    // Event listeners, listening on EDT.
    private ArrayList<ActionListener> EDTlisteners;
    //private ArrayList<ActionListener> otherListeners;

    // Calculator-specific states.
    private String currentQuery;
    private ArrayList<String> queryHistory;
    private Queue<String> pendingQueries;

    // Getters, setters
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
        SwingUtilities.invokeLater(() -> {
            for(ActionListener a : EDTlisteners){
                a.actionPerformed(event);
            }
        });
    }

    /*public synchronized String getQuery(){ return query; }
    public synchronized void setQuery(String qu){ query = qu; }

    public synchronized ArrayList<String> getQueryHistory(){ return queryHistory; }
    public synchronized void setQueryHistory(ArrayList<String> qu){ queryHistory = qu; }*/
}
