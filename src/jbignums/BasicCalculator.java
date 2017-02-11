/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jbignums;

import java.util.Arrays;

/**
 *
 * @author Kestutis
 */
public class BasicCalculator {
    protected double oper1 = 0;
    protected double oper2 = 0;
    protected String opc = "+";
    protected double result;
    
    private static int calcCount;
    private final String calcId;
    
    protected void setOpcodeFromValidValues(String opcode, boolean throwExceptionIfBad, String[] validvals)
    {
        opc = (Arrays.asList(validvals).contains(opcode) ? opcode : opc);
        if(throwExceptionIfBad && !opc.equals(opcode))
            throw new RuntimeException("Invalid value at setOperation("+opcode+")");
    }
    
    public BasicCalculator()
    {
        calcId = "Undefined";
        calcCount++;
    }
    public BasicCalculator(String id)
    {
        calcId = id;
        calcCount++;
    }
    public BasicCalculator(String id, double op1, double op2)
    {
        this(id);
        oper1 = op1;
        oper2 = op2;
    }
   
    public void setOperands(double op1, double op2)
    {
        oper1=op1;
        oper2=op2;
    }
    public void setOperation(String opcode)
    {
        this.setOpcodeFromValidValues(opcode, true, new String[]{"+","-","*","/"});
    }
    public void setOperation(String opcode, boolean throwExceptionIfBad)
    {
        this.setOpcodeFromValidValues(opcode, throwExceptionIfBad, new String[]{"+","-","*","/"});
    }
    
    public final String getOperation(){
        return opc;
    }
    public final double getLastResult(){
        return result;
    }
    public final double getOperand1(){
        return oper1;
    }
    public final double getOperand2(){
        return oper2;
    }
    
    public double makeCalculation()
    {
        result = (opc=="+" ? oper1+oper2 : (opc=="-" ? oper1-oper2 : 
                (opc=="*" ? oper1*oper2 : (opc=="/" ? oper1/oper2 : 0))));
        return result;
    }
    public void swapOperands()
    {
        double tmp = oper1;
        oper1 = oper2;
        oper2 = tmp;
    }
    
    @Override
    public String toString()
    {
        return "BasicCalculator: ID: " + calcId + ", Count: " + calcCount + ", Last Result:" + result;                
    }
    
    public void println()
    {
        System.out.println(this);
    }
}
