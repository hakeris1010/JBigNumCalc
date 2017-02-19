package jbignums.testolator;

class CalcException extends Exception
{
    protected String mWhat = "Calculator exception.";
    
    public CalcException(){ super(); }
    public CalcException(String what){
        mWhat = what;
    }
    
    @Override
    public String getMessage(){
        return mWhat;
    }
    @Override
    public String toString(){
        return super.toString()+"\nCalcExp: "+mWhat;
    }
}

class CalculationErrorCalcException extends CalcException
{
    public CalculationErrorCalcException(){ super(); }
    public CalculationErrorCalcException(String what){ super(what); }
    
    @Override
    public String getMessage(){
        return "BadResult: "+mWhat;
    }
    @Override
    public String toString(){
        return super.toString()+"\nCalcExp_BadResult: "+mWhat;
    }
}

