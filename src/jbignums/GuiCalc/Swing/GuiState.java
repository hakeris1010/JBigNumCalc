package jbignums.GuiCalc.Swing;

import jbignums.GuiCalc.Swing.GuiDesigns.GUIDesignLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public interface GuiState {
    //void addEDTListener(ActionListener list);
    void raiseEvent_OnEventDispatchThread(ActionEvent event);

    void setGuiDesignLayout(GUIDesignLayout layout);
    void setGuiDesignLayout(Class layoutClass);
    GUIDesignLayout getGuiDesignLayout();

    void setGuiMenu(GUIMenu menu);
    GUIMenu getGuiMenu();
}
