/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jbignums.GuiCalc;

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

