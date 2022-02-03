package edu.ufl.cise.plc;

public class LexerPrintTest {
    public static void main(String[] args){
        String test= """
                keyword
                123
                 "hi"
                getRed
                123.42
                """;
        Lexer testLexer=new Lexer(test);
        while(testLexer.peek().getKind() != IToken.Kind.EOF){
            Token myToken=testLexer.next();
            System.out.println("Line: " + String.valueOf(myToken.getSourceLocation().line()) + " Column: " +
                    (myToken.getSourceLocation().column()) + " Kind: " + myToken.getKind().name() + " Text: "
                    + myToken.getText());
        }
    }
}
