package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.IToken.SourceLocation;
import edu.ufl.cise.plc.IParser;
import edu.ufl.cise.plc.ast.ASTNode;

import java.util.List;


public class Parser implements IParser{
    String code;
    List<IToken> tokens;
    public Parser(String input){
        this.code=input;
    }
    public ASTNode parse() throws PLCException{
        //generate list of tokens using the lexer
        ILexer lex=CompilerComponentFactory.getLexer(code);
        do{
            tokens.add(lex.next());
        }while(tokens.get(tokens.size() - 1).getKind() != Kind.EOF);

    }
}
