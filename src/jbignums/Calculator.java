package jbignums;

public interface Calculator
{    
    public double makeCalculation();
    public double getLastResult();
}

interface TwoOperandCalculator extends Calculator
{
    public void setOperands(double op1, double op2);
    public double getOperand1();
    public double getOperand2();
    public void setOperation(String opcode);
    public String getOperation();
}

abstract class TwoOperandBasicCalculator implements TwoOperandCalculator
{    
    protected double oper1 = 0;
    protected double oper2 = 0;
    
    public double getOperand1(){
        return oper1;
    }
    public double getOperand2(){
        return oper2;
    }
    
    abstract public void setOperands(double op1, double op2);
    abstract public void setOperation(String opcode);
    abstract public String getOperation();
}

//test
