package jbignums.CalculatorPlugin.StringCalculator;

import com.sun.istack.internal.NotNull;
import jbignums.Helpers.Checkers;
import jbignums.Helpers.OutF;
import jbignums.Helpers.StrF;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
            // Primary node types.
            NUMBER,
            BASIC_OP,
            BLOCK,
            CONSTANT,
            VARIABLE,

            // Control types
            BLOCK_START,
            BLOCK_END,
            PARSE_START,
            PARSE_END,
            PARSE_INTERRUPTED
        }

        public static final class BlockTypesData{
            // Null implicitly means that CALC_BLOCK.
            public static final String CALC_BLOCK = "block";
            public static final String FUNCTION_BLOCK = "function";
            public static final String SET_BLOCK = "list";
        }

        public Types type;
        public String data;
        public BigDecimal number;

        private final List<CalcAttrib> attribs = new ArrayList<>();
        private final List<CalcNode> childs = new ArrayList<>();

        public CalcNode(){ }
        public CalcNode(Types nodeType, String sData, BigDecimal numb){
            type = nodeType;
            number = numb;
            data = sData;
            attribs.add(new CalcAttrib("data", sData));
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
            return toBasicStaring(this);
        }

        public static String toBasicStaring(CalcNode nextNode){
            return (nextNode == null ? "(null)" : "Type: \"" + nextNode.type.name() + "\"" +
                            (nextNode.data != null ? ", Data: " + nextNode.data : "") +
                            (nextNode.number != null ? ", Number: " + nextNode.number : "") );
        }
        public String toFullRecursiveString(){
            return StringExpressionParser.printCalcNodeToString(this, 0);
        }
    }

    /**
     * Base class for exceptions to appear during parsing.
     */
    public static class CalcNodeParseException extends RuntimeException{
        public enum Code {
             ERR_INVALID_TOKEN,
             ERR_BAD_PARENTHESES,
             TERMINATE_REQUESTED,
             ERR_FUNCTION_ARGLIST_EXPECTED,
             ERR_COMMA_NOTFUNCTION
        }

        private int where;
        private String token;
        private Code errType;

        public CalcNodeParseException(String what){
            super(what);
        }
        public CalcNodeParseException(Code errType1){
            super("CalcNodeParseExc. errType:"+errType1+"\n");
            errType = errType1;
        }
        public CalcNodeParseException(Code errType1, String tok, int where1){
            this(errType1);
            token = tok;
            where = where1;
        }
        public int getWhere(){return where;}
        public Code getErrType(){return errType;}
        public String getToken(){ return token;}
    }

    /**
     * Calculation expression patterns.
     */
    public static final class Patterns {
        // Parsing patterns
        public static final Pattern NUMBER = Pattern.compile("^[+-]?(?:\\d*\\.)?\\d+$");
        public static final Pattern BASIC_OPERATOR = Pattern.compile("[\\-+*/^%&]");
        public static final Pattern BLOCK_STARTER = Pattern.compile("[(]");
        public static final Pattern BLOCK_ENDER = Pattern.compile("[)]");
        public static final Pattern ARG_SEPARATOR = Pattern.compile("[,]");
        public static final Pattern FUNCTION_NAME = Pattern.compile("[a-zA-Z_](?:\\w+)");

        public static final Pattern CONSTANT = Pattern.compile("(?:e|pi)");
        public static final Pattern VARIABLE = Pattern.compile("[xXyYzZ](?:\\d*)?");

        // Operator priorities
        public static final Pattern BOP_PRIORITY_1 = Pattern.compile("[-+]");
    }

    private static final class RecursiveFlags {
        public static final int ARITHMETIC_ERRCHECK = 1 << 0;
        public static final int FUNCTION            = 1 << 1;
    }

    // Root CalcNode. This node contains all the child nodes of the expression
    private CalcNode rootNode;
    private CalcNodeParseException lastParseError;

    // SYNCHRONIZED List of shutdown vars to bind to. We use them to check for quit condition.
    private final List<AtomicBoolean> terminators = Collections.synchronizedList( new ArrayList<>() );

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
        if(shutdownVars != null)
            terminators.addAll( shutdownVars );
    }

    public void bindNodeQueue(@NotNull BlockingQueue<CalcNode> bindQueue){
        nodeQueue.set( bindQueue );
    }
    public void bindTerminatorList(@NotNull List<AtomicBoolean> lst){
        terminators.addAll( lst );
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
    private String preprocessExpression(String expr) throws CalcNodeParseException{
        expr = expr + " ";

        StringBuilder ret = new StringBuilder();
        char[] cl = expr.toCharArray();

        for(int i = 0; i < cl.length - 1; i++){
            if(i%50 == 0) { // Perform TerminatorCheck every 50 iterations
                if(Checkers.isOneAtomicBooleanSet(terminators))
                    throw new CalcNodeParseException(CalcNodeParseException.Code.TERMINATE_REQUESTED);
            }

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
        boolean print = OutF.LoggedMethods.StringExpressionParser_getNodeBlock;

        boolean timeToEnd = false;
        int iterations = 0;
        CalcNode newNode;
        // This one is used for specifying the type of recursion block to be entered,
        // e.g, function, list, or simple block (null).
        String blockType = null, blockFunctionName = null;

        OutF.logfn(print, "\n"+StrF.rep(' ', level)+"----------------");
        OutF.logfn(print, level*2,"getNodeBlock(): level: "+level+", flags: "+flags);

        // If currently inspected block node is a function, push the Arg Start node (BLOCK_START), because args=blocks
        if(mainNode.data!=null && mainNode.data.equals(CalcNode.BlockTypesData.FUNCTION_BLOCK)) {
            nodeQueue.get().add(new CalcNode(CalcNode.Types.BLOCK, null, null));
        }

        // Run loop until condition met.
        while(reader.hasNext() && !timeToEnd) {
            if(iterations%10 == 0) { // Check terminators every 10 iterations
                if(Checkers.isOneAtomicBooleanSet(terminators))
                    throw new CalcNodeParseException(CalcNodeParseException.Code.TERMINATE_REQUESTED);
            }
            iterations++;

            newNode = null;

            //========== Check all the variants ==========//
            // Using the if, else if, else if, ... , else

            // Block start spotted. Use the (optionally) earlier set up type variable to specify type.
            if (reader.hasNext(Patterns.BLOCK_STARTER)) {
                OutF.logfn(print, level*2, "Found block starter: "+reader.next());

                // Create new node, and add it's Block Type and, if specified (is a function), function name.
                newNode = new CalcNode(CalcNode.Types.BLOCK, blockType, null);
                if(blockFunctionName!=null)
                    newNode.addAttrib(new CalcNode.CalcAttrib("name",blockFunctionName));

                // Signal the processor waiting on queue that we're about to enter a new block.
                //nodeQueue.get().add(new CalcNode(CalcNode.Types.BLOCK_START, blockType, null));
                nodeQueue.get().add(newNode);

                // Call the function recursively to get the next block.
                getNodeBlock(reader, newNode, level+1 , 0);

                // Signal that we're returned from the block before.
                nodeQueue.get().add(new CalcNode(CalcNode.Types.BLOCK_END, blockType, null));
                // Reset the block type at the end.
                blockType = null;
                blockFunctionName = null;
            }

            // If BlockType variable has been set on node just before, but current node is
            // NOT a Block Starter, it means Syntax Error Occur3d!!!
            else if(blockType != null){
                throw new CalcNodeParseException(CalcNodeParseException.Code.ERR_FUNCTION_ARGLIST_EXPECTED, reader.next(), 0);
            }

            /* End block parenthesis:
             * - This function was called recursively, and the end of block is found --> return.
             */
            else if (reader.hasNext(Patterns.BLOCK_ENDER)){
                OutF.logfn(print, level*2, "Found block ender: "+reader.next());
                //reader.next();
                timeToEnd = true;
            }

            // Check for basic operators (+,-,*,/)
            else if (reader.hasNext(Patterns.BASIC_OPERATOR)){
                newNode = new CalcNode(CalcNode.Types.BASIC_OP, reader.next(), null);
                OutF.logfn(print, level*2, "Found a basic OP: "+newNode.data);
            }
            // Check for constants (pi, e, ...)
            else if (reader.hasNext(Patterns.CONSTANT)){
                newNode = new CalcNode(CalcNode.Types.CONSTANT, reader.next(), null);
                OutF.logfn(print, level*2, "Found a Constant: "+newNode.data);
            }
            // Check for variables (x1, x2, ...)
            else if (reader.hasNext(Patterns.VARIABLE)){
                newNode = new CalcNode(CalcNode.Types.VARIABLE, reader.next(), null);
                OutF.logfn(print, level*2, "Found a Variable: "+newNode.data);
            }
            // Check if next characters can be interpreted as a BigDecimal number.
            else if (reader.hasNextBigDecimal()) {
                newNode = new CalcNode(CalcNode.Types.NUMBER, null, reader.nextBigDecimal());
                OutF.logfn(print, level*2, "Found a BigDecimal number: "+newNode.number);
            }

            // Check if we encountered a ','
            else if(reader.hasNext(Patterns.ARG_SEPARATOR)){
                // fire an Exception if not on function
                if((flags & RecursiveFlags.FUNCTION) != RecursiveFlags.FUNCTION)
                    throw new CalcNodeParseException(CalcNodeParseException.Code.ERR_COMMA_NOTFUNCTION, reader.next(), 0);
                OutF.logfn(print, level*2, "Found an Arg Separator: "+reader.next());

                /* We treat individual function arguments as blocks, so we fire block start/end events on every
                 * function argument start/end.
                 * Typical event chain for function "fun ( 2 , 3 )":
                 * ("fun") FUNCTION -> ("(") BLOCK_START, ("(" and onFunction) BLOCK_START ->
                 * ("2") NUMBER -> ("," and onFunction) BLOCK_END, BLOCK_START -> ("3") NUMBER ->
                 * (")" and onFunction) BLOCK_END, (")") BLOCK_END
                 */
                // So here we fire end of old arg block and start of new arg block.
                nodeQueue.get().add(new CalcNode(CalcNode.Types.BLOCK_END, null, null));
                nodeQueue.get().add(new CalcNode(CalcNode.Types.BLOCK_START, null, null));
            }

            // Check for func.name
            else if(reader.hasNext(Patterns.FUNCTION_NAME)){
                // Set the blockType variable to function, and add function name.
                blockType = CalcNode.BlockTypesData.FUNCTION_BLOCK;
                blockFunctionName = reader.next();

                OutF.logfn(print, level*2, "Found a Function Name: "+blockFunctionName);
                OutF.logfn(print, level*2,"Starting argument collection recursively.");
                // At next iteration, the next token must be a block starter.
                // If next token if not a block starter, the blockType variable will be
                // checked, and as it is not null, exception will be thrown.
            }

            // If no valid variant has been found, Throw Error Exception!
            else{
                OutF.logfn(print, level*2, "ERROR! No valid match found!");
                throw new CalcNodeParseException(CalcNodeParseException.Code.ERR_INVALID_TOKEN,
                                                 reader.hasNext() ? reader.next() : "", 0);
            }

            //========== . ==========//
            // After checking all variants, if newNode exists, add it.
            // If node is NULL, it means no node was recognized --> ERROR ON EXPRESSION!!!!
            if(newNode != null){
                OutF.logfn(print, level*2, "Adding new node to root node.\n");
                mainNode.addChild(newNode);

                // Add the node to the queue if not a block-type.
                if( newNode.type != CalcNode.Types.BLOCK ){
                    nodeQueue.get().add( newNode );
                }
            }
        }
        // - At the loop end, if timeToEnd is still false (no end-block marker reached),
        //   and this function was called recursively (block start has been spotted), but stream has
        //   no next token available, it means there were errors with parenthesis syntax. Fire an Exception!
        // - OR: If on main block (level 0), and still some data is unread, it means closing bracket has been encountered.
        if( (!timeToEnd && level > 0) || (level == 0 && reader.hasNext())){
            throw new CalcNodeParseException(CalcNodeParseException.Code.ERR_BAD_PARENTHESES,
                                             reader.hasNext() ? reader.next() : "", 0);
        }

        // If currently inspected block node is a function, push the Arg Start node (BLOCK_START), because args=blocks
        if(mainNode.data!=null && mainNode.data.equals(CalcNode.BlockTypesData.FUNCTION_BLOCK)) {
            nodeQueue.get().add(new CalcNode(CalcNode.Types.BLOCK_END, null, null));
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

        CalcNode bamNode;
        CalcNodeParseException lastError = null;

        try {
            // Preprocess string: add spaces between operators and operands.
            // Exceptions Can Be Thrown Here!
            expression = preprocessExpression(expression);

            // Assign a new reader. We will use it to safely traverse a String.
            Scanner reader = new Scanner(expression);
            bamNode = new CalcNode(CalcNode.Types.BLOCK, null, null);

            // Signal the parse start to the queue.
            nodeQueue.get().add(new CalcNode(CalcNode.Types.PARSE_START, null, null));

            // Get all nodes recursively.
            // Exceptions Can Be Thrown Here!
            bamNode = getNodeBlock(reader, bamNode, 0, 0);

            // After the parsing is complete, signal the end of parsing to the waiting queue.
            nodeQueue.get().add(new CalcNode(CalcNode.Types.PARSE_END, null, null));

            // Close the scanner in the end.
            reader.close();
        }
        catch (CalcNodeParseException e){
            if(e.getErrType() == CalcNodeParseException.Code.TERMINATE_REQUESTED){
                System.out.println("Termination has been requested on TermVars! terminating.");
            }
            else {
                System.out.print("Exception while parsing: " + e);
                e.printStackTrace();
            }
            lastError = e;
            bamNode = null;

            // Push the "Interrupted" node onto the queue, to wake up waiters and signal them
            // that exception occured here.
            nodeQueue.get().add(new CalcNode(CalcNode.Types.PARSE_INTERRUPTED, null, null));
        }

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

    public synchronized CalcNodeParseException getLastParseError() {
        return lastParseError;
    }

    public void addTerminator(AtomicBoolean bol){
        terminators.add(bol);
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

    public static void Test_DEBUG1(){
        System.out.print("TESTBugging the StrCalParser!\n\n");
        String nuExpr = "2+6*log(56)+e+sin(2, 5)";
        /*String nuExpr = "2*+892 +    333.5+5^8 +log258(x)+(875+4-sin(pi)*2(opa())) +" +
        " 3*+8781.122 ((/ 0) (((2.654**2))    ((33453.54+5^2.58) +laog258(x+y+pi-(2*42.5446)))+(875+4.45654-sin(pi))*2(opa()))+(++2+))+";
        */

        BlockingQueue<CalcNode> BoomQueue = new LinkedBlockingQueue<>();
        // Create and bind to queue.
        StringExpressionParser parser = new StringExpressionParser(null, BoomQueue);

        // Testing the parser
        OutF.setOpened(true);

        // Start the parser thread, and after that start the NodeGetter on current thread.
        Runnable parseThread = () -> {
            long start1 = System.nanoTime();
            System.out.println("[ParseThread]: Parsing the expression...");
            CalcNode cnod = parser.parseString(nuExpr);
            System.out.println("[ParseThread]: Parsing complete! Time: " + (double) (System.nanoTime() - start1) / 1000.0 + " us\n");
        };

        // Start Node Processing Thread
        Runnable nodeProcessThread = () -> {
            long start2 = System.nanoTime();
            TestNodeData nd = new TestNodeData();
            nd.parser = parser;

            nodeQueueRecursiveInspector(BoomQueue, nd, 0);

            System.out.println("[NodeProcessThread]: Processing complete! Nodes parsed: " + nd.nodesPassed +
                               ", Time: " + (double) (System.nanoTime() - start2) / 1000.0 + " us\n");
        };

        //Start'em up!
        new Thread(parseThread).start();
        nodeProcessThread.run();

        /*try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        parser.addTerminator(new AtomicBoolean(true));
        */

        /*long start2 = System.nanoTime();
        System.out.println("\n--------------------\nNode.toString():");
        System.out.println(parser.getRootNode().toString());
        //cNode.toString();
        System.out.println("Time: "+(double)(System.nanoTime()-start2)/1000.0 + " us" );
        */
    }

    static private class TestNodeData{
        public int nodesPassed = 0;
        public StringExpressionParser parser;
    }

    private static int nodeQueueRecursiveInspector(BlockingQueue<CalcNode> BoomQueue, TestNodeData nd, int level){
        CalcNode nextNode = null;
        while (true) {
            // Take a node when available.
            try {
                nextNode = BoomQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            OutF.logfn(true,level*2, "[NodeProcessThread]: Got node: " +
                    (nextNode == null ? "(null)" : "Type: \"" + nextNode.type.name() + "\"" +
                            (nextNode.data != null ? ", Data: " + nextNode.data : "") +
                            (nextNode.number != null ? ", Number: " + nextNode.number : "")
                    )
            );
            nd.nodesPassed++;

            // Check if end node, if yes, parsing has ended, return -1 - end recursion.
            if (nextNode == null || nextNode.type == CalcNode.Types.PARSE_END || nextNode.type==CalcNode.Types.PARSE_INTERRUPTED){
                return -1;
            }
            // Block ended - just quit loop.
            if(nextNode.type == CalcNode.Types.BLOCK_END)
                break;

            // Check other possibilities.
            if(nextNode.type == CalcNode.Types.BLOCK_START){
                if(nodeQueueRecursiveInspector(BoomQueue, nd, level+1) == -1)
                    return -1;
            }
        }
        return 0;
    }
}
