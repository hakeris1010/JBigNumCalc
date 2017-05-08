package jbignums.CalculatorPlugin.StringCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

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
 *     <arg>
 *         <block>
 *             <number>2.5</number>
 *         </block>
 *     </arg>
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
        public static class CalcAttrib{
            public String name, value;
            public CalcAttrib(String nm, String val){
                name = nm; value = val;
            }
        }

        public static final int NUMBER   = 1;
        public static final int BASIC_OP = 2;
        public static final int FUNCTION = 3;
        public static final int BLOCK    = 4;
        public static final int CONSTANT = 5;
        public static final int VARIABLE = 6;


        public int type;
        public String data;
        public BigDecimal number;

        private final List<CalcAttrib> attribs = new ArrayList<>();
        private final List<CalcNode> childs = new ArrayList<>();

        public CalcNode(){ }
        public CalcNode(int nodeType, String sData, BigDecimal numb){
            type = nodeType;
            data = sData;
            number = numb;
        }

        public void addAttrib(CalcAttrib param){
            attribs.add(param);
        }
        public void addChild(CalcNode child){
            childs.add(child);
        }
        public List<CalcAttrib> getAttribs(){ return attribs; }
        public List<CalcNode> getChilds(){ return childs; }

        @Override
        public String toString(){
            return StringExpressionParser.printCalcNodeToString(this, 0);
        }
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
     * Calculation expression patterns.
     */
    public static final class Patterns {
        public static final Pattern NUMBER = Pattern.compile("^[+-]?(?:\\d*\\.)?\\d+$");
        public static final Pattern BASIC_OPERATOR = Pattern.compile("[+*/&\\-^]");
        public static final Pattern BLOCK_STARTER = Pattern.compile("[(]");
        public static final Pattern BLOCK_ENDER = Pattern.compile("[)]");
        public static final Pattern ARG_SEPARATOR = Pattern.compile("[,]");
        public static final Pattern FUNCTION_NAME = Pattern.compile("[a-zA-Z_](?:\\w+)");

        public static final Pattern CONSTANT = Pattern.compile("[e|pi]");
        public static final Pattern VARIABLE = Pattern.compile("[xXyYzZ](?:\\d*)?");

        //========================================================//
    }

    private static final class RecursiveFlags{
        public static final int CALLED_RECURSIVELY = 1 << 0;
        public static final int FUNCTION = 1 << 1;
    }

    /**
     * Root CalcNode. This node contains all the child nodes of the expression.
     */
    private CalcNode rootNode;

    private boolean isWorking = false;

    /**
     * Preprocess the string:
     * - Add Whitespaces between operators and names.
     */
    private static int needsWhitespaceBetween(char c1, char c2){
        // Number (1.0)
        if( (Character.isDigit(c1) && Character.isDigit(c2)) ||
            (Character.isDigit(c1) && c2=='.') ||
            (c1=='.' && Character.isDigit(c2)) )
            return -1;

        // Function name or constant (log2)
        if( (Character.isAlphabetic(c1) && Character.isDigit(c2)) ||
            (Character.isAlphabetic(c2) && Character.isDigit(c1)) ||
            (Character.isAlphabetic(c1) && Character.isAlphabetic(c2)) )
            return -1;

        // Parenthesis after function name ( log2( ) - NOT NEEDED ANYMORE.
        //if( (Character.isAlphabetic(c1) || Character.isDigit(c1)) && c2=='(' )

        // Whitespace and other character together
        if( Character.isWhitespace(c1) && !Character.isWhitespace(c2) ||
            Character.isWhitespace(c2) && !Character.isWhitespace(c1) )
            return -1;

        // All other cases (2+,+2,etc) - space between.
        return 1;
    }

    private static int needsDeletion(char c1, char c2){
        if(Character.isWhitespace(c1) && Character.isWhitespace(c2))
            return 0;
        return -1;
    }

    private static String preprocessExpression(String expr){
        /*ArrayList<Character> cl = new ArrayList<>();
        for(char i : expr.toCharArray()){
            cl.add( i );
        }
        int tmp;*/

        expr = expr + " ";

        StringBuilder ret = new StringBuilder();
        char[] cl = expr.toCharArray();
        int tmp;
        for(int i = 0; i < cl.length - 1; i++){
            if(needsDeletion(cl[i], cl[i+1]) >= 0){
                // delete cl[i]
            }
            else if(needsWhitespaceBetween( cl[i], cl[i+1] ) >= 0){
                ret.append(cl[i]);
                ret.append(' ');
            }
            else // No operation required - just add current char.
                ret.append(cl[i]);
        }

        return ret.toString();
    }

    /**
     * Private methods. This is the place where all the heavy lifting and magic is done.
     * The StringReader "reader" must be assigned to call this function.
     * @param reader - the Scanner object by which we iterate the expression
     * @param flags - flags specifying specific things:
     *        - FUNCTION - treat the block as a function parameter list, and return the node as a function.
     *
     */
    private CalcNode getNodeBlock(Scanner reader, CalcNode mainNode, int level, int flags) throws CalcNodeParseException {
        /* Several possibilities here - current symbol is:
         * Valid ones:
         * 1. Digit - indicates number operand.
         * 2. Bracket (, )
         * 4. Standard operand:
         *    - Arithmetic: +,-,*,/,^,%
         * ---- The following are not implemented ----
         * 5. Extended operand:
         *    - Equational: >, <, >=, <=, =
         *    - Bitwise: &,|,~
         *    - Logical implication: -->, <-->
         * 6. Letter:
         *    - Function name start (log, sin)
         *    - Constant (pi, e)
         *    - Variable (x1, x2, y1, y2)
         * 7. Array bracket: {, [, ], }
         */
        boolean timeToEnd = false;
        CalcNode newNode = null;

        // Run loop until condition met.
        while(reader.hasNext() && !timeToEnd) {
            newNode = null;
            boolean noMatchFound = false;

            //========== Check all the variants ==========//
            // Using the if, else if, else if, ... , else

            /* Block start parenthesis: 2 options:
             * 1. New block start --> call recursion.
             * 2. Function ArgList start --> turn to arg mode.
             */
            if (reader.hasNext(Patterns.BLOCK_STARTER)) {
                reader.next();
                newNode = new CalcNode(CalcNode.BLOCK, null, null);
                // Call the recursive method up, to get the block.
                getNodeBlock(reader, newNode, level+1 , 0);
            }

            /* End block parenthesis:
             * - This function was called recursively, and the end of block is found --> return.
             */
            else if (reader.hasNext(Patterns.BLOCK_ENDER)){
                reader.next();
                timeToEnd = true;
            }

            // Check for basic operators (+,-,*,/)
            else if (reader.hasNext(Patterns.BASIC_OPERATOR)){
                newNode = new CalcNode(CalcNode.BASIC_OP, reader.next(), null);
            }
            // Check for constants (pi, e, ...)
            else if (reader.hasNext(Patterns.CONSTANT)){
                newNode = new CalcNode(CalcNode.CONSTANT, reader.next(), null);
            }
            // Check for variables (x1, x2, ...)
            else if (reader.hasNext(Patterns.VARIABLE)){
                newNode = new CalcNode(CalcNode.VARIABLE, reader.next(), null);
            }

            // Check if we encountered a ','
            else if(reader.hasNext(Patterns.ARG_SEPARATOR)){
                //Skip the separator, or fire an Exception if not on function
                if((flags & RecursiveFlags.FUNCTION) != RecursiveFlags.FUNCTION)
                    throw new CalcNodeParseException(0, CalcNodeParseException.ERR_INVALID_CHARS);
                reader.next();
            }

            // Check for func.name
            else if(reader.hasNext(Patterns.FUNCTION_NAME)){
                // Get it, and add the function name to node.
                newNode = new CalcNode(CalcNode.FUNCTION, reader.next(), null);

                // After function name, we immediately expect a Parenthesis, signaling arg list beginning.
                // If no such thing can be found next, SYNTAX ERROR!!!
                if(!reader.hasNext(Patterns.BLOCK_STARTER)) {
                    System.out.println("No parenthesis after function name: \""+newNode.data+"\". Next value: \""+reader.next()+"\"");
                    throw new CalcNodeParseException(0, CalcNodeParseException.ERR_BAD_PARENTHESES);
                }

                getNodeBlock(reader, newNode, level+1, RecursiveFlags.FUNCTION);
            }

            // Check if next characters can be interpreted as a BigDecimal number.
            else if (reader.hasNextBigDecimal()) {
                newNode = new CalcNode(CalcNode.NUMBER, null, reader.nextBigDecimal());
            }

            else{ // If no valid variant has been found, set the var to true.
                noMatchFound = true;
            }

            //========== . ==========//
            // After checking all variants, if newNode exists, add it.
            // If node is NULL, it means no node was recognized --> ERROR ON EXPRESSION!!!!
            if(newNode != null){
                mainNode.addChild(newNode);
            }
            else if(noMatchFound){ // No match found --> syntax error!
                throw new CalcNodeParseException(0, CalcNodeParseException.ERR_INVALID_CHARS);
            }
        }
        // At the loop end, if timeToEnd is still false (no end-block marker reached),
        // and this function was called recursively (block start has been spotted), but stream has
        // no next token available, it means there were errors with parenthesis syntax. Fire an Exception!
        if(!timeToEnd && level > 0){
            throw new CalcNodeParseException(0, CalcNodeParseException.ERR_BAD_PARENTHESES);
        }

        return mainNode;
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
        expression = preprocessExpression(expression);

        // Assign a new reader. We will use it to safely traverse a String.
        Scanner reader = new Scanner(expression);

        CalcNode bamNode = new CalcNode(CalcNode.BLOCK, null, null);
        // Get all nodes recursively.
        try {
            bamNode = getNodeBlock(reader, bamNode, 0, 0);

        } catch (CalcNodeParseException e){
            System.out.print("Exception while parsing: "+e);
            e.printStackTrace();
            System.out.println("\nException occured on token: " + (reader.hasNext() ? reader.next() : "end of expr.") );

            bamNode = null;
        }

        // Close the scanner in the end.
        reader.close();

        // Perform synced tasks of exitting.
        synchronized (this){
            rootNode = bamNode;
            isWorking = false;
        }
        return rootNode;
    }

    public synchronized boolean isWorkingNow(){ return isWorking; }

    public synchronized CalcNode getRootNode(){
        if(isWorking) return null;
        return rootNode;
    }

    /**
     * Method prints the CalcNode recursively
     */
    public static String printCalcNodeToString( CalcNode root, int level ){
        if(root == null)
            return "(null)";

        String retstr = String.join("", Collections.nCopies(level, " "));
        retstr += "Type: " + root.type + ", data: " + root.data + ", no: " + root.number + "\n";

        for(CalcNode i : root.getChilds()){
            retstr += printCalcNodeToString(i, level + 1);
        }
        return  retstr;
    }

    /**
     * Test method to be called from Main while DEBUGGING.
     */

    public static void Test_DEBUG(){
        System.out.print("TESTBugging the StrCalParser!\n\n");

        long start;
        CalcNode cNode = null;
        String nuExpr = "2*+892 +    333.5+5^8 +log258(x)+(875+4-sin(pi)*2(opa()))";
/*
        // Testing Preprocessor
        start = System.nanoTime();
        System.out.println( "Test preprocessor:\n" + preprocessExpression( nuExpr ) );
        System.out.println("Time: "+(double)(System.nanoTime()-start)/1000.0 + " us" );
*/
        // Testing the parser
        start = System.nanoTime();

        System.out.println("Parsing the expression...");
        cNode = new StringExpressionParser().parseString(nuExpr);
        System.out.println("Time: "+(double)(System.nanoTime()-start)/1000.0 + " us" );


    }
}
