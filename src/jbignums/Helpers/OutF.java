package jbignums.Helpers;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Class to help the logging to files and specific formats.
 */
public class OutF{
    private static PrintStream strm = System.out;
    private static boolean open = true;

    private OutF(){}

    public static synchronized void setOutputStream(PrintStream str){
        strm = str;
    }
    public static synchronized void setOpened(boolean val){ open = val; }
    public static synchronized boolean isOpen(){ return open; }

    public static <T> void logf(T arg){
        logf(0, arg);
    }
    public static <T> void logfn(T arg){
        logfn(0, arg);
    }
    public static <T> void logfn(int padBySpace, T arg){
        if(!open) return;
        logf(padBySpace, arg);
        strm.println();
    }
    public static <T> void logf(int padBySpace, T arg){
        if(!open) return;
        strm.print(StrF.rep(' ', padBySpace >= 0 ? padBySpace : 0));
        strm.print(arg);
    }
}
