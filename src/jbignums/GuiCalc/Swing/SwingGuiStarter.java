/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jbignums.GuiCalc.Swing;

import jbignums.CalcProperties.GuiCalcState;
import jbignums.GuiCalc.GUICalculator;

import java.awt.event.*;
import javax.swing.*;

/**
 * The Model v0.5 specs:
 * - GUIStarter is the Main class. It takes care of creating GUI layout, MenuBars, and all the event listeners.
 * - The Calculator code is totally separated from the GUI.
 * - when GUI gets a calculation query ready, it's passed to calculators. All movement is done through evt.listeners.
 *
 * - Every Framework has it's own GUIStarter main class.
 *
 * Swing Framework properties:
 * - Many Layouts, One Menu.
 * - Layout is a Component.
 * - GUIStarter takes care of upper control (changing layouts, getting queries and sending results).
 */

public class SwingGuiStarter {
    public static final String VERSION = "v0.1";

    private JFrame frame;
    static private int frameCount  = 0;
    private GuiCalcState calcState;

    // Current layout and Menu
    private GUICalculatorLayout currentLayout;
    private GUIMenu currentMenu;

    public SwingGuiStarter(boolean createGui) {
        if (createGui) {
            setupAndShowGui();
            frameCount++;
        }
    }

    /**
     * Must be called from EDT
     */
    private void setNewGuiLayout(Class layoutClass){
        if( ! GUICalculatorLayout.class.isAssignableFrom(layoutClass) ) // If this is wrong class
            throw new RuntimeException("Wrong Layout class passed.");

        GUICalculatorLayout newLayout;
        try {
            newLayout = (GUICalculatorLayout) ( layoutClass.newInstance() );
            newLayout.create(calcState);

        } catch (Exception e) {
            System.out.println("Can't get Gui Layout!");
            e.printStackTrace();
            throw new RuntimeException("Error : " + e.toString());
        }

        // Add listeners to the 'current' layout
        newLayout.addActionListener((ActionListener) (e) -> {
            switch (e.getActionCommand()){
                case GUICalculator.Commands.CALC_EXIT_REQUESTED:
                    // Exit button pressed on layout - post quit message to frame.
                    // The exit event will be processed on frame's listener.
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    break;
                case GUICalculator.Commands.CALC_QUERY_ENTERED:
                    // Calculation query has been entered. Pass it to GuiCalcState for processing.
                    System.out.println("Calculation entered.");
                    break;
            }
        });

        // Set as current
        currentLayout = newLayout;

        // Remove all components from previous layout's ContentPane, and reset Swing.
        this.frame.getContentPane().removeAll();
        // Now set the new layout pane
        this.frame.setContentPane(newLayout.getRootContentPanel());
        this.frame.getContentPane().revalidate();
        this.frame.getContentPane().repaint();
    }

    /**
     * Sets up all the SWING gui. Must be called on EdT.
     */
    public void setupAndShowGui() {
        // Setup GUI. Everything must be done on EDT thread, so use "invokeLater"

        SwingUtilities.invokeLater( () -> {
            //==============================================================================//
            // Create a Frame and GuiCalcState.
            frame = new JFrame();
            calcState = new GuiCalcState();

            // Set the default Look and Feel for this layout.
            try{
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(Exception e){
                throw new RuntimeException("Can't set up Look And Feel!!!");
            }

            // Firstly, initialize the essential fields and layouts.
            //==============================================================================//
            // Initialize MenuBar
            try {
                currentMenu = (GuiCalcMenu)( GuiCalcProps.Defaults.menuBar.newInstance() );
                currentMenu.create(calcState);
            } catch (Exception e) {
                System.out.println("Can't get Gui Layout!");
                e.printStackTrace();
                return;
            }

            currentMenu.addActionListener((e) -> {
                if(e.getActionCommand() == GuiCalcMenu.Commands.LayoutChanged){
                    if(GuiCalcProps.CalcLayout.class.isAssignableFrom( e.getSource().getClass() )){
                        GuiCalcProps.CalcLayout layt = (GuiCalcProps.CalcLayout)(e.getSource());
                        System.out.println("Received \"Gui layout changed\" event. The new GUI:"+layt.getName());

                        // Firstly, remove the old layout's menu items from the GUIMenu
                        for(GUIMenu.MenuItem mi : currentLayout.getMenuItems()){
                            JComponent jk = mi.getJComponent();
                            if(jk != null){
                                jk.getParent().remove(jk);
                            }
                        }

                        // Add this layout as an active one
                        setNewGuiLayout(layt.getDesign());

                        // Now add the new layout's menu items to the GuiMenu
                        for(GUIMenu.MenuItem mi : currentLayout.getMenuItems()){
                            currentMenu.addMenuItem(mi);
                        }

                        System.out.println("Layout successfully changed!");
                    }
                }
            });

            frame.setJMenuBar(currentMenu.getMenuBar());

            //==============================================================================//
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
                        System.out.println("GUI Exit message posted. Disposing the frame...");

                        // Signal on state that everything has to be terminated, and wait.
                        calcState.waitForTasksEnd(true);

                        // Dispose da frame after state has exitted.
                        if(frameCount>0) frameCount--;
                        frame.dispose();
                    }
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
        return "SwingGuiStarter "+ SwingGuiStarter.VERSION+" Made by GrylloTron.";
    }

    /** ===========================================================
     * Main function.
     * @param args
     */

    public static void main(String[] args)
    {
        System.out.println("STARTING GUICALC!!!");
        new SwingGuiStarter(true);
    }
}

