package jbignums.Helpers;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Checkers {
    private Checkers(){ }
    public static boolean isOneAtomicBooleanSet(List<AtomicBoolean> bools){
        for(int i=0; i<bools.size(); i++){
            if(bools.get(i).get())
                return true;
        }
        return false;
    }
}
