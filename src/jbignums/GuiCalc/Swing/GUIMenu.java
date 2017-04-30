package jbignums.GuiCalc.Swing;

import jbignums.CalcProperties.GuiCalcState;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 *  Basic interface for JMenuBar designs
 */
public abstract class GUIMenu {
    /**
     * Basic class defining the GuiMenuItemGroup
     */
     public static final class MenuItem{
        private JComponent menuItem;
        private String menuName;
        boolean createMnem;
        boolean createAccel;

        public MenuItem(String parentMenuName, JComponent itemToAdd, boolean createMnemonic, boolean createAccelerator){
            menuItem = itemToAdd;
            menuName = parentMenuName;
            createMnem = createMnemonic;
            createAccel = createAccelerator;
        }

        public JComponent getJComponent(){ return menuItem; }
        public String getParentMenuName(){ return menuName; }
        public boolean isCreateMnemonic(){ return createMnem; }
        public boolean isCreateAccelerator(){ return createAccel; }
    }

    /**
     * Default JMenus to be included
     */
    public enum DefaultMenus{
        File(1, "File", KeyEvent.VK_F),
        Edit(2, "Edit", KeyEvent.VK_E),
        View(3, "View", KeyEvent.VK_V),
        Help(4, "Help", KeyEvent.VK_H);

        private int priority;
        private int mnemonic;
        private String name;

        DefaultMenus(int priorit, String namee, int mnemon){
            priority = priorit;
            mnemonic = mnemon;
            name = namee;
        }
        public int getPriority(){ return priority; }
        public int getMnemonic(){ return mnemonic; }
        public String getName() { return name; }

        public JMenu getJMenu(){
            JMenu newMenu = new JMenu(name);
            newMenu.setMnemonic(mnemonic);
            return newMenu;
        }
    }
    /**
     * Private fields: root JMenuBar, and items.
     */
    protected JMenuBar mubar;
    protected ArrayList<JMenu> rootMenus = new ArrayList<>();

    /**
     * Private helper methods
     */
    protected void addAllRootMenusToMubar(){
        for(JMenu m : rootMenus){
            mubar.add(m);
        }
    }

    /**
     * Public shared menu methods
     * @return - returns the Swing Root MenuBar.
     */
    public JMenuBar getMenuBar(){
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
            DefaultMenus defmen = null;
            for(DefaultMenus mn : DefaultMenus.values()){
                if( item.getParentMenuName().equals( mn.getName() ) ){
                    defmen = mn;
                    break; // We found a default menu with that name, so can quit.
                }
            }
            if(defmen != null){
                rmen = defmen.getJMenu();
            }
            else {
                rmen = new JMenu(item.getParentMenuName());
                // TODO: rmen.setMnemonic();
                rootMenus.add(rmen);
                mubar.add(rmen);
            }
        }
        // Add item to the created (or existing) menu.
        if(item.getJComponent() != null) { // If item is specified, add it.
            rmen.add(item.getJComponent());
        }
        /*if(item.isSeparatorAfter()){  // If separator, add it.
            rmen.addSeparator();
        }*/
    }

     /**
     * Creation function. This is overridable - allows different menus to work.
     * @param state - GUIState object
     */
    public abstract void create(GuiCalcState state);

    /**
     * Adds an active action listenr
     * @param list
     */
    public abstract void addActionListener(ActionListener list);
}
