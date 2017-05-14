package jbignums.Helpers;

import jbignums.CalculatorPlugin.StringCalculator.StringCalculator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

/**
 * Class to help the logging to files and specific formats.
 */
public class OutF{
    public static final class LoggedMethods {
        public static final boolean StringCalculator_startQueueProcessing = true;
        public static final boolean StringCalculator_simpleOperationChain = true;
        public static final boolean StringExpressionParser_getNodeBlock = false;
    }

    private static PrintStream strm = System.out;
    private static boolean open = true;

    private OutF(){}

    public static synchronized void setOutputStream(PrintStream str){
        strm = str;
    }
    public static synchronized void setOpened(boolean val){ open = val; }
    public static synchronized boolean isOpen(){ return open; }

    public static <T> void logf(T arg){ logf(true, 0, arg); }
    public static <T> void logfn(T arg){ logfn(true, 0, arg); }
    public static <T> void logf(boolean turnOn, T arg){ logf(turnOn, 0, arg); }
    public static <T> void logfn(boolean turnOn, T arg){ logfn(turnOn, 0, arg); }

    public static <T> void logfn(boolean turnedOn, int padBySpace, T arg){
        if(!open || !turnedOn) return;
        strm.print(StrF.rep(' ', padBySpace >= 0 ? padBySpace : 0));
        strm.print(arg);
        strm.println();
    }
    public static <T> void logf(boolean turnedOn, int padBySpace, T arg){
        if(!open || !turnedOn) return;
        strm.print(StrF.rep(' ', padBySpace >= 0 ? padBySpace : 0));
        strm.print(arg);
    }
}
