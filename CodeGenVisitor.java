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
import edu.ufl.cise.plc.CodeGenStringBuilder;

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

        arg = "(" + type + ")" + arg;
        return arg;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {

        arg += (booleanLitExpr.toString());
        return arg;

    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        arg += "\"\"\"";
        arg += (stringLitExpr.getValue());
        arg += "\"\"\"";
        return arg;
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        arg += (String.valueOf(intLitExpr.getValue()));
        Type type = intLitExpr.getCoerceTo() != null ? intLitExpr.getCoerceTo() : intLitExpr.getType();

        if (intLitExpr.getType() != type) {
            genTypeConversion(type, arg);
        }
        return arg;
    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        arg += (Float.toString(floatLitExpr.getValue()));

        Type type = floatLitExpr.getCoerceTo() != null ? floatLitExpr.getCoerceTo() : floatLitExpr.getType();

        if (floatLitExpr.getType() != type) {
            genTypeConversion(type, arg);
        }
        return arg;
    }

    //Assignment 6
    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        imports = "edu.ufl.cise.plc.runtime.ConsoleIO;\n";

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
        arg = arg + ")" + ";" + "\n";




        return arg;


    }

    //Assignment 6
    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        IToken op = unaryExpression.getOp();
        Expr expr = unaryExpression.getExpr();


        arg += (op.getText());
        expr.visit(this, arg);
        arg = arg + ";" + "\n";

        return arg;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        Type type = binaryExpr.getType();
        Expr leftExpr = binaryExpr.getLeft();
        Expr rightExpr = binaryExpr.getRight();
        Type leftType = leftExpr.getCoerceTo() != null ? leftExpr.getCoerceTo() : leftExpr.getType();
        Type rightType = rightExpr.getCoerceTo() != null ? rightExpr.getCoerceTo() : rightExpr.getType();
        Kind op = binaryExpr.getOp().getKind();

        if (leftType == IMAGE || leftType == COLOR || leftType == COLORFLOAT
         || rightType == IMAGE || rightType == COLOR || rightType == COLORFLOAT) {
            throw new UnsupportedOperationException("Not implemented");
        }
        else {
            arg += "(";
            binaryExpr.getLeft().visit(this, arg);
            arg += (binaryExpr.getOp().getText());
            binaryExpr.getRight().visit(this, arg);
            arg += ")";
        }

        if (binaryExpr.getCoerceTo() != type) {
            arg = genTypeConversion(binaryExpr.getCoerceTo(), arg);
        }

        return arg;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {

        arg += (identExpr.getText());

        if (identExpr.getCoerceTo() != null && identExpr.getCoerceTo() != identExpr.getType()) {
            genTypeConversion(identExpr.getCoerceTo(), arg);
        }

        return arg;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {

        Expr condition = conditionalExpr.getCondition();
        Expr trueCase = conditionalExpr.getTrueCase();
        Expr falseCase = conditionalExpr.getFalseCase();

        arg += "(";
        condition.visit(this, arg);
        arg += ")";
        arg += (" ? ");
        trueCase.visit(this, arg);
        arg += (" : ");
        falseCase.visit(this, arg);

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
        arg += (assignmentStatement.getName());
        arg += (" = ");
        Expr expr = assignmentStatement.getExpr();
        expr.visit(this, arg);
        arg = arg + ";" + "\n";
        return arg;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {

        arg += ("ConsoleIO.console.println(");
        Expr source = writeStatement.getSource();
        source.visit(this, arg);
        arg = arg + ");" + "\n";
        return arg;
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        arg += (readStatement.getName());

        arg += (" = ");
        Expr expr = readStatement.getSource();
        expr.visit(this, arg);
        arg = arg + ";" + "\n";
        return arg;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        arg = arg + packageName + ";" + "\n";
        arg = arg + imports + "\n";
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

        arg = arg + Type + " apply(";
        for (int i = 0; i < program.getParams().size(); i++) {
            NameDef nameDef = program.getParams().get(i);
            nameDef.visit(this, arg);
            if (i != program.getParams().size() - 1) {
                arg = arg + ", ";

            }

        }

        arg = arg + ")" + "{" + "\n" + "    ";

        for (int i = 0; i < program.getDecsAndStatements().size(); i++) {
            ASTNode stat = program.getDecsAndStatements().get(i);
            stat.visit(this, arg);
            arg = arg + ";" + "\n" + "    ";
            if (i != program.getDecsAndStatements().size() - 1) {
                arg += ("    ");
            }


        }

        arg = arg + "}" + "\n" + "}";


        return arg;



    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
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
        Expr expr = returnStatement.getExpr();

        arg += ("return ");
        expr.visit(this, arg);
        arg = arg + ";\n";
        return arg;
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {

        NameDef nameDef = declaration.getNameDef();
        nameDef.visit(this, arg);

        if (declaration.getOp() != null) {
            arg += (" = ");
            Expr expr = declaration.getExpr();
            expr.visit(this, arg);
        }

        else {
            arg = arg + ";\n";
        }
        return arg;
    }

    //Assignment 6
    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        return null;
    }
}
