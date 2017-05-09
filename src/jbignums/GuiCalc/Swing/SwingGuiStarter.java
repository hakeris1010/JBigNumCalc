/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jbignums.GuiCalc.Swing;

import jbignums.CalcProperties.AsyncCalcWorker;
import jbignums.CalculatorPlugin.AsyncQueueCalculatorPlugin;
import jbignums.CalculatorPlugin.StringCalculator.StringCalculator;
import jbignums.CalculatorPlugin.StringCalculator.StringExpressionParser;
import jbignums.GuiCalc.GUICalculator;

import java.awt.event.*;
import java.util.NoSuchElementException;
import java.util.Scanner;
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
    private AsyncCalcWorker calcState;
    private Thread controlThread;

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
    private void setNewGuiLayout(Class layoutClass, boolean addMenuItems){
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
                    // Calculation query has been entered. Pass it to AsyncCalcWorker for processing.
                    System.out.println("Calculation query entered.");
                    if(!AsyncQueueCalculatorPlugin.Query.class.isAssignableFrom( e.getSource().getClass() )){
                        throw new RuntimeException("Wrong class passed as a CALC_QUERY_ENTERED source argument.");
                    }
                    // get the query if it's valid.
                    AsyncQueueCalculatorPlugin.Query query = (AsyncQueueCalculatorPlugin.Query)(e.getSource());

                    // Create new task in the AsyncCalcWorker, and get ID.
                    // Currently Multi-GUI is not supported, so ID isn't used.
                    long taskId = calcState.launchNewCalculationTask(query);
                    if(taskId == 0){
                        System.out.print("Error on calculation task entering!");
                    }
                    break;
                //...
            }
        });

        // Set as current
        currentLayout = newLayout;

        if(addMenuItems) {
            for (GUIMenu.MenuItem mi : currentLayout.getMenuItems()) {
                currentMenu.addMenuItem(mi);
            }
        }

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
            /* Setup the asynchronous calculator worker, and start the Control Thread
             *
             * This thread will take care of getting results from the AsyncCalcWorker,
             * and sending them for displaying in the current GUICalculatorLayout.
             * BlockingQueue is used, so the thread blocks when there's no result pending.
             */
            calcState = new AsyncCalcWorker();
            controlThread = new Thread(() -> {
                while( !calcState.isShuttingDown() ) {
                    // Take the first pending result, and send it to active GUIs by it's ID.
                    AsyncQueueCalculatorPlugin.Result res = calcState.takeResultFromQueue();
                    System.out.println("[SwingGuiStarter control thread]: Taken Result! Result: " + res.getClass().getName());

                    // Currently, no Multi-GUI is implemented, so no ID processing needed.
                    // We can just pass the result to the active Layout.
                    if (res != null) {
                        currentLayout.sendCalculationResult(res);
                    }
                }
                // Shutdown condition is met at this point.
                System.out.print("[SwingGuiStarter control thread] Shutdown requested from calcState. Returning.\n");
            });
            controlThread.start();

            //==============================================================================//
            // Create a main JFrame.
            frame = new JFrame();

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
                // Perform task if Layout Changed in menu.
                switch (e.getActionCommand()) {
                    case GuiCalcMenu.Commands.LayoutChanged:
                        if (GuiCalcProps.CalcLayout.class.isAssignableFrom( e.getSource().getClass()) ) {
                            GuiCalcProps.CalcLayout layt = (GuiCalcProps.CalcLayout) (e.getSource());
                            System.out.println("Received \"Gui layout changed\" event. The new GUI:" + layt.getName());

                            // Firstly, remove the old layout's menu items from the GUIMenu
                            // TODO: Move the menu item removal to the old layout's dispose() function.
                            //       The GUILayout's abstract class will have dispose() method by default,
                            //       taking care of all the stuff.
                            for (GUIMenu.MenuItem mi : currentLayout.getMenuItems()) {
                                JComponent jk = mi.getJComponent();
                                if (jk != null) {
                                    jk.getParent().remove(jk);
                                }
                            }

                            // Add this layout as an active one, and add new menu items.
                            setNewGuiLayout(layt.getDesign(), true);

                            System.out.println("Layout successfully changed!");
                        }
                        break;

                    case GuiCalcMenu.Commands.ExitRequested:
                        // Exit requested from menu - dispatch exit event on frame.
                        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                        break;
                    //...
                }
            });

            frame.setJMenuBar(currentMenu.getMenuBar());

            //==============================================================================//
            // Initialize and Set Layout
            setNewGuiLayout( GuiCalcProps.Defaults.GuiLayout.getDesign(), true );

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
     * //@param args
     */

    public static void TestStuff(){
        Scanner scan = new Scanner("8 + 556.5 + 133.7 +  7.464e+80 :3");
        while(scan.hasNext()){
            if(scan.hasNextBigDecimal()){
                System.out.println("<Found a BigDecimal!> : "+scan.nextBigDecimal());
            }
            System.out.println( scan.next() );
        }
    }

    public static void main(String[] args)
    {
        System.out.println("STARTING GUICALC!!!");
        //new SwingGuiStarter(true);
        //TestStuff();
        //StringExpressionParser.Test_DEBUG1();
        StringCalculator.TestDEBUG1();
    }
}

