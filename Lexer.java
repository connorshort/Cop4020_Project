package edu.ufl.cise.plc;

import java.util.Map;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.IToken.SourceLocation;
import java.util.HashMap;

public class Lexer implements ILexer {
    private String code;
    private int column=-1;
    private int line=0;
    private int startPos=-1;
    private int pos=-1;

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

    private char nextChar(int position){
        position++;
        if(position >= code.length()) return '\u001a';
        return code.charAt(position);
    }

    public Token next() throws LexicalException {
        pos++;
        column++;
        startPos=pos;
        int startColumn=column;
        if(startPos >= code.length()){
            return new Token(new SourceLocation(line, startColumn), Kind.EOF, "");
        }
        char firstChar=code.charAt(startPos);
        //handle whitespace and comments first
        while(firstChar=='\n' | firstChar=='\t' | firstChar=='\r' | firstChar==' ' | firstChar=='#') {
            if (firstChar == '\n' | firstChar == '\t' | firstChar == '\r' | firstChar == ' ') {
                while (code.charAt(pos) == '\n' | code.charAt(pos) == '\t' | code.charAt(pos) == '\r' | code.charAt(pos) == ' ') {
                    if (code.charAt(pos) == '\n') {
                        column = 0;
                        line++;
                    }
                    else column++;
                    pos++;
                    if(pos >= code.length()){
                        return new Token(new SourceLocation(line, startColumn), Kind.EOF, "");
                    }
                }
                startPos = pos;
                startColumn = column;
                firstChar = code.charAt(startPos);
            }
            else if (firstChar == '#') {
                //POTENTIAL ERROR: SINCE \N ONLY APPEARS AT THE END OF A COMMENT, SHOULD THE WHILE CONDITION BE ALTERED SINCE \N WILL NOT IMMEDIATELY FOLLOW A #?
                while (code.charAt(pos) != '\n') {
                    pos++;
                    if(pos >= code.length()){
                        return new Token(new SourceLocation(line, startColumn), Kind.EOF, "");
                    }
                }
                column = 0;
            }
            startPos = pos;
            startColumn = column;
            firstChar = code.charAt(startPos);
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
                switch(nextChar(startPos)){
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
                switch(nextChar(startPos)){
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
                switch(nextChar(startPos)){
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
                switch(nextChar(startPos)){
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
                while(nextChar(pos) != '"' && nextChar(pos) != '\u001a'){
                    text = text + code.charAt(pos+1);
                    column++;
                    pos++;
                }
                pos++;
                column++;
                text=text + '"';
                return new Token(new SourceLocation(line, startColumn), Kind.STRING_LIT, text, code.substring(startPos+1,pos));
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
                while(Character.isDigit(nextChar(pos))){
                    column++;
                    pos++;
                }
                if(nextChar(pos) != '.'){
                    try {
                        return new Token(new SourceLocation(line, startColumn), Kind.INT_LIT, code.substring(startPos, pos + 1), Integer.parseInt(code.substring(startPos, pos + 1)));
                    }
                    catch(Exception E){
                        throw new LexicalException("Improper integer value");
                    }
                }
                else{
                    column++;
                    pos++;
                    while(Character.isDigit(nextChar(pos))){
                        column++;
                        pos++;
                    }
                    return new Token(new SourceLocation(line, startColumn), Kind.FLOAT_LIT, code.substring(startPos,pos+1),Float.parseFloat(code.substring(startPos,pos+1)));
                }
            }
            case 0:
                return new Token(new SourceLocation(line, startColumn), Kind.EOF, "");
            //all other cases
            default:{
                //handle identifiers and keywords
                //TODO: FINISH THIS
                String idenStart="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$_";
                String idenChars=idenStart+"1234567890";
                //POTENTIAL ERROR: DID YOU MEAN TO PUT IDENSTART IN SINGLE QUOTES?
                if(idenStart.contains(String.valueOf(firstChar))){
                    while(idenChars.contains(String.valueOf(nextChar(pos)))){
                        column++;
                        pos++;
                    }
                }
                else{
                    throw new LexicalException("Unexpected character");
                }

                //POTENTIAL ERROR: THIS IS WHAT VISHWA WROTE. MAKE SURE THERE'S NOTHING WRONG HERE.
                String text=code.substring(startPos, pos+1);
                if (text.equals("string") || text.equals("int") || text.equals("float") || text.equals("boolean") || text.equals("color") || text.equals("image") || text.equals("void")) {
                    return new Token(new SourceLocation(line, startColumn), Kind.TYPE, text);
                }

                else if (text.equals("getWidth") || text.equals("getHeight")) {
                    return new Token(new SourceLocation(line, startColumn), Kind.IMAGE_OP, text);
                }

                else if (text.equals("getRed") || text.equals("getGreen") || text.equals("getBlue")) {
                    return new Token(new SourceLocation(line, startColumn), Kind.COLOR_OP, text);
                }

                else if (text.equals("BLACK") || text.equals("BLUE") || text.equals("CYAN") || text.equals("DARK_GRAY") ||
                        text.equals("GRAY") || text.equals("GREEN") || text.equals("LIGHT_GRAY") || text.equals("MAGENTA") ||
                        text.equals("ORANGE") || text.equals("PINK") || text.equals("RED") || text.equals("WHITE") || text.equals("YELLOW")) {
                    return new Token(new SourceLocation(line, startColumn), Kind.COLOR_CONST, text);
                }

                else if (text.equals("true") || text.equals("false")) {
                    return new Token(new SourceLocation(line, startColumn), Kind.BOOLEAN_LIT, text);
                }

                else if (text.equals("if")) {
                    return new Token(new SourceLocation(line, startColumn), Kind.KW_IF, text);
                }

                else if (text.equals("else")) {
                    return new Token(new SourceLocation(line, startColumn), Kind.KW_ELSE, text);
                }

                else if (text.equals("fi")) {
                    return new Token(new SourceLocation(line, startColumn), Kind.KW_FI, text);
                }

                else if (text.equals("write")) {
                    return new Token(new SourceLocation(line, startColumn), Kind.KW_WRITE, text);
                }

                else if (text.equals("console")) {
                    return new Token(new SourceLocation(line, startColumn), Kind.KW_CONSOLE, text);
                }
                return new Token(new SourceLocation(line, startColumn), Kind.IDENT, text);

            }

        }
    }
    public Token peek() throws LexicalException{
        int tempColumn=column;
        int tempPos=pos;
        int tempStartPos=startPos;
        int tempLine=line;
        Token t=this.next();
        column=tempColumn;
        pos=tempPos;
        startPos=tempStartPos;
        line=tempLine;
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