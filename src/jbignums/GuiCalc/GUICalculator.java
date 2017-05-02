package jbignums.GuiCalc;

import jbignums.CalculatorPlugin.CalculatorPlugin;

import java.util.List;

public interface GUICalculator {
    // Event command codes of the GUI calculator.
    final class Commands {
        public static final String CALC_QUERY_ENTERED = "GUI_CalcQueryEntered";
        public static final String CALC_EXIT_REQUESTED = "GUI_ExitRequested";
    }

    /**
     * Gets a list of supported CalcPlugins. Used to examine.
     * @return - plugin classes.
     */
    List<Class> getSupportedCalculatorPlugins();

    /**
     * Gets the XML document describing the desirable layout.
     */
    //XMLObject getLayoutXML();

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
    void sendCalculationResult(CalculatorPlugin.Result res);

    // TODO: The GuiState might be even not needed in the new model!
    /**
     * Standard creation method.
     * @param cState - a state object to which GUI is communicating.
     */
    //void create(GuiState cState);
}
