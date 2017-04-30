package jbignums.GuiCalc;

import jbignums.StringCalculator.StringCalculator;

public interface GUICalculator {
    // Event command codes of the GUI calculator.
    final class Commands {
        public static final String CALC_QUERY_ENTERED = "GUI_CalcQueryEntered";
        public static final String CALC_EXIT_REQUESTED = "GUI_ExitRequested";
    }

    final class QueryData {
        public String calcQuery;
        public long ID;
    }

    /**
     * Add an Implementation-defined action event listener.
     * Listener might be used to listen for events from the Commands class above.
     * E.g. when calculation query is entered, an event will be fired to all the listeners.
     * @param listener - a reference to object representing a listener.
     */
    void addActionListener(Object listener);

    /**
     * Called when calculation is completed. Sends a result object to GUI for showing.
     * @param res - calculation result.
     */
    void sendCalculationResult(StringCalculator.Result res);

    // TODO: The GuiState might be even not needed in the new model!
    /**
     * Standard creation method.
     * @param cState - a state object to which GUI is communicating.
     */
    //void create(GuiState cState);
}
