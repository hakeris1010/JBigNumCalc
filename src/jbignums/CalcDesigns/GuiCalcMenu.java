package jbignums.CalcDesigns;

import jbignums.CalcProperties.GuiCalcProps;
import jbignums.CalcProperties.GuiCalcState;
import jbignums.GuiCalc.GuiCalc;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 *  A GuiMenu class
 *  Working principle:
 *  - We pass a State param to it, and when Swing GUI event occurs (e.g. menu itam selected),
 *    event listener modifies the State (which is concurrent), and State's inner methods fire events on BackEnd
 *    thread, setting up System Options or Launching Jobs.
 *
 *  - TODO:
 *      Don't stick with only 1 Menu class implementing everything:
 *      Create this design:
 *      - All interested GUI Components (e.g. GUIDesignLayout's) define their own list of GUIMenu.MenuItems,
 *          all of them WITH THEIR OWN registered EventListeners. This way Event Handling code can be related only
 *          to the items interested.
 */

public class GuiCalcMenu implements GUIMenu {
    JMenuBar mubar;
    ArrayList<JMenu> menus;
    ActionListener actl;

    // Dynamically construct the new menu
    public GuiCalcMenu(GuiCalcState state)
    {
        actl = new GuiCalcMenuBarListener(state);
        populateMenuBar();
    }

    public JMenuBar getMenuBar() {
        return mubar;
    }

    public void addMenuItem(MenuItem item)
    {
        // Search for "Name" in JMenus list, and add the item to the appropriate menu.
    }

    @Deprecated
    private void populateMenuBar()
    {
        // View Menu.
        JMenu menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        mubar.add(menu);
        menus.add(menu);

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
        menus.add(menu);

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
        menus.add(menu);

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

// These listeners must be invoked on the EDT Thread.
@Deprecated
class GuiCalcMenuBarListener implements ActionListener
{
    private GuiCalcState calcState;
    
    GuiCalcMenuBarListener(GuiCalcState state){
        calcState = state;
    }
    
    // Default Action Listen0r
    @Override
    public void actionPerformed(ActionEvent e)
    {
        switch(e.getActionCommand())
        {
            case "View_NormalMode":
                JOptionPane.showMessageDialog(null, "Normal Mode selected.", "InfoBox", JOptionPane.INFORMATION_MESSAGE);
                calcState.setCalcMode(GuiCalcProps.CalcLayout.NORMAL);
                break;
            case "View_ScientificMode":
                JOptionPane.showMessageDialog(null, "Scientific Mode selected.", "InfoBox", JOptionPane.INFORMATION_MESSAGE);
                calcState.setCalcMode(GuiCalcProps.CalcLayout.SCIENTIFIC);
                break;
            case "View_TypedInput":
                //calcState.canTypeInQuery.set(true);
                break;
            case "View_Conversion":
                //calcState.isConversionMode.set(true);
                break;
            case "Edit_Copy":
                try{
                    StringSelection stringSelection = new StringSelection(/*calcState.getQuery()*/ "Nyaa");
                    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clpbrd.setContents(stringSelection, null);
                } catch (Exception ex){
                    JOptionPane.showMessageDialog(null, "Can't put to clipboard.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case "Edit_Paste":
                /*clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable tr = clpbrd.getContents(null);*/
                break;
            case "Edit_History":
                JOptionPane.showMessageDialog(null, /*calcState.getQueryHistory()*/ "Nyaa", "History", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Help_Help":
                JOptionPane.showMessageDialog(null, GuiCalc.getHelpMessage(), "Help", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Help_About":
                JOptionPane.showMessageDialog(null, GuiCalc.getAboutMessage(), "About", JOptionPane.INFORMATION_MESSAGE);
                break;
        }
    }
}
