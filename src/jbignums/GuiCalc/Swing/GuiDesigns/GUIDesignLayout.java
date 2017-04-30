package jbignums.GuiCalc.Swing.GuiDesigns;

import jbignums.CalcProperties.GuiCalcState;
import jbignums.GuiCalc.Swing.GUIMenu;

import javax.swing.*;
import java.util.List;

/**
 *  Basic interface for all Gui Designs
 */
public interface GUIDesignLayout {
    void create(GuiCalcState state);
    JPanel getRootContentPanel();

    List<GUIMenu.MenuItem> getMenuItems();
}

