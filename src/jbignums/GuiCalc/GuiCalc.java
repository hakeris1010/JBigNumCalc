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
    GuiCalcState cs;

    public GuiCalc(boolean createGui) {
        if (createGui) {
            setupAndShowGui();
            frameCount++;
        }
    }

    /**
     * Must be called from EDT
     */
    private void setNewGuiLayout(Class layoutClass){
        if( ! GUIDesignLayout.class.isAssignableFrom(layoutClass) ) // If this is wrong class
            throw new RuntimeException("Wrong Layout class passed.");

        GUIDesignLayout newLayout;
        try {
            newLayout = (GUIDesignLayout) ( layoutClass.newInstance() );
            newLayout.create(cs);
        } catch (Exception e) {
            System.out.println("Can't get Gui Layout!");
            e.printStackTrace();
            throw new RuntimeException("Error : " + e.toString());
        }
        this.frame.setContentPane(newLayout.getRootContentPanel());
    }

    /**
     * Sets up all the SWING gui. Must be called on EdT.
     */
    public void setupAndShowGui() {
        // Setup GUI. Everything must be done on EDT thread, so use "invokeLater"

        SwingUtilities.invokeLater( () -> {
            // Create a Frame and GuiCalcState.
            frame = new JFrame();
            cs = new GuiCalcState();

            // Set the default Look and Feel for this layout.
            try{
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(Exception e){
                throw new RuntimeException("Can't set up Look And Feel!!!");
            }

            // Initialize MenuBar
            GUIMenu newMenu;
            try {
                newMenu = (GUIMenu)( GuiCalcProps.Defaults.menuBar.newInstance() );
                newMenu.create(cs);
                cs.setGuiMenu(newMenu);
            } catch (Exception e) {
                System.out.println("Can't get Gui Layout!");
                e.printStackTrace();
                return;
            }
            frame.setJMenuBar(newMenu.getMenuBar());                    // Set MenuBar

            // Initialize and Set Layout
            setNewGuiLayout( GuiCalcProps.Defaults.GuiLayout.getDesign() );

            // Set cosmetic things
            frame.setTitle("JBigNums Calculator");
            frame.setLocation(100+frameCount*20, 100+frameCount*20);
            //setSize(mode.width(), mode.height());
            //this.setResizable(false);

            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Set that no action on Close, but:
            frame.addWindowListener(new WindowAdapter() {               // Show confirm message on close,
                @Override                                               // and safely shutdown background tasks.
                public void windowClosing(WindowEvent windowEvent) {
                    if (JOptionPane.showConfirmDialog(frame ,
                        "Are you sure to close this window?", "Really Closing?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                    {
                        // Signal on state that everything has to be shutted down, and wait.
                        cs.waitForTasksEnd(true);

                        if(frameCount>0) frameCount--;
                        frame.dispose();
                    }
                }
            } );

            // Now add Event Listeners for our GUI custom events (Fired by State)
            cs.addEDTListener( (ActionEvent evt) ->{
                switch(evt.getActionCommand()){
                    case GuiCalcState.Commands.GUI_LAYOUT_CHANGED:
                        setNewGuiLayout( cs.getCalcLayout().getDesign() );
                    // TODO: Maybe some more.
                }
            } );

            // Get frame ready for showing.
            frame.pack();
            frame.setVisible(true);
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

