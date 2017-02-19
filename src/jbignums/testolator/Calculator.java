package jbignums.testolator;

public interface Calculator
{    
    public double makeCalculation() throws CalculationErrorCalcException;
    public double getLastResult();
}

interface TwoOperandCalculator extends Calculator
{
    public void setOperands(double op1, double op2);
    public double getOperand1();
    public double getOperand2();
    public void setOperation(String opcode) throws CalcException;
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
    abstract public void setOperation(String opcode) throws CalcException ; 
    abstract public String getOperation();
    abstract public void println();
}
