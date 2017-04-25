package jbignums.CalcDesigns;

import jbignums.CalcProperties.GuiCalcState;

import javax.swing.*;

/**
 *  Basic interface for all Gui Designs
 */
public interface GUIDesignLayout {
    void create(GuiCalcState state);
    JPanel getRootContentPanel();
}

