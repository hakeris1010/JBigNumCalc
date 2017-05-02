package jbignums.GuiCalc.Swing;

import jbignums.GuiCalc.Swing.GuiDesigns.GUIDesignLayout;
import jbignums.GuiCalc.Swing.GuiCalcMenu;
import jbignums.GuiCalc.Swing.GuiDesigns.JGUI_SimpleCalculator;

public final class GuiCalcProps {
    /**
     *  All the general GUI defaults.
     */
    public static final class Defaults {
        // Default Menu Bar (Controlling Other General GUI elements)
        static public final Class menuBar = GuiCalcMenu.class;
        // Default General GUI elements
        // The Design Layout
        static public final CalcLayout GuiLayout = CalcLayout.NORMAL;
        // Input Methods
        static public final boolean TypedInput = false;

        // Other defaults are dependent to their GUILayouts.
    }

    public enum CalcLayout {
        NORMAL ("Normal", true, JGUI_SimpleCalculator.class),
        SCIENTIFIC ("Scientific", false, JGUI_SimpleCalculator.class);

        // Private parts
        private final String name;
        private Class design;
        private boolean isdef;

        CalcLayout(String lname, boolean isDefault, Class designClass){
            // Check if passed class is a subclass of GUIDesignLayout.
            if( ! GUIDesignLayout.class.isAssignableFrom(designClass) )
                throw new RuntimeException("Layout class is now implementing a GUIDesignLayout interface");

            name = lname;
            design = designClass;
            isdef = isDefault;
        }
        public String getName(){ return name; }
        public Class getDesign(){ return design; }
        public boolean isDefault(){ return isdef; }
    }

}
