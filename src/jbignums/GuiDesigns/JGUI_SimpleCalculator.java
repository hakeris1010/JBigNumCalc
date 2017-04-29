package jbignums.GuiDesigns;

import jbignums.CalcProperties.GuiCalcProps;
import jbignums.CalcProperties.GuiCalcState;
import jbignums.StringCalculator.StringCalculator;

import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Kestutis on 2017-04-18.
 */

public class JGUI_SimpleCalculator implements GUIDesignLayout {
    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        RootPanel = new JPanel();
        RootPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        Feedback = new JPanel();
        Feedback.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        RootPanel.add(Feedback, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        CalcOutput = new JTextField();
        CalcOutput.setEditable(false);
        Feedback.add(CalcOutput, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        Feedback.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(-1, 60), null, 0, false));
        CalcInput = new JTextArea();
        scrollPane1.setViewportView(CalcInput);
        Buttons = new JPanel();
        Buttons.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        RootPanel.add(Buttons, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        Functions = new JPanel();
        Functions.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        Buttons.add(Functions, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        MCButton = new JButton();
        MCButton.setText("MC");
        Functions.add(MCButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        MRButton = new JButton();
        MRButton.setText("MR");
        Functions.add(MRButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        MSButton = new JButton();
        MSButton.setText("MS");
        Functions.add(MSButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        MPlusButton = new JButton();
        MPlusButton.setText("M+");
        Functions.add(MPlusButton, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        MMinusButton = new JButton();
        MMinusButton.setText("M-");
        Functions.add(MMinusButton, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        BackspaceButton = new JButton();
        BackspaceButton.setText("<--");
        Functions.add(BackspaceButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        CEButton = new JButton();
        CEButton.setText("CE");
        Functions.add(CEButton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        C_Button = new JButton();
        C_Button.setText("C");
        Functions.add(C_Button, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        InvertButton = new JButton();
        InvertButton.setText("±");
        Functions.add(InvertButton, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        SqrtButton = new JButton();
        SqrtButton.setText("√");
        Functions.add(SqrtButton, new com.intellij.uiDesigner.core.GridConstraints(1, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        Others = new JPanel();
        Others.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        Buttons.add(Others, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        Digits = new JPanel();
        Digits.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        Others.add(Digits, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        a7Button = new JButton();
        a7Button.setText("7");
        Digits.add(a7Button, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        a8Button = new JButton();
        a8Button.setText("8");
        Digits.add(a8Button, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        a9Button = new JButton();
        a9Button.setText("9");
        Digits.add(a9Button, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        a4Button = new JButton();
        a4Button.setText("4");
        Digits.add(a4Button, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        a5Button = new JButton();
        a5Button.setText("5");
        Digits.add(a5Button, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        a6Button = new JButton();
        a6Button.setText("6");
        Digits.add(a6Button, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        a1Button1 = new JButton();
        a1Button1.setText("1");
        Digits.add(a1Button1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        a2Button1 = new JButton();
        a2Button1.setText("2");
        Digits.add(a2Button1, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        a3Button1 = new JButton();
        a3Button1.setText("3");
        Digits.add(a3Button1, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        a0Button = new JButton();
        a0Button.setText("0");
        Digits.add(a0Button, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        PeriodButton = new JButton();
        PeriodButton.setText(".");
        PeriodButton.setToolTipText("'.'");
        Digits.add(PeriodButton, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        Operations = new JPanel();
        Operations.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        Others.add(Operations, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        DivideButton = new JButton();
        DivideButton.setText("/");
        Operations.add(DivideButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        ModButton = new JButton();
        ModButton.setText("%");
        Operations.add(ModButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        MulButton = new JButton();
        MulButton.setText("*");
        Operations.add(MulButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        HyperbolicButton = new JButton();
        HyperbolicButton.setText("1/x");
        Operations.add(HyperbolicButton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        MinusButton = new JButton();
        MinusButton.setText("-");
        Operations.add(MinusButton, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        PlusButton = new JButton();
        PlusButton.setText("+");
        Operations.add(PlusButton, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        EqualsButton = new JButton();
        EqualsButton.setText("=");
        Operations.add(EqualsButton, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        separator1.setOrientation(1);
        Others.add(separator1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        Buttons.add(separator2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return RootPanel;
    }

    //TODO: Whole GUI - One Object. GuiCalcState will no longer hold MenuBar and stuff.

    /**
     * ====================================================================
     * Code written by a human programmer.
     * ====================================================================
     */
    /**
     * = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * Constants
     */
    // Default values container class.
    private static final class Defaults {
        public static final boolean TypedInput = false;
    }

    // Event command codes of this layout.
    public static final class Commands {
        public static final String CALC_QUERY_ENTERED = "GUI_CalcQueryEntered";
        public static final String CALC_EXIT_REQUESTED = "GUI_ExitRequested";
    }

    /**
     * = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * Private fields
     */
    // The GuiCalcState, which we pass in Constructor.
    // We signal the main threads about events here.
    private GuiCalcState state;
    // a bool which is set the first time create() method is called.
    private volatile boolean guiCreated = false;

    // A List where we'll store MenuBar items belonging to this layout.
    // So on LayoutChanged event we could remove them from the menu bar.
    private ArrayList<JComponent> layoutMenuItems = new ArrayList<>();

    // ActionListeners attached to this layout (by a state or an initial thread)
    private final List<ActionListener> listeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
     * Public methods
     */
    public JGUI_SimpleCalculator() {
    }

    public JGUI_SimpleCalculator(GuiCalcState cState) {
        create(cState);
    }

    /**
     * New model of separating GUI from core is implemented.
     * - In this model, whole GUI is a Component, which fires events and whose state
     *   can be configured with setter methods.
     *
     * - This model, which is based on principle "Event Raiser lives Longer than the Target".
     * - In this case, the Raiser is the Native EDT, which lives whole application runtime.
     * - The GUIDesignLayout (e.g. current) lives shorter, and is a component which fires specific events.
     * - By using this model, we avoid memory leaks, and the problems of removing old layout's
     *   event listeners from the GuiCalcState.
     * - THE BIGGEST advantage: GUI is totally separated from the core!
     *
     * -*-*-*-*-*-*-
     * - This method takes care of all the events fired onto this layout.
     * - It's just like, for example, the main EDT dispatcher invoking JButton's private onEvent method.
     *   and that method calling all attached JButton's ActionListeners.
     */

    /**
     * Method receives the calculation result (intermediate or end), and updates GUI accordingly.
     * Before someone should call this, a CALC_QUERY_ENTERED event has to be raised by GUI,
     * requesting calculation start.
     *
     * @param res - a StringCalculator result object represented in GrylloCalc (or compatible) language.
     */
    public synchronized void sendCalculationResult(StringCalculator.Result res) {
        SwingUtilities.invokeLater(() -> {
            switch (res.resultType) {
                case StringCalculator.ResultType.INTERMEDIATE_DATA:
                    // Process the progress - update progress bars and stuff, if needed.
                    System.out.println("Got intermediate results from the Calculation: " + res.resultType);
                    break;
                case StringCalculator.ResultType.END:
                    // Update the result field!
                    CalcOutput.setText(res.strResult); // Use the in-built function to get simple form of Result.
                    break;
                //...
            }
        });
    }

    //Call when end result is ready. It's just a wrapper over sendCalculationResult
    //public void setEndCalculationResult(StringCalculator.Result res){ sendCalculationResult(res); }

    /**
     * Add the listener which will be called when specific events happen on this object.
     * for example, a calculation query is entered. Then, all added listeners are invoked.
     *
     * @param list - action listener object.
     */
    public void addActionListener(ActionListener list) {
        listeners.add(list);
    }

    /**
     * This method fires an event to all of the attached GUI event listeners.
     * - Keep in mind that all listeners will be executed on the GUI Event Dispatch Thread (In this case AWT EDT).
     */
    public void raiseEventToAttachedListeners(ActionEvent event) {
        Runnable task = () -> {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).actionPerformed(event);
            }
        };
        // We need to be sequential, so if we're already on EDT, just call it.
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else { // If not EDT, use the "invokeLater()".
            SwingUtilities.invokeLater(task);
        }
    }

    /**
     * At the time constructor is called, all the Swing Components are already initialized in
     * the Instance Initializer (Generated by GUI Designer), calling $$$setupUI$$$().
     * (Initializers are called before constructors)
     *
     * @param cState - calculator State, which will signal events.
     */
    @Override
    public synchronized void create(GuiState cState) {
        // The static initializer is called before the constructor, so currently all JFields are already init'd.
        if (guiCreated)
            throw new RuntimeException("GUI can only be create()'d once!");
        guiCreated = true;

        // Set the State pointer. We will use this to signal Main about events.
        state = (GuiCalcState) cState;

        //-- Swing Component (Output to Main Thread) listeners
        //===============    Keys    ================//

        // The Key Listener for checking if user inputted numbers on keyboard.
        RootPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                super.keyTyped(keyEvent);
                // Check for 1,2..9
            }
        });

        //================ Text Areas ================//

        // Input Area listener. If Typed Input is set, check if user inputted CalculationString by hand.
        CalcInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                System.out.println("insertUpdate");

            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {

            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {

            }
        });

        //================  Buttons  ================//

        // ActionListener for all buttons. They all fire ActionCommands.
        // We define this AnonClass, and then we'll add it to every button.
        ActionListener buttList = (ActionEvent actionEvent) -> {
            switch (actionEvent.getActionCommand()) {
                case "MC":
                    break;
                // And all others.
            }
        };

        MCButton.addActionListener(buttList);
        MRButton.addActionListener(buttList);
        MSButton.addActionListener(buttList);
        MPlusButton.addActionListener(buttList);
        MMinusButton.addActionListener(buttList);
        BackspaceButton.addActionListener(buttList);
        CEButton.addActionListener(buttList);
        C_Button.addActionListener(buttList);
        InvertButton.addActionListener(buttList);
        SqrtButton.addActionListener(buttList);
        a7Button.addActionListener(buttList);
        a8Button.addActionListener(buttList);
        a9Button.addActionListener(buttList);
        a4Button.addActionListener(buttList);
        a5Button.addActionListener(buttList);
        a6Button.addActionListener(buttList);
        a1Button1.addActionListener(buttList);
        a2Button1.addActionListener(buttList);
        a3Button1.addActionListener(buttList);
        a0Button.addActionListener(buttList);
        PeriodButton.addActionListener(buttList);
        DivideButton.addActionListener(buttList);
        ModButton.addActionListener(buttList);
        MulButton.addActionListener(buttList);
        HyperbolicButton.addActionListener(buttList);
        MinusButton.addActionListener(buttList);
        PlusButton.addActionListener(buttList);
        EqualsButton.addActionListener(buttList);

        //================  Update properties of GUI items  ================//
        // Change font size of Output TextField.
        Font outputFont = new JTextField().getFont().deriveFont(24f);
        CalcOutput.setFont(outputFont);
        CalcOutput.setText("Baba");

        //================  Add related menu items  ================//
        // Add menu items and listeners for this Layout.
        //createMenuBarItems();
    }

    // Create the control items in da MenuBar. GUIState must have been set at this point.
    private void createMenuBarItems() {
        // Set the checkbox controlling if we can type in the query into the queryfield.
        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Typed Input");
        cbMenuItem.setSelected(Defaults.TypedInput); // DEFAULT - can't type in.
        //cbMenuItem.setMnemonic(KeyEvent.VK_T);
        //cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
        cbMenuItem.setActionCommand("View_TypedInput");
        cbMenuItem.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("Typed Input Selected.");
                CalcInput.setEditable(true);
            }
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                System.out.println("Typed Input DeSelected.");
                CalcInput.setEditable(false);
            }
        });
        // Add to the list of currently used menu items, so that on destruction we could erase'em.
        layoutMenuItems.add(cbMenuItem);

        // Add separator.
        JSeparator separ = new JSeparator();
        layoutMenuItems.add(separ);

        //The following method automatically create the JMenu with the specified name if it doesn't exist.
        state.getGuiMenu().addMenuItem(new GUIMenu.MenuItem("View", separ));
        // Add current item.
        state.getGuiMenu().addMenuItem(new GUIMenu.MenuItem("View", cbMenuItem));
    }

    public JPanel getRootContentPanel() {
        // Return the Already-Initialized WootPaneru
        return RootPanel;
    }

    // GUI Designer generated items.

    private JTextArea CalcInput;
    private JTextField CalcOutput;
    private JPanel Functions;
    private JPanel Digits;
    private JPanel Operations;
    private JPanel Feedback;
    private JPanel Buttons;
    private JButton MCButton;
    private JButton MRButton;
    private JButton MSButton;
    private JButton MPlusButton;
    private JButton MMinusButton;
    private JButton BackspaceButton;
    private JButton CEButton;
    private JButton C_Button;
    private JButton InvertButton;
    private JButton SqrtButton;
    private JButton a7Button;
    private JButton a8Button;
    private JButton a9Button;
    private JButton a4Button;
    private JButton a5Button;
    private JButton a6Button;
    private JButton a1Button1;
    private JButton a2Button1;
    private JButton a3Button1;
    private JButton a0Button;
    private JButton PeriodButton;
    private JButton DivideButton;
    private JButton ModButton;
    private JButton MulButton;
    private JButton HyperbolicButton;
    private JButton MinusButton;
    private JButton PlusButton;
    private JButton EqualsButton;

    private JPanel RootPanel;
    private JPanel Others;
    private JScrollPane InputScrollPane;


}
