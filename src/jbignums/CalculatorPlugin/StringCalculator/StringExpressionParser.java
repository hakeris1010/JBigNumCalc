package jbignums.CalculatorPlugin.StringCalculator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Basic String expression Parser class.
 * Parses a NumberFormat-type calculation expression into an XML format.
 * - For example. We have an expression:
 *      2 + (9*5) * log(2)
 * - We get this result:
 *
 * <number>2</number>
 * <oper>plus</oper>
 * <block>
 *     <number>9</number>
 *     <oper>mul</oper>
 *     <number>5</number>
 * </block>
 * <oper>mul</oper>
 * <func code="log">
 *     <arg>2</arg>
 * </func>
 *
 * ----------------------------------------
 * - We get this by traversing the string while searching for numbers and operations.
 */
public class StringExpressionParser {
    /**
     * Node type class for representing a calculation object, designed to be easily convertable to XML.
     */
    public static class CalcNode{
        public static final class Types{
            int DEFAULT = 0;
            int NUMBER = 1;
            int BASIC_OP = 2;
            int FUNCTION = 3;
            int BLOCK = 4;
        }

        public int type;
        public String data;
        public BigDecimal number;

        private final List<CalcNode> params = new ArrayList<>();
        private final List<CalcNode> childs = new ArrayList<>();

        public CalcNode(){ }
        public CalcNode(int nodeType, String sData, BigDecimal numb){
            type = nodeType;
            data = sData;
            number = numb;
        }

        public void addParam(CalcNode param){
            params.add(param);
        }
        public void addChild(CalcNode child){
            childs.add(child);
        }
        public List<CalcNode> getParams(){ return params; }
        public List<CalcNode> getChilds(){ return childs; }
    }

    /**
     * Base class for exceptions to appear during parsing.
     */
    class CalcNodeParseException extends RuntimeException{
        public static final int ERR_INVALID_CHARS   = 1;
        public static final int ERR_BAD_PARENTHESES = 2;

        private int where;
        private int errType;

        public CalcNodeParseException(String what){
            super(what);
        }
        public CalcNodeParseException(int where1, int errType1){
            super("Error when parsing a GrylloCalc String to a CalcNode: where:"+where1+", errType:"+errType1);
            where = where1;
            errType = errType1;
        }
        public int getWhere(){return where;}
        public int getErrType(){return errType;}
    }

    /**
     * Root CalcNode. This node contains all the child nodes of the expression.
     */
    private CalcNode rootNode;
    private Scanner reader;

    private boolean isWorking = false;

    /**
     * Private methods. This is the place where all the heavy lifting and magic is done.
     * The StringReader "reader" must be assigned to call this function.
     */
    private CalcNode getNextNode() throws CalcNodeParseException {
        /*if(reader.hasNext("[\\(]")){
            reader.nextByte();
            try{
                getNextNode();
            } catch (CalcNodeParseException e){

            }
        }*/
        return null;
    }

    /**
     * Constructor is only default.
     * The parsing is done by invoking the following method
     * @param expression - an ASCII string of GrylloCalc language, representing a calculation expression
     * @return - a CalcNode-type tree of elements to be calculated.
     */

    public CalcNode parseString(String expression) {
        synchronized (this){
            if(isWorking){
                System.out.println("Work is already in progress!");
                return null;
            }
            isWorking = true;
        }
        // Assign a new reader. We will use it to safely traverse a String.
        reader = new Scanner(expression);

        // All work is done here.
        /*while(reader.hasNext()){
            //if(reader.available() == 0) break;
            CalcNode nuNode = getNextNode();

        }*/

        // Close the scanner in the end.
        reader.close();

        // Perform synced tasks of exitting.
        synchronized (this){
            isWorking = false;
        }
        return rootNode;
    }

    public synchronized boolean isWorkingNow(){ return isWorking; }

    public synchronized CalcNode getRootNode(){
        if(isWorking) return null;
        return rootNode;
    }

}
