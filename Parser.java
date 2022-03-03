package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.Types.Type;

import java.beans.Expression;
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
        return program();
    }
    Expr expression() throws SyntaxException {
        if(check(Kind.KW_IF))
            return conditionalExpression();
        else
            return logicalOrExpression();
    }

    void checkIncomplete(String message) throws SyntaxException{
        if(check(Kind.EOF))
            throw new SyntaxException(message);
    }

    //start of assignment 3 changes
    Program program() throws SyntaxException{
        IToken head=tokens.get(position);
        Type type;
        String name;
        List<NameDef> params = new ArrayList<NameDef>();
        List<ASTNode> decsAndStatements = new ArrayList<ASTNode>();

        if(check(Kind.KW_VOID)) {
            type = Type.toType("void");
        }
        else if(!check(Kind.TYPE)){
            throw new SyntaxException("Program must have a return type");
        }
        else{
            type=Type.toType(head.getText());
        }
        position++;

        if(check(Kind.IDENT)) {
            name=tokens.get(position).getText();
        }
        else{
            throw new SyntaxException("Program must have a name");
        }
        position++;

        if(!check(Kind.LPAREN)){
            throw new SyntaxException("Expected '(' after program name");
        }
        position++;

        if(check(Kind.TYPE)){
            params.add(nameDef());
            while(check(Kind.COMMA)) {
                position++;
                params.add(nameDef());
            }
        }

        if(!check(Kind.RPAREN)){
            throw new SyntaxException("Expected ')' after parameters");
        }
        position++;

        while(!check(Kind.EOF)){
            if(check(Kind.TYPE, Kind.KW_WRITE, Kind.RETURN, Kind.IDENT)){
                if(check(Kind.TYPE)) decsAndStatements.add(declaration());
                else decsAndStatements.add(statement());
                //running into problems here
                if(!check(Kind.SEMI)){
                    throw new SyntaxException("Expected ;");
                }
                position++;
            }
            else throw new SyntaxException("Syntax error: Unexpected token");
        }
        return new Program(head, type, name, params, decsAndStatements);
    }

    NameDef nameDef() throws SyntaxException{
        if(!check(Kind.TYPE)) throw new SyntaxException("Syntax error: Expected a type");
        IToken head=tokens.get(position);
        Type type=Type.toType(tokens.get(position).getText());
        position++;
        if(check(Kind.IDENT)){
            position++;
            return new NameDef(head, head.getText(), tokens.get(position-1).getText());
        }
        else if(check(Kind.LSQUARE)){
            Dimension d=dimension();
            if(!check(Kind.IDENT)) throw new SyntaxException("Syntax error: expected identifier after dimension");
            position++;
            return new NameDefWithDim(head, head.getText(), tokens.get(position-1).getText(), d);
        }
        else throw new SyntaxException("Invalid name definition");
    }

    Declaration declaration() throws SyntaxException{
        IToken head=tokens.get(position);
        NameDef n=nameDef();
        if(!check(Kind.ASSIGN, Kind.LARROW))
            return new VarDeclaration(head, n, null, null);
        else{
            IToken op=tokens.get(position);
            position++;
            return new VarDeclaration(head, n, op, expression());
        }
    }

    ConditionalExpr conditionalExpression() throws SyntaxException {
        IToken head=tokens.get(position);
        position++;
        if(!check(Kind.LPAREN))
            throw new SyntaxException("Expected '(' after if statement");
        position++;
        Expr condition=expression();
        if(!check(Kind.RPAREN))
            throw new SyntaxException("Expected ')' after conditional expression");
        position++;
        Expr positive=expression();
        if(!check(Kind.KW_ELSE))
            throw new SyntaxException("Expected 'else'");
        position++;
        Expr negative=expression();
        if(!check(Kind.KW_FI))
        throw new SyntaxException("Expected 'fi'");
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
            Expr result=left;
            while(check(Kind.OR)){
                IToken op=tokens.get(position);
                position++;
                Expr right=logicalAndExpression();
                result= new BinaryExpr(head, result, op, right);
            }
            return result;
        }
    }

    Expr logicalAndExpression() throws SyntaxException {
        IToken head = tokens.get(position);
        Expr left=comparisonExpression();
        if(!check(Kind.AND)){
            return left;
        }
        else{
            Expr result=left;
            while(check(Kind.AND)){
                IToken op=tokens.get(position);
                position++;
                Expr right=comparisonExpression();
                result= new BinaryExpr(head, result, op, right);
            }
            return result;
        }
    }

    Expr comparisonExpression() throws SyntaxException {
        IToken head = tokens.get(position);
        Expr left=additiveExpression();
        if(!check(Kind.LT, Kind.GT, Kind.EQUALS, Kind.NOT_EQUALS, Kind.LE, Kind.GE)){
            return left;
        }
        else{
            Expr result=left;
            while(check(Kind.LT, Kind.GT, Kind.EQUALS, Kind.NOT_EQUALS, Kind.LE, Kind.GE)){
                IToken op=tokens.get(position);
                position++;
                Expr right=additiveExpression();
                result= new BinaryExpr(head, result, op, right);
            }
            return result;
        }
    }

    Expr additiveExpression() throws SyntaxException {
        IToken head = tokens.get(position);
        Expr left=multiplicativeExpression();
        if(!check(Kind.PLUS,Kind.MINUS)){
            return left;
        }
        else{
            Expr result=left;
            while(check(Kind.PLUS,Kind.MINUS)){
                IToken op=tokens.get(position);
                position++;
                Expr right=multiplicativeExpression();
                result= new BinaryExpr(head, result, op, right);
            }
            return result;
        }
    }

    Expr multiplicativeExpression() throws SyntaxException {
        IToken head = tokens.get(position);
        Expr left=unaryExpression();
        if(!check(Kind.TIMES,Kind.DIV,Kind.MOD)){
            return left;
        }
        else{
            Expr result=left;
            while(check(Kind.TIMES,Kind.DIV,Kind.MOD)){
                IToken op=tokens.get(position);
                position++;
                Expr right=unaryExpression();
                result= new BinaryExpr(head, result, op, right);
            }
            return result;
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
            case COLOR_CONST -> {
                position++;
                return new ColorConstExpr(t);
            }
            case LANGLE -> {
                position++;
                Expr left = expression();
                if(!check(Kind.COMMA)) throw new SyntaxException("Expected ',' in color expression");
                position++;
                Expr middle=expression();
                if(!check(Kind.COMMA)) throw new SyntaxException("Expected ',' in color expression");
                position++;
                Expr right=expression();
                if(!check(Kind.RANGLE)) throw new SyntaxException("Expected '>>' at end of color expression");
                position++;
                return new ColorExpr(t, left, middle, right);
            }
            case KW_CONSOLE -> {
                position++;
                return new ConsoleExpr(t);
            }
            default -> {
                throw new SyntaxException("Unexpected token in primary expression");
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

    Dimension dimension() throws SyntaxException {
        IToken t=tokens.get(position);
        position++;
        Expr x=expression();
        position++;
        Expr y=expression();
        position++;
        return new Dimension(t, x, y);
    }

    Statement statement() throws SyntaxException{
        IToken head = tokens.get(position);
        if(check(Kind.KW_WRITE)){
            position++;
            Expr source=expression();
            if(!check(Kind.RARROW)) throw new SyntaxException("Expected '->' in write statement");
            position++;
            Expr dest=expression();
            return new WriteStatement(head, source, dest);
        }
        else if(check(Kind.RETURN)){
            position++;
            Expr expr=expression();
            return new ReturnStatement(head, expr);
        }
        else if(check(Kind.IDENT)){
            position++;
            PixelSelector selector=null;
            if(check(Kind.LSQUARE)){
                selector=pixelSelector();
            }
            if(check(Kind.ASSIGN)){
                position++;
                return new AssignmentStatement(head, head.getText(), selector, expression());
            }
            else if(check(Kind.LARROW)){
                position++;
                return new ReadStatement(head, head.getText(), selector, expression());
            }
            else throw new SyntaxException("Unexpected character in statement");

        }
        else throw new SyntaxException("Invalid statement");
    }
    private boolean check(Kind... kinds){
        for(Kind k : kinds){
            if(position<tokens.size() &&tokens.get(position).getKind()==k)
                return true;
        }
        return false;
    }
}
