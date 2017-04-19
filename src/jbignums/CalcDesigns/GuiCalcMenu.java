package jbignums.CalcDesigns;

import jbignums.GuiCalc.GuiCalc;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class GuiCalcMenu implements SwingMenu {
    JMenuBar mubar;
    ActionListener actl;

    public JMenuBar getMenuBar() {
        return mubar;
    }
    
    // Dynamically construct the new menu
    public GuiCalcMenu(ActionListener actList)
    {
        actl = actList;
        populateMenuBar();
    }

    private void populateMenuBar()
    {
        // View Menu.
        JMenu menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        mubar.add(menu);

        //Modes
        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("Normal Mode");
        rbMenuItem.setSelected( true ); // DEFAULT
        rbMenuItem.setMnemonic(KeyEvent.VK_N);
        rbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
        rbMenuItem.setActionCommand("MenuBar_View_NormalMode");
        rbMenuItem.addActionListener(actl);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Scientific Mode");
        rbMenuItem.setSelected( false );
        rbMenuItem.setMnemonic(KeyEvent.VK_S);
        rbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
        rbMenuItem.setActionCommand("MenuBar_View_ScientificMode");
        rbMenuItem.addActionListener(actl);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        //a group of check box menu items
        menu.addSeparator();

        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Typed Input");
        cbMenuItem.setSelected( false ); // DEFAULT
        cbMenuItem.setMnemonic(KeyEvent.VK_T);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
        cbMenuItem.setActionCommand("View_TypedInput");
        cbMenuItem.addActionListener(actl);
        menu.add(cbMenuItem);

        // Other modes (Conversion, etc)
        menu.addSeparator();

        cbMenuItem = new JCheckBoxMenuItem("Conversion&Stuff");
        cbMenuItem.setSelected( false );
        cbMenuItem.setMnemonic(KeyEvent.VK_O);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        cbMenuItem.setActionCommand("View_Conversion");
        cbMenuItem.addActionListener(actl);
        menu.add(cbMenuItem);

        // Edit menu
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        mubar.add(menu);

        JMenuItem menuItem = new JMenuItem("Copy");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.setActionCommand("Edit_Copy");
        menuItem.addActionListener(actl);
        menu.add(menuItem);

        menuItem = new JMenuItem("Paste");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.setActionCommand("Edit_Paste");
        menuItem.addActionListener(actl);
        menu.add(menuItem);

        menuItem = new JMenuItem("View History", KeyEvent.VK_H);
        menuItem.setActionCommand("Edit_History");
        menuItem.addActionListener(actl);
        menu.add(menuItem);

        // Help Menu
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        mubar.add(menu);

        menuItem = new JMenuItem("View Help", KeyEvent.VK_E);
        menuItem.setActionCommand("Help_Help");
        menuItem.addActionListener(actl);
        menu.add(menuItem);

        menuItem = new JMenuItem("About");
        menuItem.setActionCommand("Help_About");
        menuItem.addActionListener(actl);
        menu.add(menuItem);
    }
}
