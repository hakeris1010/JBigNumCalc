/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jbignums.testolator;

import java.util.Arrays;

/**
 *
 * @author Kestutis
 */
public class BasicCalculator extends TwoOperandBasicCalculator implements Cloneable {
    protected String opc = "+";
    protected double result;
    
    private static int calcCount;
    private final String calcId;
    
    protected void setOpcodeFromValidValues(String opcode, boolean throwExceptionIfBad, String[] validvals) throws CalcException
    {
        opc = (Arrays.asList(validvals).contains(opcode) ? opcode : opc);
        if(throwExceptionIfBad && !opc.equals(opcode))
            throw new CalcException("Invalid value at setOperation("+opcode+")");
    }
    protected double makeCalculationWithParams(double op1, double op2, String opco) throws CalculationErrorCalcException
    {
        double r3sult = (opco=="+" ? op1+op2 : (opco=="-" ? op1-op2 : 
                (opco=="*" ? op1*op2 : (opco=="/" ? op1/op2 : 0))));
        if(!Double.isFinite(r3sult))
            throw new CalculationErrorCalcException("Result is NaN!");
        return r3sult;
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
    public void setOperation(String opcode) throws CalcException
    {
        this.setOpcodeFromValidValues(opcode, true, new String[]{"+","-","*","/"});
    }
    public void setOperation(String opcode, boolean throwExceptionIfBad) throws CalcException
    {
        this.setOpcodeFromValidValues(opcode, throwExceptionIfBad, new String[]{"+","-","*","/"});
    }
    
    public final String getOperation(){
        return opc;
    }
    public final double getLastResult(){
        return result;
    }
    
    public double makeCalculation() throws CalculationErrorCalcException
    {
        return (result = makeCalculationWithParams(oper1, oper2, opc));
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
    @Override
    public void println()
    {
        System.out.println(this);
    }
    @Override
    public BasicCalculator clone() throws CloneNotSupportedException
    {
        /*BasicCalculator nuca = (BasicCalculator)super.clone();
        /*nuca.oper1 = oper1;
        nuca.oper2 = oper2;
        nuca.calcId = calcId;
        nuca.opc = opc;
        nuca.result = result;
        return nuca;*/
        return (BasicCalculator)super.clone();
    }
}
