package jbignums.GuiDesigns;

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
    ArrayList<JMenu> rootMenus = new ArrayList<>();
    ActionListener actl;

    // Dynamically construct the new menu
    public GuiCalcMenu(){ }
    public GuiCalcMenu(GuiCalcState state) {
        create(state);
    }

    @Override
    public void create(GuiState state){
        // As this method must be called on EDT, we create Swing menu objects here.
        mubar = new JMenuBar();
        // Create a listener and populate stuff.
        actl = new GuiCalcMenuBarListener(state);
        populateMenuBar();
    }

    @Override
    public JMenuBar getMenuBar() {
        return mubar;
    }

    public void addMenuItem(MenuItem item) {
        // Search for "Name" in JMenus list, and add the item to the appropriate menu.
        JMenu rmen = null;
        for(JMenu mn : rootMenus) {
            if ( item.getParentMenuName().equals(mn.getText()) ) { // E.g. we want a "Edit" menu, name is "Edit"
                rmen = mn;
                break;
            }
        }
        // No menu with that name. Create one.
        if(rmen == null) {
            rmen = new JMenu(item.getParentMenuName());
            // TODO: rmen.setMnemonic();
            rootMenus.add(rmen);
            mubar.add(rmen);
        }

        if(item.getMenuItem() != null) { // If item is specified, add it.
            rmen.add(item.getMenuItem());
        }
        if(item.isSeparatorAfter()){  // If separator, add it.
            rmen.addSeparator();
        }
    }

    // Private methods ================================//
    private void addAllRootMenusToMubar(){
        for(JMenu m : rootMenus){
            mubar.add(m);
        }
    }

    private void populateMenuBar()
    {
        //=============== View Menu. ===============//
        JMenu menu = DefaultMenus.View.getJMenu();
        rootMenus.add( menu );

        // Layouts
        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("Normal Mode");
        rbMenuItem.setSelected( true ); // DEFAULT
        rbMenuItem.setMnemonic(KeyEvent.VK_N);
        rbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
        rbMenuItem.setActionCommand("View_NormalMode");
        rbMenuItem.addActionListener(actl);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Scientific Mode");
        rbMenuItem.setSelected( false );
        rbMenuItem.setMnemonic(KeyEvent.VK_S);
        rbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
        rbMenuItem.setActionCommand("View_ScientificMode");
        rbMenuItem.addActionListener(actl);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        //=============== Edit Menu. ===============//
        menu = DefaultMenus.Edit.getJMenu();
        rootMenus.add(menu);

        // History view
        JMenuItem menuItem = new JMenuItem("View History", KeyEvent.VK_H);
        menuItem.setActionCommand("Edit_History");
        menuItem.addActionListener(actl);
        menu.add(menuItem);

        //=============== Help Menu. ===============//
        menu = DefaultMenus.Help.getJMenu();
        rootMenus.add(menu);

        menuItem = new JMenuItem("View Help", KeyEvent.VK_E);
        menuItem.setActionCommand("Help_Help");
        menuItem.addActionListener(actl);
        menu.add(menuItem);

        menuItem = new JMenuItem("About");
        menuItem.setActionCommand("Help_About");
        menuItem.addActionListener(actl);
        menu.add(menuItem);

        // ------- Add all Menus in the list to the MenuBar ------- //
        addAllRootMenusToMubar();
    }
}

// These listeners must be invoked on the EDT Thread.
class GuiCalcMenuBarListener implements ActionListener
{
    private GuiState calcState;
    
    GuiCalcMenuBarListener(GuiState state){
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
                calcState.setGuiDesignLayout(GuiCalcProps.CalcLayout.NORMAL.getDesign());
                break;
            case "View_ScientificMode":
                JOptionPane.showMessageDialog(null, "Scientific Mode selected.", "InfoBox", JOptionPane.INFORMATION_MESSAGE);
                calcState.setGuiDesignLayout(GuiCalcProps.CalcLayout.SCIENTIFIC.getDesign());
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
