package jbignums.CalculatorPlugin.StringCalculator;

import com.sun.istack.internal.NotNull;
import jbignums.Helpers.OutF;
import jbignums.Helpers.StrF;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
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
 *     <number>2</number>
 *     <block>
 *         <number>2.5</number>
 *     </block>
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

        public enum Types {
            NUMBER,
            BASIC_OP,
            FUNCTION,
            BLOCK,
            CONSTANT,
            VARIABLE,

            BLOCK_START,
            BLOCK_END,
            PARSE_START,
            PARSE_END
        }

        public Types type;
        public String data;
        public BigDecimal number;

        private final List<CalcAttrib> attribs = new ArrayList<>();
        private final List<CalcNode> childs = new ArrayList<>();

        public CalcNode(){ }
        public CalcNode(Types nodeType, String sData, BigDecimal numb){
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
    public class CalcNodeParseException extends RuntimeException{
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
        public static final Pattern BASIC_OPERATOR = Pattern.compile("[\\-+*/^%&]");
        public static final Pattern BLOCK_STARTER = Pattern.compile("[(]");
        public static final Pattern BLOCK_ENDER = Pattern.compile("[)]");
        public static final Pattern ARG_SEPARATOR = Pattern.compile("[,]");
        public static final Pattern FUNCTION_NAME = Pattern.compile("[a-zA-Z_](?:\\w+)");

        public static final Pattern CONSTANT = Pattern.compile("(?:e|pi)");
        public static final Pattern VARIABLE = Pattern.compile("[xXyYzZ](?:\\d*)?");

        //========================================================//
    }

    private static final class RecursiveFlags{
        public static final int ARITHMETIC_ERRCHECK = 1 << 0;
        public static final int FUNCTION = 1 << 1;
    }

    // Root CalcNode. This node contains all the child nodes of the expression
    private CalcNode rootNode;
    private CalcNodeParseException lastParseError;

    // SYNCHRONIZED List of shutdown vars to bind to. We use them to check for quit condition.
    private AtomicReference< List<AtomicBoolean> > terminators = new AtomicReference<>();

    // A Reference to a Blocking Queue to which we pass newly parsed nodes if set.
    // Used by the multi-threaded simultaneous parsing and processing by the calculator.
    private AtomicReference< BlockingQueue<CalcNode> > nodeQueue = new AtomicReference<>();

    private boolean isWorking = false;

    /**
     * Bind a parser with an (optional) calculator and shutdown variables.
     * By default, a parser does not check them.
     */
    public StringExpressionParser(){ }
    public StringExpressionParser(List<AtomicBoolean> shutdownVars, BlockingQueue<CalcNode> bindQueue){
        nodeQueue.set( bindQueue );
        terminators.set( shutdownVars );
    }

    public void bindNodeQueue(BlockingQueue<CalcNode> bindQueue){
        nodeQueue.set( bindQueue );
    }
    public void bindTerminatorList(List<AtomicBoolean> lst){
        terminators.set(lst);
    }

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

    /**
     * Stage 1 of parsing: The Preprocessing.
     * - The operators and numbers/functions/variables are sepacrated by spaces to get ready for tokenizing.
     * @param expr - String-type expression.
     * @return - Preprocessed string expression.
     */
    private static String preprocessExpression(String expr){
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
     * Stage 2 of parsing: The "Nodeifying"
     * - All string tokens (operators, numbers, functions) are converted to Nodes.
     * - One call to this function return one Block - The Block Node containing nodes between Parentheses ( )
     * - Function works recursively. When Block start ( is encountered, it calls itself on the following element.
     * - The Scanner is used to iterate through an Expression string.
     * - Most Syntax errors are detected in this stage (invalid characters, bad parenthesis structure).
     * - However, the duplicate operators and other calculation-specific errors are undetected.
     * @param reader - the Scanner object by which we iterate the expression
     * @param mainNode - the NotNull root node of the block to be parsed
     * @param level - level of recursion.
     * @param flags - flags specifying specific things:
     *        - FUNCTION - treat the block as a function parameter list, and return the node as a function.
     */
    private CalcNode getNodeBlock(@NotNull Scanner reader, @NotNull CalcNode mainNode, int level, int flags) throws CalcNodeParseException {
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
        int opCount = 0;
        CalcNode newNode;

        OutF.logfn("\n"+StrF.rep(' ', level)+"----------------");
        OutF.logfn(level*2,"getNodeBlock(): level: "+level+", flags: "+flags);

        // Run loop until condition met.
        while(reader.hasNext() && !timeToEnd) {
            newNode = null;
            boolean noMatchFound = false;

            //========== Check all the variants ==========//
            // Using the if, else if, else if, ... , else
            //TODO: When performing these checks, also track the state and detect errors like
            //      duplicate operators (+*).
            //      Option #2: Perform these checks on another phase, the "Tree Error Correction".

            /* Block start parenthesis: 2 options:
             * 1. New block start --> call recursion.
             * 2. Function ArgList start --> turn to arg mode.
             */
            if (reader.hasNext(Patterns.BLOCK_STARTER)) {
                OutF.logfn(level*2, "Found block starter: "+reader.next());
                //reader.next();
                newNode = new CalcNode(CalcNode.Types.BLOCK, null, null);
                // Call the recursive method up, to get the block.

                // Signal the processor waiting on queue that we're about to enter a new block.
                if(nodeQueue.get() != null){
                    nodeQueue.get().add(new CalcNode(CalcNode.Types.BLOCK_START, null, null));
                }

                // Call the function recursively to get the next block.
                getNodeBlock(reader, newNode, level+1 , 0);

                // Signal that we're returned from the block before.
                if(nodeQueue.get() != null){
                    nodeQueue.get().add(new CalcNode(CalcNode.Types.BLOCK_END, null, null));
                }
            }

            /* End block parenthesis:
             * - This function was called recursively, and the end of block is found --> return.
             */
            else if (reader.hasNext(Patterns.BLOCK_ENDER)){
                OutF.logfn(level*2, "Found block ender: "+reader.next());
                //reader.next();
                timeToEnd = true;
            }

            // Check for basic operators (+,-,*,/)
            else if (reader.hasNext(Patterns.BASIC_OPERATOR)){
                newNode = new CalcNode(CalcNode.Types.BASIC_OP, reader.next(), null);
                OutF.logfn(level*2, "Found a basic OP: "+newNode.data);
            }
            // Check for constants (pi, e, ...)
            else if (reader.hasNext(Patterns.CONSTANT)){
                newNode = new CalcNode(CalcNode.Types.CONSTANT, reader.next(), null);
                OutF.logfn(level*2, "Found a Constant: "+newNode.data);
            }
            // Check for variables (x1, x2, ...)
            else if (reader.hasNext(Patterns.VARIABLE)){
                newNode = new CalcNode(CalcNode.Types.VARIABLE, reader.next(), null);
                OutF.logfn(level*2, "Found a Variable: "+newNode.data);
            }

            // Check if we encountered a ','
            else if(reader.hasNext(Patterns.ARG_SEPARATOR)){
                //Skip the separator, or fire an Exception if not on function
                if((flags & RecursiveFlags.FUNCTION) != RecursiveFlags.FUNCTION)
                    throw new CalcNodeParseException(0, CalcNodeParseException.ERR_INVALID_CHARS);
                OutF.logfn(level*2, "Found an Arg Separator: "+reader.next());
                //reader.next();
            }

            // Check for func.name
            else if(reader.hasNext(Patterns.FUNCTION_NAME)){
                // Get it, and add the function name to node.
                newNode = new CalcNode(CalcNode.Types.FUNCTION, reader.next(), null);
                OutF.logfn(level*2, "Found a Function Name: "+newNode.data);
                OutF.logfn(level*2,"Starting argument collection recursively.");

                // After function name, we immediately expect a Parenthesis, signaling arg list beginning.
                // If no such thing can be found next, SYNTAX ERROR!!!
                if(!reader.hasNext(Patterns.BLOCK_STARTER)) {
                    OutF.logfn("No parenthesis after function. Next value: \""+reader.next()+"\"");
                    throw new CalcNodeParseException(0, CalcNodeParseException.ERR_BAD_PARENTHESES);
                }
                // Remove the block starter, and launch recursion on further nodes.
                reader.next();

                if(nodeQueue.get() != null){
                    // Add this function node
                    nodeQueue.get().add( newNode );
                    // Signal the processor waiting on queue that we're about to enter a new block.
                    nodeQueue.get().add(new CalcNode(CalcNode.Types.BLOCK_START, null, null));
                }

                // Call the function recursively to get the next block.
                getNodeBlock(reader, newNode, level+1, RecursiveFlags.FUNCTION);

                // Signal that we're returned from the block before.
                if(nodeQueue.get() != null){
                    nodeQueue.get().add(new CalcNode(CalcNode.Types.BLOCK_END, null, null));
                }
            }

            // Check if next characters can be interpreted as a BigDecimal number.
            else if (reader.hasNextBigDecimal()) {
                newNode = new CalcNode(CalcNode.Types.NUMBER, null, reader.nextBigDecimal());
                OutF.logfn(level*2, "Found a BigDecimal number: "+newNode.number);
            }

            else{ // If no valid variant has been found, set the var to true.
                OutF.logfn(level*2, "ERROR! No valid match found!");
                noMatchFound = true;
            }

            //========== . ==========//
            // After checking all variants, if newNode exists, add it.
            // If node is NULL, it means no node was recognized --> ERROR ON EXPRESSION!!!!
            if(newNode != null){
                OutF.logfn(level*2, "Adding new node to root node.\n");
                mainNode.addChild(newNode);

                // Add the node to the queue if not a block-type.
                if(nodeQueue.get() != null && ( newNode.type != CalcNode.Types.BLOCK &&
                                                newNode.type != CalcNode.Types.FUNCTION )){
                    nodeQueue.get().add( newNode );
                }
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
        // If on main block (level 0), and still some data is unread, it means closing bracket has been encountered.
        if(level == 0 && reader.hasNext()){
            throw new CalcNodeParseException(0, CalcNodeParseException.ERR_BAD_PARENTHESES);
        }

        return mainNode;
    }

    /**
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
            lastParseError = null; // Nullify the last error - make a fresh start.
        }
        expression = preprocessExpression(expression);

        // Assign a new reader. We will use it to safely traverse a String.
        Scanner reader = new Scanner(expression);
        CalcNode bamNode = new CalcNode(CalcNode.Types.BLOCK, null, null);
        CalcNodeParseException lastError = null;

        // Signal the parse start to the queue.
        nodeQueue.get().add( new CalcNode(CalcNode.Types.PARSE_START, null, null) );

        // Get all nodes recursively.
        try {
            bamNode = getNodeBlock(reader, bamNode, 0, 0);

        } catch (CalcNodeParseException e){
            System.out.print("Exception while parsing: "+e);
            e.printStackTrace();
            System.out.println("\nException occured on token: " + (reader.hasNext() ? reader.next() : "end of expr.") );

            lastError = e;
            bamNode = null;
        }
        // After the parsing is complete, signal the end of parsing to the waiting queue.
        nodeQueue.get().add( new CalcNode(CalcNode.Types.PARSE_END, null, null) );

        // Close the scanner in the end.
        reader.close();

        // Perform synced tasks of exitting.
        synchronized (this){
            rootNode = bamNode;
            lastParseError = lastError;
            isWorking = false;
        }
        return rootNode;
    }

    public synchronized boolean isWorkingNow(){ return isWorking; }

    public synchronized CalcNode getRootNode(){
        if(isWorking) return null;
        return rootNode;
    }

    public synchronized CalcNodeParseException getLastParseError(){
        return lastParseError;
    }

    /**
     * Method prints the CalcNode recursively
     */
    public static String printCalcNodeToString( CalcNode root, int level ){
        if(root == null)
            return "(null)";

        String retstr = StrF.rep(' ', level*2) + "Type: " + root.type.name() + ", data: " + root.data + ", no: " + root.number + "\n";

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

        String nuExpr = "2*+892 +    333.5+5^8 +log258(x)+(875+4-sin(pi)*2(opa())) +" +
        " 3*+8781.122 ((/ 0) (((2.654**2))    ((33453.54+5^2.58) +laog258(x+y+pi-(2*42.5446)))+(875+4.45654-sin(pi))*2(opa()))+(++2+))+";

        BlockingQueue<CalcNode> BoomQueue = new LinkedBlockingQueue<>();
        // Create and bind to queue.
        StringExpressionParser parser = new StringExpressionParser(null, BoomQueue);

        // Testing the parser
        OutF.setOpened(false);

        // Start the parser thread, and after that start the NodeGetter on current thread.
        new Thread( () -> {
            long start1 = System.nanoTime();
            System.out.println("\n[ParseThread]: Parsing the expression...\n");
            CalcNode cnod = parser.parseString(nuExpr);
            System.out.println("\n[ParseThread]: Parsing complete! Time: " + (double) (System.nanoTime() - start1) / 1000.0 + " us\n");
        }).start();

        // Start taking values from Queue and inspect them in this loop.
        nodeQueueRecursiveInspector(BoomQueue, 0);

        /*long start2 = System.nanoTime();
        System.out.println("\n--------------------\nNode.toString():");
        System.out.println(parser.getRootNode().toString());
        //cNode.toString();
        System.out.println("Time: "+(double)(System.nanoTime()-start2)/1000.0 + " us" );
        */
    }

    private static int nodeQueueRecursiveInspector(BlockingQueue<CalcNode> BoomQueue, int level){
        CalcNode nextNode = null;
        while (true) {
            // Take a node when available.
            try {
                nextNode = BoomQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(StrF.rep(' ',level*2) + "[NodeProcessThread]: Got node: " +
                    (nextNode == null ? "(null)" : "Type: \"" + nextNode.type.name() + "\"" +
                            (nextNode.data != null ? ", Data: " + nextNode.data : "") +
                            (nextNode.number != null ? ", Number: " + nextNode.number : "")
                    )
            );

            // Check if end node, if yes, parsing has ended, return -1 - end recursion.
            if (nextNode == null || nextNode.type == CalcNode.Types.PARSE_END){
                return -1;
            }
            // Block ended - just quit loop.
            if(nextNode.type == CalcNode.Types.BLOCK_END)
                break;

            // Check other possibilities.
            if(nextNode.type == CalcNode.Types.BLOCK_START){
                if(nodeQueueRecursiveInspector(BoomQueue, level+1) == -1)
                    return -1;
            }
        }
        return 0;
    }
}
