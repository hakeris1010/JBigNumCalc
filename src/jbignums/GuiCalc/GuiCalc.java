/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jbignums.GuiCalc;

import jbignums.JBigNums;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

/**
 *
 * @author Kestutis
 */

class GuiCalcState //Thread-safe.
{
    private volatile GuiCalc.CalcMode mode = GuiCalc.CalcMode.NORMAL;
    private volatile String query;
    private volatile String queryHistory;
    
    public AtomicBoolean isReady = new AtomicBoolean(true);
    public AtomicBoolean isConversionMode = new AtomicBoolean(false);
    public AtomicBoolean canTypeInQuery = new AtomicBoolean(false);   
    
    public synchronized GuiCalc.CalcMode getCalcMode(){ return mode; }
    public synchronized void setCalcMode(GuiCalc.CalcMode md){ mode = md; }
    
    public synchronized String getQuery(){ return query; }
    public synchronized void setQuery(String qu){ query = qu; }
    
    public synchronized String getQueryHistory(){ return queryHistory; }
    public synchronized void setQueryHistory(String qu){ queryHistory = qu; }
}

public class GuiCalc extends JFrame{
    public static final String VERSION = "v0.1";
    
    private static int frameCount=0;
    private final GuiCalcState cs = new GuiCalcState();
    
    public enum CalcMode { 
        NORMAL (300, 400), 
        SCIENTIFIC (600, 400); 
        
        private final int mWidth;
        private final int mHeight;
        CalcMode(int w, int h){
            mWidth = w;
            mHeight = h;
        }
        public int width(){ return mWidth; }
        public int height(){ return mHeight; }
    }
    
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
        
        ImageIcon icon = new ImageIcon(GuiCalc.class.getResource("/res/kawaii2.png")); //Our kawaii girl 
        
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

class GuiCalcMenuBar implements ActionListener
{    
    private final JMenuBar mubar;
    private final GuiCalc ginst;
    
    public GuiCalcMenuBar(GuiCalc theInst)
    {
        mubar = new JMenuBar();
        ginst = theInst;
        populateMenuBar(theInst.getCalcState());
    }
            
    private void populateMenuBar(GuiCalcState cs)
    {
    // View Menu.
        JMenu menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        mubar.add(menu);

        //Modes
        ButtonGroup group = new ButtonGroup();
        
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("Normal Mode");
        rbMenuItem.setSelected(cs.getCalcMode()==GuiCalc.CalcMode.NORMAL);
        rbMenuItem.setMnemonic(KeyEvent.VK_N);
        rbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
        rbMenuItem.setActionCommand("MenuBar_View_NormalMode");
        rbMenuItem.addActionListener(this);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Scientific Mode");
        rbMenuItem.setSelected(cs.getCalcMode()==GuiCalc.CalcMode.SCIENTIFIC);
        rbMenuItem.setMnemonic(KeyEvent.VK_S);
        rbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
        rbMenuItem.setActionCommand("MenuBar_View_ScientificMode");
        rbMenuItem.addActionListener(this);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        //a group of check box menu items
        menu.addSeparator();
        
        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Typed Input");
        cbMenuItem.setSelected(cs.canTypeInQuery.get());
        cbMenuItem.setMnemonic(KeyEvent.VK_T);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
        cbMenuItem.setActionCommand("View_TypedInput");
        cbMenuItem.addActionListener(this);
        menu.add(cbMenuItem);

        // Other modes (Conversion, etc)
        menu.addSeparator();
        
        cbMenuItem = new JCheckBoxMenuItem("Conversion&Stuff");
        cbMenuItem.setSelected(cs.isConversionMode.get());
        cbMenuItem.setMnemonic(KeyEvent.VK_O);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        cbMenuItem.setActionCommand("View_Conversion");
        cbMenuItem.addActionListener(this);
        menu.add(cbMenuItem);
        
    // Edit menu
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        mubar.add(menu);
        
        JMenuItem menuItem = new JMenuItem("Copy");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.setActionCommand("Edit_Copy");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Paste");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.setActionCommand("Edit_Paste");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("View History", KeyEvent.VK_H);
        menuItem.setActionCommand("Edit_History");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
    // Help Menu
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        mubar.add(menu);
        
        menuItem = new JMenuItem("View Help", KeyEvent.VK_E);
        menuItem.setActionCommand("Help_Help");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("About");
        menuItem.setActionCommand("Help_About");
        menuItem.addActionListener(this);
        menu.add(menuItem);
    }
    
    public JMenuBar getMenuBar()
    {
        return mubar;
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        switch(e.getActionCommand())
        {
            case "MenuBar_View_NormalMode":
                JOptionPane.showMessageDialog(null, "Normal Mode selected.", "InfoBox", JOptionPane.INFORMATION_MESSAGE);
                ginst.getCalcState().setCalcMode(GuiCalc.CalcMode.NORMAL);
                ginst.updateView();
                break;
            case "MenuBar_View_ScientificMode":
                JOptionPane.showMessageDialog(null, "Scientific Mode selected.", "InfoBox", JOptionPane.INFORMATION_MESSAGE);
                ginst.getCalcState().setCalcMode(GuiCalc.CalcMode.SCIENTIFIC);
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
