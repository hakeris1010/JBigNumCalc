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

class NyaaThread extends Thread
{
    private GuiCalc ga;
    
    @Override
    public void run()
    {
        GuiCalc nu = new GuiCalc();
        nu.setVisible(true);
    }
}

public class JBigNums {
    public static final String VERSION = "v0.1";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        NyaaThread t1 = new NyaaThread();
        //NyaaThread t2 = new NyaaThread();
        t1.start();
        //t2.start();
    }
}
