package edu.ufl.cise.plc;

import java.util.List;
import java.util.Map;
import java.util.Set;


import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;

import edu.ufl.cise.plc.runtime.ConsoleIO;

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class CodeGenVisitor implements ASTVisitor {
    String packageName = "";
    String imports = "";


    public CodeGenVisitor(String packageName) {
        this.packageName = packageName;

    }

    public Object genTypeConversion(Type coerce, Object arg) {
        String type = "";
        if (coerce == INT) {
            type = "int";
        }
        else if (coerce == FLOAT) {
            type = "float";
        }
        else if (coerce == STRING) {
            type = "String";
        }
        else if (coerce == BOOLEAN) {
            type = "boolean";
        }

        else {
            return arg;
        }


        arg = "(" + type + ")" + arg;
        return arg;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        System.out.println("visitBooleanLitExpr");
        String kind = "";

        if (booleanLitExpr.getValue() == true) {
            kind = "true";
        }
        else if (booleanLitExpr.getValue() == false) {
            kind = "false";
        }
        else {
            throw new IllegalArgumentException("Neither true or false.");
        }

        arg += kind;



        return arg;

    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        System.out.println("visitStringLitExpr");

        arg += "\"";
        arg += stringLitExpr.getValue();
        arg += "\"";
        return arg;
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        System.out.println("visitIntLitExpr");
        Object arg2 = String.valueOf(intLitExpr.getValue());
        Type type = intLitExpr.getCoerceTo() != null ? intLitExpr.getCoerceTo() : intLitExpr.getType();

        if (intLitExpr.getType() != type) {
            arg2 = genTypeConversion(type, arg2);
        }
        arg += (String) arg2;
        return arg;
    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        System.out.println("visitFloatLitExpr");
        Object arg2 = (Float.toString(floatLitExpr.getValue()));
        arg2 += "f";

        Type type = floatLitExpr.getCoerceTo() != null ? floatLitExpr.getCoerceTo() : floatLitExpr.getType();

        if (floatLitExpr.getType() != type) {
            arg2 = genTypeConversion(type, arg2);
        }
        arg += (String) arg2;
        return arg;
    }

    //Assignment 6
    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        System.out.println("visitConsoleExpr");
        imports = "import edu.ufl.cise.plc.runtime.ConsoleIO;\n";

        String Type = "";
        String Type2 = "";

        if (consoleExpr.getCoerceTo() == INT) {
            Type = "Integer";
            Type2 = "INT";
        }
        else if (consoleExpr.getCoerceTo() == BOOLEAN) {
            Type = "Boolean";
            Type2 = "BOOLEAN";
        }
        else if (consoleExpr.getCoerceTo() == FLOAT) {
            Type = "Float";
            Type2 = "FLOAT";
        }
        else if (consoleExpr.getCoerceTo() == STRING) {
            Type = "String";
            Type2 = "STRING";

        }
        else {
            throw new IllegalArgumentException("Compiler bug Unexpected value: " + consoleExpr.getCoerceTo());
        }

        arg = arg + "(" + Type + ")" + "\n";
        arg += "ConsoleIO.readValueFromConsole(";
        arg = arg + "\"" + Type2 + "\"" + ", " + "\"";
        arg = arg + "Enter " + Type + ":" + "\"";
        arg = arg + ")" + "\n";




        return arg;


    }

    //Assignment 6
    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        System.out.println("visitUnaryExpr");
        IToken op = unaryExpression.getOp();
        Expr expr = unaryExpression.getExpr();
        Object arg2 = op.getText();
        arg2 = expr.visit(this, arg2);
        arg2 = "(" + arg2 + ")";


        arg += (String)arg2;



        return arg;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        System.out.println("visitBinaryExpr");
        Object arg2 = "";
        Type type = binaryExpr.getType();
        Expr leftExpr = binaryExpr.getLeft();
        Expr rightExpr = binaryExpr.getRight();
        Type leftType = leftExpr.getCoerceTo() != null ? leftExpr.getCoerceTo() : leftExpr.getType();
        Type rightType = rightExpr.getCoerceTo() != null ? rightExpr.getCoerceTo() : rightExpr.getType();
        Kind op = binaryExpr.getOp().getKind();
        if (op == Kind.EQUALS || op == Kind.NOT_EQUALS) {
            if (leftExpr.getType() == STRING) {
                if (op == Kind.NOT_EQUALS) {
                    arg2 += "!";
                }
                arg2 += leftExpr.getText();
                arg2 += ".equals(";
                arg2 += rightExpr.getText();
                arg2 += ")";
                arg += (String) arg2;
                return arg;

            }

        }

        if (leftType == IMAGE || leftType == COLOR || leftType == COLORFLOAT
                || rightType == IMAGE || rightType == COLOR || rightType == COLORFLOAT) {
            throw new UnsupportedOperationException("Not implemented");
        }
        else {
            arg2 = "(";
            arg2 = binaryExpr.getLeft().visit(this, arg2);
            arg2 += (binaryExpr.getOp().getText());
            arg2 = binaryExpr.getRight().visit(this, arg2);
            arg2 += ")";
        }

        if (leftType != rightType) {
            arg2 = genTypeConversion(leftType, arg2);


        }
        arg += (String)arg2;
        return arg;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        System.out.println("visitIdentExpr");
        arg += (identExpr.getText());

        if (identExpr.getCoerceTo() != null && identExpr.getCoerceTo() != identExpr.getType()) {
            //genTypeConversion(identExpr.getCoerceTo(), arg);
        }

        return arg;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
        System.out.println("visitConditionalExpr");
        Expr condition = conditionalExpr.getCondition();
        Expr trueCase = conditionalExpr.getTrueCase();
        Expr falseCase = conditionalExpr.getFalseCase();

        arg += "(";
        arg += "(";
        arg = condition.visit(this, arg);
        arg += ")";
        arg += (" ? ");
        arg = trueCase.visit(this, arg);
        arg += (" : ");
        arg = falseCase.visit(this, arg);
        arg += ")";

        return arg;
    }

    //Assignment 6
    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        return null;
    }

    //Assignment 6
    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        System.out.println("visitAssignmentStatement");
        Object arg2 = (assignmentStatement.getName());
        arg2 += (" = ");
        Expr expr = assignmentStatement.getExpr();
        if (expr.getType() != expr.getCoerceTo()) {
            Type coerce = expr.getCoerceTo();
            String type = "";
            if (coerce == INT) {
                type = "int";
            }
            else if (coerce == FLOAT) {
                type = "float";
            }
            else if (coerce == STRING) {
                type = "String";
            }
            else if (coerce == BOOLEAN) {
                type = "boolean";
            }

            if (type != "") {
                arg2 += "(" + type + ")";
            }
        }
        arg2 = expr.visit(this, arg2);
        arg2 = arg2 + ";" + "\n";
        arg += (String)arg2;
        return arg;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        System.out.println("visitWriteStatement");
        arg += ("ConsoleIO.console.println(");
        Expr source = writeStatement.getSource();
        arg = source.visit(this, arg);
        arg = arg + ");" + "\n";
        return arg;
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        System.out.println("visitReadStatement");
        arg += (readStatement.getName());

        arg += (" = ");
        Expr expr = readStatement.getSource();
        arg = expr.visit(this, arg);
        arg = arg + ";" + "\n";
        return arg;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        System.out.println("visitProgram");
        arg = "";
        arg = arg + "public class " + program.getName() + "{" + "\n";
        arg = arg + "    " + "public static ";

        String Type = "";

        if (program.getReturnType() == VOID) {
            Type = "void";
        }
        else if (program.getReturnType() == INT) {
            Type = "int";
        }
        else if (program.getReturnType() == FLOAT) {
            Type = "float";
        }
        else if (program.getReturnType() == BOOLEAN) {
            Type = "boolean";
        }
        else if (program.getReturnType() == STRING) {
            Type = "String";
        }
        else {
            throw new IllegalArgumentException("Compiler bug Unexpected value: " + program.getReturnType());
        }

        arg = arg + Type + " apply (";
        for (int i = 0; i < program.getParams().size(); i++) {
            NameDef nameDef = program.getParams().get(i);
            arg = nameDef.visit(this, arg);
            if (i != program.getParams().size() - 1) {
                arg = arg + ", ";

            }

        }

        arg = arg + ")" + "{" + "\n" + "        ";

        for (int i = 0; i < program.getDecsAndStatements().size(); i++) {
            ASTNode stat = program.getDecsAndStatements().get(i);
            arg = (String)stat.visit(this, arg);

            arg += "\n";

            if (i != program.getDecsAndStatements().size() - 1) {
                arg += ("        ");
            }


        }

        arg = arg + "    " + "}" + "\n" + "}";

        arg = "package " + packageName + ";" + "\n" + imports + "\n" + arg;

        return arg;



    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
        System.out.println("visitNameDef");
        String Type = "";
        if (nameDef.getType() == INT) {
            Type = "int";
        }
        else if (nameDef.getType() == FLOAT) {
            Type = "float";
        }
        else if (nameDef.getType() == BOOLEAN) {
            Type = "boolean";
        }
        else if (nameDef.getType() == STRING) {
            Type = "String";
        }
        else {
            throw new IllegalArgumentException("Compiler bug Unexpected value: " + nameDef.getType());
        }
        arg += (Type);
        arg += (" ");
        arg += (nameDef.getName());
        return arg;
    }

    //Assignment 6
    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        System.out.println("visitReturnStatement");
        Expr expr = returnStatement.getExpr();

        arg += ("return ");
        arg = expr.visit(this, arg);
        arg = arg + ";\n";
        return arg;
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        System.out.println("visitVarDeclaration");
        Object arg2 = "";
        NameDef nameDef = declaration.getNameDef();
        arg2 = nameDef.visit(this, arg2);
        String Type = "";
        if (declaration.getOp() != null) {
            arg2 += " = ";
            if (nameDef.getType() != declaration.getExpr().getType()) {

                Type coerce = nameDef.getType();

                if (coerce == INT) {
                    Type = "int";
                }
                else if (coerce == FLOAT) {
                    Type = "float";
                }
                else if (coerce == STRING) {
                    Type = "String";
                }
                else if (coerce == BOOLEAN) {
                    Type = "boolean";
                }



            }

            if (Type != "") {
                arg2 += "(" + Type + ")";
            }
            Expr expr = declaration.getExpr();

            arg2 = expr.visit(this, arg2);
        }


        arg2 = arg2 + ";\n";

        arg += (String) arg2;
        return arg;
    }

    //Assignment 6
    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        return null;
    }

}




