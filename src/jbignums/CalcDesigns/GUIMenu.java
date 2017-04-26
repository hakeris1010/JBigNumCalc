package jbignums.CalcDesigns;

import jbignums.CalcProperties.GuiCalcState;

import javax.swing.*;
import java.awt.event.KeyEvent;
/**
 *  Basic interface for JMenuBar designs
 */
public interface GUIMenu {
    /**
     * Basic class defining the GuiMenuItemGroup
     */
     class MenuItem{
        private JMenuItem menuItem;
        private String menuName;
        private boolean separatorAfter;

        public MenuItem(String parentMenuName, JMenuItem itemToAdd, boolean addSeparatorAfter){
            menuItem = itemToAdd;
            menuName = parentMenuName;
            separatorAfter = addSeparatorAfter;
        }

        public JMenuItem getMenuItem(){ return menuItem; }
        public String getParentMenuName(){ return menuName; }
        public boolean isSeparatorAfter(){ return separatorAfter; }
    }

    /**
     * Default JMenus to be included
     */
    enum DefaultMenus{
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
        public JMenu getJMenu(){
            JMenu newMenu = new JMenu(name);
            newMenu.setMnemonic(mnemonic);
            return newMenu;
        }
    }

    // Functions to-be implemented.
    void create(GuiCalcState state);
    JMenuBar getMenuBar();
    void addMenuItem(MenuItem item);
}
