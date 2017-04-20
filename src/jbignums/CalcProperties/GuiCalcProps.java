package jbignums.CalcProperties;

import jbignums.CalcDesigns.GUIDesignLayout;
import jbignums.CalcDesigns.GuiCalcMenu;
import jbignums.CalcDesigns.JGUI_SimpleCalculator;

public class GuiCalcProps {
    /**
     *  All the general GUI defaults.
     */
    static class Defaults {
        // Default Menu Bar (Controlling Other General GUI elements)
        static public final Class menuBar = GuiCalcMenu.class;

        // Default General GUI elements
        // The Design Layout
        static public final CalcMode GuiLayout = CalcMode.NORMAL;
        // Input Methods
        static public final boolean TypedInput = false;

        // Other defaults are dependent to their GUILayouts.
    }

    enum CalcMode {
        NORMAL (300, 400, JGUI_SimpleCalculator.class),
        SCIENTIFIC (600, 400, JGUI_SimpleCalculator.class);

        // Private parts
        private final int mWidth;
        private final int mHeight;
        private Class design;

        CalcMode(int w, int h, Class designClass){
            // Check if passed class is a subclass of GUIDesignLayout.
            if( ! GUIDesignLayout.class.isAssignableFrom(designClass) )
                throw new RuntimeException("Layout class is now implementing a GUIDesignLayout interface");

            mWidth = w;
            mHeight = h;
            design = designClass;
        }
        public int width(){ return mWidth; }
        public int height(){ return mHeight; }
        public Class getDesign(){ return design; }
    }
}
