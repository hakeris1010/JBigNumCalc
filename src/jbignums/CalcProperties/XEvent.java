package jbignums.CalcProperties;

public class XEvent {
    public static final int ACTION_PERFORMED = 1001;

    private Object source1;
    private int id1;
    private String command1;

    XEvent(Object source, int id, String command){
        source1 = source;
        id1 = id;
        command1 = command;
    }

    public Object getSource(){ return source1; }
    public int getID(){ return id1; }
    public String getCommand(){ return command1; }
}
