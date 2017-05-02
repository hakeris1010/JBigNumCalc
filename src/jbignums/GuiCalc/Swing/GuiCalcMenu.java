package jbignums.GuiCalc.Swing;

import jbignums.CalcProperties.AsyncCalcWorker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

public class GuiCalcMenu extends GUIMenu {
    // Event commands.
    public static final class Commands{
        public static final String LayoutChanged = "GuiMenu_LayoutChanged";
        public static final String ExitRequested = "GuiMenu_ExitRequested";
    }

    private AsyncCalcWorker state;
    private List<ActionListener> listeners = Collections.synchronizedList( new ArrayList<>() );

    // Dynamically construct the new menu
    public GuiCalcMenu(){ }
    public GuiCalcMenu(AsyncCalcWorker state) {
        create(state);
    }

    private void fireEventToListeners(ActionEvent event){
        for(int i = 0; i<listeners.size(); i++){
            listeners.get(i).actionPerformed( event );
        }
    }

    /**
     * Adds a new ActionListener to this menu.
     * The listeners can listen for events defined in the Commands class.
     * - In current menu case, it's Layout Changed events.
     * @param list - listener object.
     */
    public void addActionListener(ActionListener list){
        listeners.add(list);
    }

    @Override
    public void create(AsyncCalcWorker guiState){
        state = guiState;

        // As this method must be called on EDT, we create Swing menu objects here.
        mubar = new JMenuBar();

        // Create root menus, and add them to a list.
        JMenu fileMenu = DefaultMenus.File.getJMenu();
        JMenu viewMenu = DefaultMenus.View.getJMenu();
        JMenu editMenu = DefaultMenus.Edit.getJMenu();
        JMenu helpMenu = DefaultMenus.Help.getJMenu();

        rootMenus.add( fileMenu );
        rootMenus.add( viewMenu );
        rootMenus.add( editMenu );
        rootMenus.add( helpMenu );

        // Event listeners
        ActionListener actl = (e) -> {
            switch(e.getActionCommand())
            {
                case "Edit_History":
                    JOptionPane.showMessageDialog(null, /*calcState.getQueryHistory()*/ "Nyaa", "History", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "Help_Help":
                    JOptionPane.showMessageDialog(null, SwingGuiStarter.getHelpMessage(), "Help", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "Help_About":
                    JOptionPane.showMessageDialog(null, SwingGuiStarter.getAboutMessage(), "About", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "File_Exit":
                    // Exit button pressed. Send the ExitRequested event.
                    fireEventToListeners( new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Commands.ExitRequested) );
                    break;
            }
        };

        ActionListener layoutListener = (e) -> {
            for(GuiCalcProps.CalcLayout lay : GuiCalcProps.CalcLayout.values()){
                if( e.getActionCommand().equals( "View_Layout:"+lay.getName() ) ){
                    // If command is this, it means this layout has been selected. Fire an event symbolizing this.
                    fireEventToListeners( new ActionEvent( lay, ActionEvent.ACTION_PERFORMED, Commands.LayoutChanged ) );
                }
            }
        };

        //=============== View Menu. ===============//
        // Add the Exit button.
        JMenuItem menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
        menuItem.setActionCommand("File_Exit");
        menuItem.addActionListener(actl);
        fileMenu.add(menuItem);

        //=============== View Menu. ===============//
        // Add all options for layouts.
        ButtonGroup group = new ButtonGroup();

        for (GuiCalcProps.CalcLayout lay : GuiCalcProps.CalcLayout.values()) {
            JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(lay.getName()+"Layout");
            rbMenuItem.setSelected( lay.isDefault() ); // DEFAULT
            //rbMenuItem.setMnemonic(KeyEvent.VK_N);
            //rbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
            rbMenuItem.setActionCommand( "View_Layout:"+lay.getName() );
            //Set the Listener - it will setup the gui through State.
            rbMenuItem.addActionListener( layoutListener );

            group.add(rbMenuItem);
            viewMenu.add(rbMenuItem);
        }

        //=============== Edit Menu. ===============//
        // History view
        menuItem = new JMenuItem("View History", KeyEvent.VK_H);
        menuItem.setActionCommand("Edit_History");
        menuItem.addActionListener(actl);
        editMenu.add(menuItem);

        //=============== Help Menu. ===============//
        menuItem = new JMenuItem("View Help", KeyEvent.VK_E);
        menuItem.setActionCommand("Help_Help");
        menuItem.addActionListener(actl);
        helpMenu.add(menuItem);

        menuItem = new JMenuItem("About");
        menuItem.setActionCommand("Help_About");
        menuItem.addActionListener(actl);
        helpMenu.add(menuItem);

        // ------- Add all Menus in the list to the MenuBar ------- //
        addAllRootMenusToMubar();
    }
}

