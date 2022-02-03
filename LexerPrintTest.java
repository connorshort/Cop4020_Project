package edu.ufl.cise.plc;

public class LexerPrintTest {
    public static void main(String[] args) throws LexicalException {
        String test= """
                keyword
                123
                 "hi"
                getRed
                123.42
                
                #this is a comment
                boolean cheese 432abcd""";
        Lexer testLexer=new Lexer(test);
        do{
            Token myToken=testLexer.next();
            System.out.println("Line: " + String.valueOf(myToken.getSourceLocation().line()) + " Column: " +
                    (myToken.getSourceLocation().column()) + " Kind: " + myToken.getKind().name() + " Text: "
                    + myToken.getText());
        }while(testLexer.peek().getKind() != IToken.Kind.EOF);
    }
}
