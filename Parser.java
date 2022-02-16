package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.*;

import java.util.ArrayList;
import java.util.List;


public class Parser implements IParser{
    String code;
    List<IToken> tokens = new ArrayList<IToken>();
    int position =0;
    public Parser(String input){
        this.code=input;
    }
    public ASTNode parse() throws PLCException{
        //generate list of tokens using the lexer
        ILexer lex=CompilerComponentFactory.getLexer(code);
        do{
            tokens.add(lex.next());
        }while(tokens.get(tokens.size() - 1).getKind() != Kind.EOF);
        return expression();
    }
    Expr expression() throws SyntaxException {
        if(check(Kind.KW_IF))
            return conditionalExpression();
        else
            return logicalOrExpression();
    }

    ConditionalExpr conditionalExpression() throws SyntaxException {
        IToken head=tokens.get(position);
        position++;
        if(!check(Kind.LPAREN))
            throw new SyntaxException("Expected '(' after if statement");
        position++;
        Expr condition=expression();
        position++;
        Expr positive=expression();
        position++;
        Expr negative=expression();
        position++;
        return new ConditionalExpr(head, condition, positive, negative);
    }

    Expr logicalOrExpression() throws SyntaxException {
        IToken head = tokens.get(position);
        Expr left=logicalAndExpression();
        if(!check(Kind.OR)){
            return left;
        }
        else{
            position++;
            return new BinaryExpr(head, left, tokens.get(position-1), logicalOrExpression());
        }
    }

    Expr logicalAndExpression() throws SyntaxException {
        IToken head = tokens.get(position);
        Expr left=comparisonExpression();
        if(!check(Kind.AND)){
            return left;
        }
        else{
            position++;
            return new BinaryExpr(head, left, tokens.get(position-1), logicalAndExpression());
        }
    }

    Expr comparisonExpression() throws SyntaxException {
        IToken head = tokens.get(position);
        Expr left=additiveExpression();
        if(!check(Kind.LT, Kind.GT, Kind.EQUALS, Kind.NOT_EQUALS, Kind.LE, Kind.GE)){
            return left;
        }
        else{
            position++;
            return new BinaryExpr(head, left, tokens.get(position-1), comparisonExpression());
        }
    }

    Expr additiveExpression() throws SyntaxException {
        IToken head = tokens.get(position);
        Expr left=multiplicativeExpression();
        if(!check(Kind.PLUS,Kind.MINUS)){
            return left;
        }
        else{
            position++;
            return new BinaryExpr(head, left, tokens.get(position-1), additiveExpression());
        }
    }

    Expr multiplicativeExpression() throws SyntaxException {
        IToken head = tokens.get(position);
        Expr left=unaryExpression();
        if(!check(Kind.TIMES,Kind.DIV,Kind.MOD)){
            return left;
        }
        else{
            position++;
            return new BinaryExpr(head, left, tokens.get(position-1), multiplicativeExpression());
        }
    }

    Expr unaryExpression() throws SyntaxException {
        IToken head = tokens.get(position);
        if(check(Kind.BANG,Kind.MINUS,Kind.COLOR_OP,Kind.IMAGE_OP)){
            position++;
            return new UnaryExpr(head, tokens.get(position-1), unaryExpression());
        }
        else return unaryExpressionPostfix();
    }

    Expr unaryExpressionPostfix() throws SyntaxException {
        IToken head = tokens.get(position);
        Expr left=primaryExpression();
        if(check(Kind.LSQUARE)){
            PixelSelector selector=pixelSelector();
            return new UnaryExprPostfix(head, left, selector);
        }
        return left;
    }

    Expr primaryExpression() throws SyntaxException {
        IToken t=tokens.get(position);
        switch(t.getKind()){
            case BOOLEAN_LIT -> {
                position++;
                return new BooleanLitExpr(t);
            }
            case STRING_LIT -> {
                position++;
                return new StringLitExpr(t);
            }
            case INT_LIT -> {
                position++;
                return new IntLitExpr(t);
            }
            case FLOAT_LIT -> {
                position++;
                return new FloatLitExpr(t);
            }
            case IDENT -> {
                position++;
                return new IdentExpr(t);
            }
            case LPAREN -> {
                position++;
                Expr inner = expression();
                position++;
                return inner;
            }
            default -> {
                throw new SyntaxException("Unexpected token");
            }

        }
    }

    PixelSelector pixelSelector() throws SyntaxException {
        IToken t=tokens.get(position);
        position++;
        Expr x=expression();
        position++;
        Expr y=expression();
        position++;
        return new PixelSelector(t, x, y);
    }

    private boolean check(Kind... kinds){
        for(Kind k : kinds){
            if(tokens.get(position).getKind()==k)
                return true;
        }
        return false;
    }
}
