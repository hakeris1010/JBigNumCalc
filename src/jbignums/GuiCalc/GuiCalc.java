/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jbignums.GuiCalc;

import jbignums.CalcDesigns.GUIDesignLayout;
import jbignums.CalcDesigns.GUIMenu;
import jbignums.CalcProperties.GuiCalcProps;
import jbignums.CalcProperties.GuiCalcState;
import jbignums.JBigNums;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author Kestutis
 */

public class GuiCalc {
    public static final String VERSION = "v0.1";

    private JFrame frame;
    static private int frameCount  = 0;

    public GuiCalc(){
        setupAndShowGui();
        frameCount++;
    }

    public void setupAndShowGui(){
        // Setup GUI. Everything must be done on EDT thread, so use "invokeLater"

        SwingUtilities.invokeLater( () -> {
            // Set the default Look and Feel for this layout.
            try{
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(Exception e){
                throw new RuntimeException("Can't set up Look And Feel!!!");
            }

            frame.setTitle("JBigNums Calculator");
            frame.setLocation(100+frameCount*20, 100+frameCount*20);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            //setSize(mode.width(), mode.height());
            //this.setResizable(false);

            // Set layout manager.
            GUIDesignLayout newLayout = null;
            try {
                newLayout = (GUIDesignLayout)( GuiCalcProps.Defaults.GuiLayout.getDesign().newInstance() );
            } catch (Exception e) {
                System.out.println("Can't get Gui Layout!");
                e.printStackTrace();
                return;
            }

            frame.setContentPane( newLayout.getRootContentPanel() );

            // Show confirm message on X press.
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    if (JOptionPane.showConfirmDialog(frame ,
                        "Are you sure to close this window?", "Really Closing?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                    {
                        if(frameCount>0) frameCount--;
                        frame.dispose();
                    }
                }
            });

            GUIMenu newMenu = null;
            try {
                newMenu = (GUIMenu)( GuiCalcProps.Defaults.menuBar.newInstance() );
            } catch (Exception e) {
                System.out.println("Can't get Gui Layout!");
                e.printStackTrace();
                return;
            }
            frame.setJMenuBar(newMenu.getMenuBar());

            frame.pack();
        } );
    }

    public static String getHelpMessage()
    {
        return "Use calculator by pressing buttons or typing in query field.";
    }

    public static String getAboutMessage()
    {
        return "GuiCalc "+GuiCalc.VERSION+". JBigNums "+JBigNums.VERSION+". Made by GrylloTron.";
    }
}

