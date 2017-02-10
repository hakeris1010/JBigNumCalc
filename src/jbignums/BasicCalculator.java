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
    private double oper1 = 0;
    private double oper2 = 0;
    private String opc = "+";
    private double result;
    
    private static int calcCount;
    private final String calcId;
    
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
        if(Arrays.asList("-", "+", "*", "/").contains(opcode))
            opc = opcode;
        else 
            throw new RuntimeException("Invalid opcode.");
    }
    public void setOperation(String opcode, boolean throwExceptionIfBad)
    {
        if(throwExceptionIfBad)
            setOperation(opcode);
        else
        {
            opc = (Arrays.asList("-", "+", "*", "/").contains(opcode) ? opcode : "+");
        }
    }
    public String getOperation(){
        return opc;
    }
    public double getLastResult(){
        return result;
    }
    public double getOperand1(){
        return oper1;
    }
    public double getOperand2(){
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
        return "ID: " + calcId + ", Count: " + calcCount + ", Last Result:" + result;                
    }
    
    public void println()
    {
        System.out.println(this);
    }
}
