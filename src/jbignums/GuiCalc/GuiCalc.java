/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jbignums.GuiCalc;

import jbignums.CalcDesigns.GuiCalcMenu;
import jbignums.CalcDesigns.JGUI_SimpleCalculator;
import jbignums.CalcDesigns.SwingGUIDesign;
import jbignums.JBigNums;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

/**
 *
 * @author Kestutis
 */

class DefaultGUICalc {
    static public final CalcMode calcMode = CalcMode.NORMAL;
    static public final Class menuBar = GuiCalcMenu.class;
}

class GuiCalcState //Thread-safe.
{
    public CalcModeState modeState;

    private String query;
    private ArrayList<String> queryHistory;

    // Getters, setters

    public synchronized String getQuery(){ return query; }
    public synchronized void setQuery(String qu){ query = qu; }

    public synchronized ArrayList<String> getQueryHistory(){ return queryHistory; }
    public synchronized void setQueryHistory(ArrayList<String> qu){ queryHistory = qu; }
}

class CalcModeState
{
    private CalcMode currentMode;

}

enum CalcMode {
    NORMAL (300, 400, JGUI_SimpleCalculator.class),
    SCIENTIFIC (600, 400, JGUI_SimpleCalculator.class);

    // Private parts
    private final int mWidth;
    private final int mHeight;
    private Class design;

    CalcMode(int w, int h, Class designClass){
        if(designClass != SwingGUIDesign.class)
            throw new RuntimeException("Layout class is now implementing a SwingGUIDesign interface");

        mWidth = w;
        mHeight = h;
        design = designClass;
    }
    public int width(){ return mWidth; }
    public int height(){ return mHeight; }
    public Class getDesign(){ return design; }
}

public class GuiCalc extends JFrame{
    public static final String VERSION = "v0.1";

    private static int frameCount=0;
    private final GuiCalcState cs = new GuiCalcState();

    private void setTextPane()
    {
        // Create a test text field with border.
        JTextField queryField = new JTextField();
        queryField.setText("Nyaa, kawaii desu~~");
        //queryField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        queryField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        queryField.setHorizontalAlignment(JTextField.RIGHT);
        queryField.setEditable(false);

        JTextField resultField = new JTextField();
        resultField.setText("Result");
        //resultField.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        resultField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        resultField.setHorizontalAlignment(JTextField.RIGHT);
        resultField.setEditable(false);

        // Set our panes.
        JPanel controlPane = new JPanel(new BorderLayout());
        controlPane.add(queryField, BorderLayout.NORTH);
        controlPane.add(resultField, BorderLayout.SOUTH);

        this.add(controlPane, BorderLayout.NORTH);
    }

    private void setButtonPane()
    {

    }

    private void setExtendedPane()
    {

    }

    public GuiCalc(){
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e){
            throw new RuntimeException("Can't set up Look And Feel!!!");
        }

        setTitle("JBigNums Calculator");
        setLocation(100+frameCount*20, 100+frameCount*20);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //setSize(mode.width(), mode.height());
        //this.setResizable(false);

        // Set layout manager.
        getContentPane().setLayout(new BorderLayout());

        // Show confirm message on X press.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(GuiCalc.this ,
                    "Are you sure to close this window?", "Really Closing?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                {
                    if(frameCount>0) frameCount--;
                    GuiCalc.this.dispose();
                }
            }
        });

        setJMenuBar(new GuiCalcMenuBar(this).getMenuBar());

        setTextPane();
        setButtonPane();
        setExtendedPane();

        ImageIcon icon = new ImageIcon(GuiCalc.class.getResource("/kawaii2.png")); //Our kawaii girl

        JLabel label = new JLabel(icon);
        JPanel kawaiiPanel = new JPanel();
        kawaiiPanel.add(label);

        kawaiiPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Image newimg = icon.getImage().getScaledInstance(kawaiiPanel.getWidth(), kawaiiPanel.getHeight(), java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
                label.setIcon(new ImageIcon(newimg));  // transform it back
            }
        });

        add(kawaiiPanel, BorderLayout.CENTER);

        pack();

        frameCount++;
    }

    public GuiCalcState getCalcState(){
        return cs;
    }

    public void updateView()
    {

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

class GuiCalcMenuBarListener implements ActionListener
{
    private final GuiCalc ginst;

    public GuiCalcMenuBarListener(GuiCalc theInst)
    {
        ginst = theInst;
    }

    // Default Action Listen0r
    @Override
    public void actionPerformed(ActionEvent e)
    {
        switch(e.getActionCommand())
        {
            case "View_NormalMode":
                JOptionPane.showMessageDialog(null, "Normal Mode selected.", "InfoBox", JOptionPane.INFORMATION_MESSAGE);
                ginst.getCalcState().setCalcMode(CalcMode.NORMAL);
                ginst.updateView();
                break;
            case "View_ScientificMode":
                JOptionPane.showMessageDialog(null, "Scientific Mode selected.", "InfoBox", JOptionPane.INFORMATION_MESSAGE);
                ginst.getCalcState().setCalcMode(CalcMode.SCIENTIFIC);
                ginst.updateView();
                break;
            case "View_TypedInput":
                ginst.getCalcState().canTypeInQuery.set(true);
                ginst.updateView();
                break;
            case "View_Conversion":
                ginst.getCalcState().isConversionMode.set(true);
                ginst.updateView();
                break;
            case "Edit_Copy":
                try{
                    StringSelection stringSelection = new StringSelection(ginst.getCalcState().getQuery());
                    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clpbrd.setContents(stringSelection, null);
                } catch (Exception ex){ 
                    JOptionPane.showMessageDialog(null, "Can't put to clipboard.", "Error", JOptionPane.ERROR_MESSAGE); 
                }
                break;
            case "Edit_Paste":
                /*clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable tr = clpbrd.getContents(null);*/
                break;
            case "Edit_History":
                JOptionPane.showMessageDialog(null, ginst.getCalcState().getQueryHistory(), "History", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Help_Help":
                JOptionPane.showMessageDialog(null, GuiCalc.getHelpMessage(), "Help", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Help_About":
                JOptionPane.showMessageDialog(null, GuiCalc.getAboutMessage(), "About", JOptionPane.INFORMATION_MESSAGE);
                break;
        }
    }
}
