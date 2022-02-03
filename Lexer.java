package edu.ufl.cise.plc;

import java.util.Map;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.IToken.SourceLocation;
import java.util.HashMap;

public class Lexer implements ILexer {
    private String code;
    private int column=0;
    private int line=0;
    private int startPos=0;
    private int pos=0;

    private static final Map<Character, Kind> singleChars;

    static{
        singleChars=new HashMap<>();
        singleChars.put(')', Kind.RPAREN);
        singleChars.put('(', Kind.LPAREN);
        singleChars.put('[', Kind.LSQUARE);
        singleChars.put(']', Kind.RSQUARE);
        singleChars.put('+', Kind.PLUS);
        singleChars.put('*', Kind.TIMES);
        singleChars.put('/', Kind.DIV);
        singleChars.put('%', Kind.MOD);
        singleChars.put('&', Kind.AND);
        singleChars.put('|', Kind.OR);
        singleChars.put(';', Kind.SEMI);
        singleChars.put(',', Kind.COMMA);
        singleChars.put('^', Kind.RETURN);
    }

    public Lexer(String input){
        this.code=input;
    }

    public Token next(){
        pos++;
        column++;
        startPos=pos;
        int startColumn=column;
        char firstChar=code.charAt(startPos);
        //handle whitespace and comments first
        while(firstChar=='\n' | firstChar=='\t' | firstChar=='\r' | firstChar==' ' | firstChar=='#')
            if(firstChar=='\n' | firstChar=='\t' | firstChar=='\r' | firstChar==' ' ){
                while(code.charAt(pos)=='\n' | code.charAt(pos)=='\t' | code.charAt(pos)=='\r' | code.charAt(pos)==' ' ){
                    if(code.charAt(pos)=='\n'){
                        column=0;
                        line++;
                    }
                    else column++;
                    pos++;
                }
                startPos=pos;
                startColumn=column;
                firstChar=code.charAt(startPos);
            }
            if(firstChar=='#'){
                //POTENTIAL ERROR: SINCE \N ONLY APPEARS AT THE END OF A COMMENT, SHOULD THE WHILE CONDITION BE ALTERED SINCE \N WILL NOT IMMEDIATELY FOLLOW A #?
                while(code.charAt(pos)!='\n'){
                    pos++;
                }
                column=0;
                line++;
                startPos=pos;
                startColumn=column;
                firstChar=code.charAt(startPos);
            }
        //generate token now that whitespace and comments are ignored
        switch(firstChar){
            //handle single char tokens with no other possibilities
            case '(':
            case ')':
            case '[':
            case ']':
            case '+':
            case '*':
            case '/':
            case '%':
            case '&':
            case '|':
            case ';':
            case ',':
            case '^':{
                return new Token(new SourceLocation(line, startColumn), singleChars.get(firstChar), String.valueOf(firstChar));
            }
            //handle all double char possibilities
            case '<':{
                switch(code.charAt(startPos+1)){
                    case '<': {
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.LANGLE, "<<");
                    }
                    case '=':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.LE, "<=");
                    }
                    case'-':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.LARROW, "<-");
                    }
                    default:{
                        return new Token(new SourceLocation(line, startColumn), Kind.LT, String.valueOf(firstChar));
                    }
                }
            }
            case '>':{
                switch(code.charAt(startPos+1)){
                    case '>':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.RANGLE, ">>");
                    }
                    case '=':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.GE, ">=");
                    }                
                    default:{
                        return new Token(new SourceLocation(line, startColumn), Kind.GT, String.valueOf(firstChar));
                    }
                }
            }
            case '-':{
                switch(code.charAt(startPos+1)){
                    case '>':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.RARROW, "->");
                    }
                    default:{
                        return new Token(new SourceLocation(line, startColumn), Kind.MINUS, String.valueOf(firstChar));
                    }
                }
            }
            case '=':{
                switch(code.charAt(startPos+1)){
                    case '=':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.EQUALS, "==");
                    }
                    default:{
                        return new Token(new SourceLocation(line, startColumn), Kind.ASSIGN, String.valueOf(firstChar));
                    }
                }
            }
            //handle string literal
            case '"':{
                String text="\"";
                while(code.charAt(pos+1) != '"'){
                    text = text + code.charAt(pos+1);
                    column++;
                    pos++;
                }
                pos++;
                column++;
                text=text + '"';
                return new Token(new SourceLocation(line, startColumn), Kind.STRING_LIT, text, code.substring(startPos+1,pos-1));
            }

            //handle numeric literals
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':{
                while(Character.isDigit(code.charAt(pos+1))){
                    column++;
                    pos++;
                }
                if(code.charAt(pos+1) != '.'){
                    return new Token(new SourceLocation(line, startColumn), Kind.INT_LIT, code.substring(startPos,pos),Integer.parseInt(code.substring(startPos,pos)));
                }
                else{
                    while(Character.isDigit(code.charAt(pos+1))){
                        column++;
                        pos++;
                    }
                    return new Token(new SourceLocation(line, startColumn), Kind.INT_LIT, code.substring(startPos,pos),Float.parseFloat(code.substring(startPos,pos)));
                }
            }
            //all other cases
            default:{
                //handle identifiers and keywords
                //TODO: FINISH THIS
                String idenStart="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$_";
                String idenChars=idenStart+"1234567890";
                //POTENTIAL ERROR: DID YOU MEAN TO PUT IDENSTART IN SINGLE QUOTES?
                if(idenStart.contains(String.valueOf(firstChar))){
                    while(idenChars.contains(String.valueOf(code.charAt(pos+1)))){
                        column++;
                        pos++;
                    }
                }

                //POTENTIAL ERROR: THIS IS WHAT VISHWA WROTE. MAKE SURE THERE'S NOTHING WRONG HERE.

                if (code.substring(startPos, pos) == "string" || code.substring(startPos, pos) == "int" || code.substring(startPos, pos) == "float" || code.substring(startPos, pos) == "boolean" || code.substring(startPos, pos) == "color" || code.substring(startPos, pos) == "image" || code.substring(startPos, pos) == "void") {
                    return new Token(new SourceLocation(line, startColumn), Kind.TYPE, code.substring(startPos, pos));
                }

                else if (code.substring(startPos, pos) == "getWidth" || code.substring(startPos, pos) == "getHeight") {
                    return new Token(new SourceLocation(line, startColumn), Kind.IMAGE_OP, code.substring(startPos, pos));
                }

                else if (code.substring(startPos, pos) == "getRed" || code.substring(startPos, pos) == "getGreen" || code.substring(startPos, pos) == "getBlue") {
                    return new Token(new SourceLocation(line, startColumn), Kind.COLOR_OP, code.substring(startPos, pos));
                }

                else if (code.substring(startPos, pos) == "BLACK" || code.substring(startPos, pos) == "BLUE" || code.substring(startPos, pos) == "CYAN" || code.substring(startPos, pos) == "DARK_GRAY" ||
                        code.substring(startPos, pos) == "GRAY" || code.substring(startPos, pos) == "GREEN" || code.substring(startPos, pos) == "LIGHT_GRAY" || code.substring(startPos, pos) == "MAGENTA" ||
                        code.substring(startPos, pos) == "ORANGE" || code.substring(startPos, pos) == "PINK" || code.substring(startPos, pos) == "RED" || code.substring(startPos, pos) == "WHITE" || code.substring(startPos, pos) == "YELLOW") {
                    return new Token(new SourceLocation(line, startColumn), Kind.COLOR_CONST, code.substring(startPos, pos));
                }

                else if (code.substring(startPos, pos) == "true" || code.substring(startPos, pos) == "false") {
                    return new Token(new SourceLocation(line, startColumn), Kind.BOOLEAN_LIT, code.substring(startPos, pos));
                }

                else if (code.substring(startPos, pos) == "if") {
                    return new Token(new SourceLocation(line, startColumn), Kind.KW_IF, code.substring(startPos, pos));
                }

                else if (code.substring(startPos, pos) == "else") {
                    return new Token(new SourceLocation(line, startColumn), Kind.KW_ELSE, code.substring(startPos, pos));
                }

                else if (code.substring(startPos, pos) == "fi") {
                    return new Token(new SourceLocation(line, startColumn), Kind.KW_FI, code.substring(startPos, pos));
                }

                else if (code.substring(startPos, pos) == "write") {
                    return new Token(new SourceLocation(line, startColumn), Kind.KW_WRITE, code.substring(startPos, pos));
                }

                else if (code.substring(startPos, pos) == "console") {
                    return new Token(new SourceLocation(line, startColumn), Kind.KW_CONSOLE, code.substring(startPos, pos));
                }
                return new Token(new SourceLocation(line, startColumn), Kind.IDENT, code.substring(startPos, pos));

            }

        }
    }
    public Token peek(){
        int tempColumn=column;
        int tempPos=pos;
        int tempStartPos=startPos;
        Token t=this.next();
        column=tempColumn;
        pos=tempPos;
        startPos=tempStartPos;
        if(t.getKind()==Kind.BOOLEAN_LIT)
            return new Token(t.getSourceLocation(), t.getKind(), t.getText(), t.getBooleanValue());
        else if(t.getKind()==Kind.STRING_LIT)
            return new Token(t.getSourceLocation(), t.getKind(), t.getText(), t.getStringValue());
        else if(t.getKind()==Kind.FLOAT_LIT)
            return new Token(t.getSourceLocation(), t.getKind(), t.getText(), t.getFloatValue());
        else if(t.getKind()==Kind.INT_LIT)
            return new Token(t.getSourceLocation(), t.getKind(), t.getText(), t.getIntValue());
        else return new Token(t.getSourceLocation(), t.getKind(), t.getText());
    }

}