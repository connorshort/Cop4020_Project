package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.IToken.SourceLocation;

public class Token implements IToken {
    private SourceLocation location;
    private Kind kind;
    private String text;
    private int intValue;
    private float floatValue;
    private boolean booleanValue;
    private String stringValue;

    public Token(SourceLocation loc, Kind kind, String text){
        this.location=loc;
        this.kind=kind;
        this.text=text;
    }

    public Token(SourceLocation loc, Kind kind, String text, Object literal){
        this(loc, kind, text);
        switch(kind){
            case FLOAT_LIT:
                floatValue= (float) literal;
                break;
            case INT_LIT:
                intValue= (int) literal;
                break;
            case STRING_LIT:
                stringValue= (String) literal;
                break;
            case BOOLEAN_LIT:
                booleanValue= (boolean) literal;
                break;
        }
    }
    //returns the token kind
	public Kind getKind(){
        return kind;
    }

	public String getText(){
        return text;
    }
	
	public SourceLocation getSourceLocation(){
        return location;
    }

	public int getIntValue(){
        if(kind==Kind.INT_LIT) return intValue;
        else return 0;
    }

	//returns the float value represented by the characters of this token if kind is FLOAT_LIT
	public float getFloatValue(){
        if(kind==Kind.FLOAT_LIT)
            return floatValue;
        else return 0;
    }

	//returns the boolean value represented by the characters of this token if kind is BOOLEAN_LIT
	public boolean getBooleanValue(){
        if(kind==Kind.BOOLEAN_LIT) return booleanValue;
        else return false;
    }

	//returns the String represented by the characters of this token if kind is STRING_LIT
	//The delimiters should be removed and escape sequences replaced by the characters they represent.  
	public String getStringValue(){
        if(kind==Kind.STRING_LIT) return stringValue;
        else return "NON STRING LITERAL";
    }
}
