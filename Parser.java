package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.IToken.SourceLocation;
import edu.ufl.cise.plc.IParser;
import edu.ufl.cise.plc.ast.ASTNode;


public class Parser implements IParser{
    String code;
    public Parser(String input){
        this.code=input;
    }
    public ASTNode parse() throws PLCException{

    }
}
