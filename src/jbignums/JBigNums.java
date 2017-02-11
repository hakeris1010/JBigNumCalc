/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jbignums;

/**
 *
 * @author Kestutis
 */
public class JBigNums {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Testing. BasicCalculator:");
        BasicCalculator calc = new BasicCalculator("1");
        calc.setOperands(1, 2);
        calc.setOperation("+");
        double res = calc.makeCalculation();
        System.out.println("Result: " + calc.getOperand1() + calc.getOperation() + calc.getOperand2() + " = " + res);
        calc.println();
        
        System.out.println("\nTesting ThreeNumberCalculator");
        ThreeNumberCalculator calc3 = new ThreeNumberCalculator("ThreeNum1", 2, 3, 4);
        calc3.setOperation("*");
        calc3.setOperation2("+");
        res = calc3.makeCalculation();
        System.out.println("Result: " + calc3.getOperand1() + calc3.getOperation() + 
                calc3.getOperand2() + calc3.getOperation2() + calc3.getOperand3() + " = " + res);
        calc3.println();
                
        System.out.println("\nTesting BasicCalcWithPow:");
        calc = new BasicCalculatorWithPow();
        calc.setOperands(2, 4);
        calc.setOperation("^");
        res = calc.makeCalculation();
        System.out.println("Result: " + calc.getOperand1() + calc.getOperation() + calc.getOperand2() + " = " + res);
        calc.println();
    }
    
}
