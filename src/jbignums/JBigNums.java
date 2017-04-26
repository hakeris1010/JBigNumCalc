/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jbignums;

import jbignums.GuiCalc.GuiCalc;

/**
 *
 * @author Kestutis
 */

public class JBigNums {
    public static final String VERSION = "v0.1";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        System.out.println("STARTING GUICALC!!!");
        new GuiCalc(true);
    }
}
