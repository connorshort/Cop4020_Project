package edu.ufl.cise.plc;

import java.util.Map;

import edu.ufl.cise.plc.ILexer;
import edu.ufl.cise.plc.Token;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.SourceLocation;

public class Lexer extends ILexer{
    private String code;
    private int column=0;
    private int line=0;
    private int startPos=0;
    private int pos=0;

    private static final Map<Char, Kind> singleChars;
    private static final Map<String, Kind> reserved;

    static{
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

    public Lexer(string input){
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
                while(charAt(pos)=='\n' | charAt(pos)=='\t' | charAt(pos)=='\r' | charAt(pos)==' ' ){
                    if(charAt(pos)=='\n'){
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
                while(charAt(pos)!='\n'){
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
                return new Token(new SourceLocation(line, startColumn), singleChars.get(firstChar), firstChar);
            }
            //handle all double char possibilities
            case '<':{
                switch(code.CharAt(startPos+1)){
                    case '<': {
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.LANGLE, '<<');
                    }
                    case '=':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.LE, '<=');
                    }
                    case'-':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.LARROW, '<-');
                    }
                    default:{
                        return new Token(new SourceLocation(line, startColumn), Kind.LT, firstChar);
                    }
                }
            }
            case '>':{
                switch(code.CharAt(startPos+1)){
                    case '>':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.RANGLE, '>>');
                    }
                    case '=':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.GE, '>=');
                    }                
                    default:{
                        return new Token(new SourceLocation(line, startColumn), Kind.GT, firstChar);
                    }
                }
            }
            case '-':{
                switch(code.CharAt(startPos+1)){
                    case '>':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.RARROW, '->');
                    }
                    default:{
                        return new Token(new SourceLocation(line, startColumn), Kind.MINUS, firstChar);
                    }
                }
            }
            case '=':{
                switch(code.CharAt(startPos+1)){
                    case '=':{
                        pos++;
                        column++;
                        return new Token(new SourceLocation(line, startColumn), Kind.EQUALS, '==');
                    }
                    default:{
                        return new Token(new SourceLocation(line, startColumn), Kind.ASSIGN, firstChar);
                    }
                }
            }
            //handle string literal
            case '"':{
                String text='"';
                while(code.CharAt(pos+1) != '"'){
                    text = text + code.CharAt(pos+1);
                    column++;
                    pos++;
                }
                pos++;
                column++;
                text=text + '"';
                return new Token(new SourceLocation(line, startColumn), Kind.STRING_LIT, text, code.subsstring(startPos+1,pos-1);
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
                    return new Token(new SourceLocation(line, startColumn), Kind.INT_LIT, code.subsstring(startPos,pos),Integer.parseInt(code.subsstring(startPos,pos));
                }
                else{
                    while(Character.isDigit(code.charAt(pos+1))){
                        column++;
                        pos++;
                    }
                    return new Token(new SourceLocation(line, startColumn), Kind.INT_LIT, code.subsstring(startPos,pos),Float.parseFloat(code.subsstring(startPos,pos));
                }
            }
            //all other cases
            default:{
                //handle identifiers and keywords
                //TODO: FINISH THIS
                String idenStart='abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$_'
                String idenChars=idenStart+'1234567890'
                //POTENTIAL ERROR: DID YOU MEAN TO PUT IDENSTART IN SINGLE QUOTES?
                if(idenStart.contains(firstChar)){
                    while(idenChars.contains(code.charAt(pos+1)){
                        column++;
                        pos++;
                    }
                }

                //POTENTIAL ERROR: THIS IS WHAT VISHWA WROTE. MAKE SURE THERE'S NOTHING WRONG HERE.

                if (code.subsstring(startPos, pos) == "string" || code.subsstring(startPos, pos) == "int" || code.subsstring(startPos, pos) == "float" || code.subsstring(startPos, pos) == "boolean" || code.subsstring(startPos, pos) == "color" || code.subsstring(startPos, pos) == "image" || code.subsstring(startPos, pos) == "void") {
                    return new Token(new SourceLocation(line, startColumn), Kind.TYPE, code.subsstring(startPos, pos));
                }

                else if (code.subsstring(startPos, pos) == "getWidth" || code.subsstring(startPos, pos) == "getHeight") {
                    return new Token(new SourceLocation(line, startColumn), KIND.IMAGE_OP, code.subsstring(startPos, pos));
                }

                else if (code.subsstring(startPos, pos) == "getRed" || code.subsstring(startPos, pos) == "getGreen" || code.subsstring(startPos, pos) == "getBlue") {
                    return new Token(new SourceLocation(line, startColumn), KIND.COLOR_OP, code.subsstring(startPos, pos));
                }

                else if (code.subsstring(startPos, pos) == "BLACK" || code.subsstring(startPos, pos) == "BLUE" || code.subsstring(startPos, pos) == "CYAN" || code.subsstring(startPos, pos) == "DARK_GRAY" ||
                        code.subsstring(startPos, pos) == "GRAY" || code.subsstring(startPos, pos) == "GREEN" || code.subsstring(startPos, pos) == "LIGHT_GRAY" || code.subsstring(startPos, pos) == "MAGENTA" ||
                        code.subsstring(startPos, pos) == "ORANGE" || code.subsstring(startPos, pos) == "PINK" || code.subsstring(startPos, pos) == "RED" || code.subsstring(startPos, pos) == "WHITE" || code.subsstring(startPos, pos) == "YELLOW") {
                    return new Token(new SourceLocation(line, startColumn), KIND.COLOR_CONST, code.subsstring(startPos, pos));
                }

                else if (code.subsstring(startPos, pos) == "true" || code.subsstring(startPos, pos) == "false") {
                    return new Token(new SourceLocation(line, startColumn), KIND.BOOLEAN_LIT, code.subsstring(startPos, pos));
                }

                else if (code.subsstring(startPos, pos) == "if") {
                    return new Token(new SourceLocation(line, startColumn), KIND.KW_IF, code.subsstring(startPos, pos));
                }

                else if (code.subsstring(startPos, pos) == "else") {
                    return new Token(new SourceLocation(line, startColumn), KIND.KW_ELSE, code.subsstring(startPos, pos));
                }

                else if (code.subsstring(startPos, pos) == "fi") {
                    return new Token(new SourceLocation(line, startColumn), KIND.KW_FI, code.subsstring(startPos, pos));
                }

                else if (code.subsstring(startPos, pos) == "write") {
                    return new Token(new SourceLocation(line, startColumn), KIND.KW_WRITE, code.subsstring(startPos, pos));
                }

                else if (code.subsstring(startPos, pos) == "console") {
                    return new Token(new SourceLocation(line, startColumn), KIND.KW_CONSOLE, code.subsstring(startPos, pos));
                }
                return new Token(new SourceLocation(line, startColumn), KIND.IDENT, code.subsstring(startPos, pos));

            }

        }
    }
    public Token peek(){
        tempColumn=column;
        tempPos=pos;
        tempStartPos=startPos;
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