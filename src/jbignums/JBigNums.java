/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jbignums;

import jbignums.CalcDesigns.JGUI_SimpleCalculator;
import jbignums.CalcDesigns.JetBrainsGUIDesign;
import jbignums.GuiCalc.GuiCalc;

/**
 *
 * @author Kestutis
 */

class CalcManager
{
    // Encapsulated GUI calculator object
    private GuiCalc gcalc;
    // Default GUI Design Layout.
    private final JetBrainsGUIDesign defDesign = new JGUI_SimpleCalculator();
    
    public void start()
    {
        System.out.println("Nyaa");
    }
}

public class JBigNums {
    public static final String VERSION = "v0.1";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CalcManager newCalc = new CalcManager();
        newCalc.start();
    }
}
