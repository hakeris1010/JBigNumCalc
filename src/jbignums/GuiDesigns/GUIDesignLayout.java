package jbignums.GuiDesigns;

import jbignums.CalcProperties.GuiCalcState;

import javax.swing.*;

/**
 *  Basic interface for all Gui Designs
 */
public interface GUIDesignLayout {
    void create(GuiState state);
    JPanel getRootContentPanel();
}

