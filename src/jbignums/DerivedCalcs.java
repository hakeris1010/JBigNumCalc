package jbignums;

import java.util.Arrays;

class BasicCalculatorWithPow extends BasicCalculator
{
    @Override
    public void setOperation(String opcode) throws CalcException
    {
        this.setOperation(opcode, true);
    }
    
    @Override
    public void setOperation(String opcode, boolean throwExcIfBad) throws CalcException
    {
        super.setOpcodeFromValidValues(opcode, throwExcIfBad, new String[]{"+","-","*","/","^"});
    }
    
    @Override
    public double makeCalculation() throws CalculationErrorCalcException
    {
        result = (opc=="^" ? Math.pow(oper1, oper2) : super.makeCalculation());
        return result;
    }
    
    @Override
    public String toString()
    {
        return "BasicCalculatorWithPow: Operation:"+oper1+opc+oper2+", LastResult: "+result;
    }
}

class ThreeNumberCalculator extends BasicCalculator
{
    private double oper3 = 0;
    private String opc2 = "+";
    
    public ThreeNumberCalculator(String id, double op1, double op2, double op3)
    {
        super(id, op1, op2);
        oper3 = op3;
    }
    
    public void setOperands(double op1, double op2, double op3)
    {
        super.setOperands(op1, op2);
        oper3 = op3;
    }
    public void setOperation2(String opcode) throws CalcException
    {
        setOperation2(opcode, true);
    }
    public void setOperation2(String opcode, boolean throwExcIfBad) throws CalcException
    {
        opc2 = (Arrays.asList("+","-","*","/").contains(opcode) ? opcode : opc2);
        if(throwExcIfBad && !opc2.equals(opcode))
            throw new CalcException("Invalid value at setOperation("+opcode+")");
    }
    public String getOperation2(){
        return opc2;
    }
    public double getOperand3(){
        return oper3;
    }
    
    @Override
    public double makeCalculation() throws CalculationErrorCalcException
    {
        double tres = super.makeCalculation();
        result = (opc2=="+" ? tres+oper3 : (opc2=="-" ? tres-oper3 : 
                 (opc2=="*" ? tres*oper3 : (opc2=="/" ? tres/oper3 : tres))));
        if(!Double.isFinite(result))
            throw new CalculationErrorCalcException("ThreeOpCalc: Result is NaN!");
        return result;
    }
    
    @Override
    public String toString()
    {
        return "ThreeNumberCalculator: Operation:"+oper1+opc+oper2+opc2+oper3+", LastResult: "+result;
    } 
}
