package jbignums.Helpers;

import java.util.Arrays;

public class StrF {
    private StrF(){ }

    public static String rep(char c, int count){
        char[] ba = new char[count];
        Arrays.fill(ba, c);
        return new String(ba);
    }
}
