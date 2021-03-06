package edu.ufl.cise.plc;

import java.util.*;


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
//import jdk.incubator.foreign.FunctionDescriptor;


import static edu.ufl.cise.plc.ast.Types.Type.*;

public class CodeGenVisitor implements ASTVisitor {
    String packageName = "";
    Set<String> Imports = new HashSet<String>();
    String file = "";


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
        String arg2 = "\"\"\"\n";
        arg2 = arg2 + stringLitExpr.getValue();
        arg2 += "\"\"\"";
        arg += (String)arg2;

        return arg;

    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
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
        Object arg2 = (Float.toString(floatLitExpr.getValue()));
        arg2 += "f";

        Type type = floatLitExpr.getCoerceTo() != null ? floatLitExpr.getCoerceTo() : floatLitExpr.getType();

        if (floatLitExpr.getType() != type) {
            arg2 = genTypeConversion(type, arg2);
        }
        arg += (String) arg2;
        return arg;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        if (identExpr.getCoerceTo() == IMAGE) {
            Imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
            arg += "ImageOps.clone(";
        }
        else if (identExpr.getCoerceTo() == INT && identExpr.getType() == COLOR) {
            arg += (identExpr.getText());
            arg += ".pack()";
            return arg;
        }

        else if (identExpr.getCoerceTo() == COLOR && identExpr.getType() == INT) {
            arg += "new ColorTuple(";
            arg += identExpr.getText();
            arg += ")";
            return arg;
        }
        arg += (identExpr.getText());

        return arg;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
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

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
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
        else if (program.getReturnType() == COLOR) {
            Type = "ColorTuple";
        }
        else if (program.getReturnType() == IMAGE) {
            Type = "BufferedImage";
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

        Object arg3 = "package " + packageName + ";" + "\n";

        Iterator<String> itr = Imports.iterator();

        while (itr.hasNext()) {
            arg3 += itr.next();
        }

        arg = arg3 + "\n" + arg;

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
        else if (nameDef.getType() == IMAGE) {
            Type = "BufferedImage";

        }
        else if (nameDef.getType() == COLOR) {
            Type = "ColorTuple";
        }
        else {
            throw new IllegalArgumentException("Compiler bug Unexpected value: " + nameDef.getType());
        }
        arg += (Type);
        arg += (" ");
        arg += (nameDef.getName());
        return arg;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        Expr expr = returnStatement.getExpr();

        arg += ("return ");
        arg = expr.visit(this, arg);
        arg = arg + ";\n";
        return arg;
    }

    //Assignment 6
    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        Object arg2 = "";
        NameDef nameDef = declaration.getNameDef();

        arg2 = nameDef.visit(this, arg2);

        String Type = "";

        if (declaration.getOp() != null) {
            if (declaration.getOp().getKind() == Kind.ASSIGN) {
                if (declaration.getNameDef().getType() == IMAGE && declaration.getExpr().getType() == IMAGE) {
                    if (declaration.getDim() != null) {
                        Imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
                        arg2 += " = ImageOps.resize(";
                        arg2 = declaration.getExpr().visit(this, arg2);
                        arg2 += ", ";
                        arg2 = declaration.getDim().visit(this, arg2);
                        arg2 += ");";
                        arg += (String)arg2;
                        return arg;

                    }
                }
                arg2 += " = ";
                arg2 = declaration.getExpr().visit(this, arg2);
                arg += (String)arg2;
                arg += ";\n";
                return arg;
            }

            else if (declaration.getOp().getKind() == Kind.LARROW && declaration.getExpr().getType() == STRING && declaration.getNameDef().getType() != IMAGE) {
                Imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                arg2 += " = ";
                Type type = declaration.getNameDef().getType();

                if (type == STRING) {
                    arg2 += "(String) ";
                }
                else if (type == INT) {
                    arg2 += "(int) ";
                }

                else if (type == FLOAT) {
                    arg2 += "(float) ";
                }

                else if (type == COLOR) {
                    arg2 += "(ColorTuple)";
                }

                else if (type == IMAGE) {
                    arg2 += "(BufferedImage)";
                }

                else if (type == BOOLEAN) {
                    arg2 += "(boolean)";
                }

                arg2 += "FileURLIO.readValueFromFile(";

                arg2 += declaration.getExpr().getText();
                arg += (String)arg2;
                arg += ");\n";
                return arg;
            }

        }



        if (nameDef.getType() == IMAGE) {
            Imports.add("import java.awt.image.BufferedImage;\n");

            if (declaration.getExpr() != null) {

                Imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                if (nameDef.getDim() != null) {
                    arg2 += "= FileURLIO.readImage(";

                    arg2 = declaration.getExpr().visit(this, arg2);
                    arg2 += ", ";
                    arg2 = declaration.getDim().visit(this, arg2);
                    arg2 += ");\n";
                    arg += (String)arg2;
                    return arg;
                }
                else {
                    arg2 += "= FileURLIO.readImage(";
                    //arg2 += " = ImageOps.";
                    arg2 = declaration.getExpr().visit(this, arg2);
                    arg2 += ");\n";

                    arg += (String)arg2;
                    return arg;
                }
            }

            else {

                if (nameDef.getDim() != null) {
                    arg2 += " = new BufferedImage(";
                    arg2 = declaration.getDim().visit(this, arg2);
                    arg2 += ", BufferedImage.TYPE_INT_RGB);\n";
                    arg += (String)arg2;
                    return arg;
                }
                else {

                    //Should've thrown an exception beforehand.
                }
            }
        }

        if (declaration.getOp() != null) {
            arg2 += " = ";


            if (nameDef.getType() != declaration.getExpr().getType() && nameDef.getType() != IMAGE) {

                Type coerce = nameDef.getType();

                if (coerce == INT) {
                    Type = "int";
                    if (declaration.getExpr().getType() == STRING) {
                        arg2 += "(" + Type + ") " + "FileURLIO.readValueFromFile(";
                        arg2 = declaration.getExpr().visit(this, arg2);
                        arg2 += ");";
                        arg += (String)arg2;
                        return arg;
                    }
                }
                else if (coerce == FLOAT) {
                    Type = "float";
                    if (declaration.getExpr().getType() == STRING) {

                        arg2 += "(" + Type + ") " + "FileURLIO.readValueFromFile(";
                        arg2 = declaration.getExpr().visit(this, arg2);
                        arg2 += ");";
                        arg += (String)arg2;
                        return arg;
                    }
                }
                else if (coerce == STRING) {
                    Type = "String";
                }
                else if (coerce == BOOLEAN) {
                    Type = "boolean";
                    if (declaration.getExpr().getType() == STRING) {
                        arg2 += "(" + Type + ") " + "FileURLIO.readValueFromFile(";
                        arg2 = declaration.getExpr().visit(this, arg2);
                        arg2 += ");";
                        arg += (String)arg2;
                        return arg;
                    }
                }

                else if (coerce == IMAGE) {
                    Type = "BufferedImage";

                }

                else if (coerce == COLOR) {
                    Type = "ColorTuple";
                    arg2 += "(" + Type + ") " + "FileURLIO.readValueFromFile(";
                    arg2 = declaration.getExpr().visit(this, arg2);
                    arg2 += ");";
                    arg += (String)arg2;
                    return arg;
                }


            }



            else if (nameDef.getType() == COLOR) {
                Imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
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
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {


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
            else if (leftExpr.getType() == COLOR) {
                Object arg3 = "";
                arg3 = leftExpr.visit(this, arg3);
                Object arg4 = "";
                arg4 = rightExpr.visit(this, arg4);

                if (op == Kind.NOT_EQUALS) {
                    if (!arg3.equals(arg4)) {

                        arg += "true";

                    }
                    else {
                        arg += "false";
                    }
                }
                else {
                    if (arg3.equals(arg4)) {
                        arg += "true";
                    }
                    else {
                        arg += "false";
                    }
                }
                return arg;
            }

        }

        if (leftType == IMAGE || leftType == COLOR || leftType == COLORFLOAT
                || rightType == IMAGE || rightType == COLOR || rightType == COLORFLOAT) {

            Imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");

            if (leftType == IMAGE && rightType == COLOR || leftType == COLOR && rightType == IMAGE) {

            }

            else if (leftType == COLOR && rightType == COLOR) {

                arg2 += "(ImageOps.binaryTupleOp(ImageOps.OP.valueOf(";
                if (op == Kind.DIV) {
                    arg2 += "\"DIV\"),";
                }
                else if (op == Kind.MINUS) {
                    arg2 += "\"MINUS\"), ";
                }
                else if (op == Kind.PLUS) {
                    arg2 += "\"PLUS\"), ";
                }
                else if (op == Kind.TIMES) {
                    arg2 += "\"TIMES\"), ";
                }
                else if (op == Kind.MOD) {
                    arg2 += "\"MOD\"), ";
                }

                arg2 = leftExpr.visit(this, arg2);
                arg2 += ", ";

                if (binaryExpr.getLeft().getType() == COLOR && binaryExpr.getRight().getType() == INT) {
                    arg2 += "new ColorTuple(";
                    arg2 = binaryExpr.getRight().visit(this, arg2);
                    arg2 += ")))";
                    arg += (String)arg2;
                    return arg;
                }
                arg2 = rightExpr.visit(this, arg2);
                arg2 += "))";
            }

            else if (leftType == IMAGE && rightType == INT || leftType == INT && rightType == IMAGE) {

                arg2 += "(ImageOps.binaryImageScalarOp(ImageOps.OP.valueOf(";
                if (op == Kind.DIV) {
                    arg2 += "\"DIV\"),";
                }
                else if (op == Kind.MINUS) {
                    arg2 += "\"MINUS\"), ";
                }
                else if (op == Kind.PLUS) {
                    arg2 += "\"PLUS\"), ";
                }
                else if (op == Kind.TIMES) {
                    arg2 += "\"TIMES\"), ";
                }
                else if (op == Kind.MOD) {
                    arg2 += "\"MOD\"), ";
                }

                arg2 = leftExpr.visit(this, arg2);
                arg2 += ", ";

                if (binaryExpr.getLeft().getType() == COLOR && binaryExpr.getRight().getType() == INT) {
                    arg2 += "new ColorTuple(";
                    arg2 = binaryExpr.getRight().visit(this, arg2);
                    arg2 += ")))";
                    arg += (String)arg2;
                    return arg;
                }
                arg2 = rightExpr.visit(this, arg2);
                arg2 += "))";

                /*arg2 += "ImageOps.binaryImageScalarOp(";
                if (op == Kind.DIV) {
                    arg2 += "DIV,";
                }
                else if (op == Kind.MINUS) {
                    arg2 += "MINUS, ";
                }
                else if (op == Kind.PLUS) {
                    arg2 += "PLUS, ";
                }
                else if (op == Kind.TIMES) {
                    arg2 += "TIMES, ";
                }

                arg2 = binaryExpr.getLeft().visit(this, arg2);
                arg2 += ", ";
                arg2 = binaryExpr.getRight().visit(this, arg2);
                arg2 += ")";

                arg += (String)arg2;
                return arg;*/

            }
        }
        else {

            arg2 = "(";
            arg2 = binaryExpr.getLeft().visit(this, arg2);
            arg2 += (binaryExpr.getOp().getText());
            arg2 = binaryExpr.getRight().visit(this, arg2);
            arg2 += ")";
            if (leftType != rightType) {
                arg2 = genTypeConversion(leftType, arg2);


            }
        }


        arg += (String)arg2;
        return arg;
    }

    //Assignment 6
    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        if (consoleExpr.getType() == STRING) {
            Imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
        }
        else {
            Imports.add("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
        }

        String Type = "";
        String Type2 = "";

        if (consoleExpr.getCoerceTo() == INT) {
            Type = "Integer";
            Type2 = "INT";
        }
        else if (consoleExpr.getCoerceTo() == BOOLEAN) {
            Type = "boolean";
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
        else if (consoleExpr.getCoerceTo() == COLOR) {
            Imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
            Type = "ColorTuple";
            Type2 = "COLOR";
        }

        else {
            throw new IllegalArgumentException("Compiler bug Unexpected value: " + consoleExpr.getCoerceTo());
        }

        arg = arg + "(" + Type + ")";
        if (consoleExpr.getType() == STRING) {


            arg += "FileURLIO.readValueFromFile(";


            arg = arg + consoleExpr.getText() + ")";
        }
        else {
            arg += "ConsoleIO.readValueFromConsole(";
            arg = arg + "\"" + Type2 + "\"" + ", " + "\"";
            arg = arg + "Enter " + Type + ":" + "\"";
            arg = arg + ")";
        }





        return arg;


    }

    //Assignment 6
    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        IToken op = unaryExpression.getOp();
        Expr expr = unaryExpression.getExpr();
        Object arg2 = op.getText();
        if (op.getKind() == Kind.COLOR_OP) {
            if ((expr.getType() == INT)) {

                if (arg2.equals("getRed")) {
                    arg2 = "ColorTuple.getRed(";
                }
                else if (arg2.equals("getGreen")) {
                    arg2 = "ColorTuple.getGreen(";
                }
                else if (arg2.equals("getBlue")) {
                    arg2 = "ColorTuple.getBlue(";
                }

                arg2 = "(" + arg2;
                arg2 += "(";
                arg2 = expr.visit(this, arg2);
                arg2 += ")))";
                arg += (String)arg2;
                return arg;
            }

            else if (expr.getType() == IMAGE) {
                Imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");

                if (arg2.equals("getRed")) {
                    arg2 = "ImageOps.extractRed(";
                }
                else if (arg2.equals("getGreen")) {
                    arg2 = "ImageOps.extractGreen(";
                }
                else if (arg2.equals("getBlue")) {
                    arg2 = "ImageOps.extractBlue(";
                }

                arg2 = expr.visit(this, arg2);
                arg2 += ")";
                arg += (String)arg2;
                return arg;

            }


        }

        else if (unaryExpression.getOp().getKind() == Kind.IMAGE_OP) {
            arg2 = unaryExpression.getExpr().getText();
            arg2 += ".";
            arg2 += unaryExpression.getOp().getText();
            arg2 += "()";
            arg += (String)arg2;
            return arg;
        }

        arg2 = expr.visit(this, arg2);
        arg2 = "(" + arg2 + ")";

        arg += (String)arg2;

        return arg;
    }

    //Assignment 6
    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        if (readStatement.getSource().getType().toString().equals("STRING")) {
            Imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");

            arg += readStatement.getName();
            arg += " = ";

            Type _type = readStatement.getTargetType();

            String type = "";

            if (_type == INT) {
                type = "Integer";
            }
            else if (_type == STRING) {
                type = "String";
            }
            else if (_type == BOOLEAN) {
                type = "boolean";
            }
            else if (_type == FLOAT) {
                type = "Float";
            }
            else if (_type == COLOR) {
                type = "ColorTuple";
            }
            else if (_type == IMAGE) {
                type = "BufferedImage";
            }

            arg = arg + "(" + type + ")" + "\n";
            file = readStatement.getSource().getText();

            if (readStatement.getTargetDec().getType() == IMAGE) {
                if (readStatement.getTargetDec().getDim() != null) {

                    Imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
                    arg += "ImageOps.resize(";
                    arg += "FileURLIO.readImage(";
                    arg = arg + file + ")";
                    arg += ", ";
                    arg = readStatement.getTargetDec().getDim().visit(this, arg);
                    arg += ");\n";
                    return arg;


                }


                if (readStatement.getTargetDec().getDim() != null) {

                    Imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
                    arg += "ImageOps.resize(";
                    arg += "FileURLIO.readValueFromFile(";
                    arg = arg + file + ")";
                    arg += ", ";
                    arg = readStatement.getTargetDec().getDim().visit(this, arg);
                    arg += ");\n";
                    return arg;



                }
            }

            else {
                arg += "FileURLIO.readValueFromFile(";
            }
            arg = arg + file + ");";


        }

        else {
            Imports.add("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");

            arg += (readStatement.getName());

            arg += (" = ");
            Expr expr = readStatement.getSource();
            arg = expr.visit(this, arg);
            arg = arg + ";" + "\n";
        }



        return arg;
    }

    //Assignment 6
    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        file = writeStatement.getDest().getText();

        if (writeStatement.getDest().getType() == STRING && writeStatement.getSource().getType() != IMAGE) {
            Imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
            arg += "FileURLIO.writeValue(";
            Expr source = writeStatement.getSource();
            arg = source.visit(this, arg);
            arg = arg + ", " + "" + writeStatement.getDest().getText() + "" + ");" + "\n";

        }
        else if (writeStatement.getSource().getType() == IMAGE && writeStatement.getDest().getType() == CONSOLE) {

            Imports.add("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
            arg += "ConsoleIO.displayImageOnScreen(";
            arg += writeStatement.getSource().getText();
            arg = arg + ");" + "\n";


        }

        else if (writeStatement.getSource().getType() == IMAGE && writeStatement.getDest().getType() == STRING) {
            Imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
            arg += "FileURLIO.writeImage(";
            arg += writeStatement.getSource().getText();
            arg += ", ";
            arg += file;
            arg += ");\n";
            return arg;
        }


        else {
            Imports.add("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
            arg += ("ConsoleIO.console.println(");
            Expr source = writeStatement.getSource();
            arg = source.visit(this, arg);
            arg = arg + ");" + "\n";

        }


        return arg;
    }

    //Assignment 6
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        Object arg2 = "";
        Expr expr = assignmentStatement.getExpr();
        Type name = assignmentStatement.getTargetDec().getType();

        if (name == IMAGE && expr.getType() == IMAGE) {
            if (assignmentStatement.getTargetDec().getDim() != null) {
                arg2 = assignmentStatement.getName();
                arg2 += " = ";
                Imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
                arg2 += "ImageOps.resize(";
                arg2 += expr.getText();
                arg2 += ", ";
                arg2 = assignmentStatement.getTargetDec().getDim().visit(this, arg2);
                arg2 += ");\n";
                arg += (String) arg2;
                return arg;
            }
            else {
                arg2 = assignmentStatement.getName();
                arg2 += " = ";
                arg2 = expr.visit(this, arg2);
                arg2 += ";\n";
                arg += (String)arg2;
                return arg;
            }
        }

        else if (name == IMAGE && expr.getType() == INT) {
            Imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
            Imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
            String image = assignmentStatement.getName();
            arg2 = arg2 + "for (int x = 0; x < " + image + ".getWidth(); x++)\n    ";
            arg2 = arg2 + "for (int y = 0; y < " + image + ".getHeight(); y++)\n        ";
            arg2 = arg2 + "ImageOps.setColor(" + image + ", x, y, ";
            arg2 += "new ColorTuple(";
            arg2 = expr.visit(this, arg2);
            arg2 += "));\n";
            arg += (String)arg2;
            return arg;
        }

        else if ((expr.getCoerceTo() == COLOR || (expr.getType() == COLOR && expr.getCoerceTo() == null)) && !(name == COLOR && expr.getType() == COLOR)) {
            Imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
            String image = assignmentStatement.getName();
            arg2 = arg2 + "for (int x = 0; x < " + image + ".getWidth(); x++)\n    ";
            arg2 = arg2 + "for (int y = 0; y < " + image + ".getHeight(); y++)\n        ";
            arg2 = arg2 + "ImageOps.setColor(" + image + ", x, y, ";
            arg2 = expr.visit(this, arg2);
            arg2 += ");\n";
            arg += (String)arg2;
            return arg;
        }

        else if (expr.getCoerceTo() == INT) {
            String image = assignmentStatement.getTargetDec().getText();
            arg2 += "ColorTuple X = new ColorTuple(truncate(";
            arg2 = expr.visit(this, arg2);
            arg2 += "));\n";
            arg2 = arg2 + "for (int x = 0; x < " + image + ".getWidth(); x++)\n    ";
            arg2 = arg2 + "for (int y = 0; y < " + image + ".getHeight(); y++)\n        ";
            arg2 = arg2 + "ImageOps.setColor(" + image + ", x, y, X);\n";
            arg += (String)arg2;
            return arg;
        }

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

            else if (coerce == IMAGE) {
                type = "BufferedImage";

            }

            else if (coerce == COLOR) {
                type = "ColorTuple";

            }

            if (type != "") {
                arg2 += "(" + type + ")";
            }
        }
        arg2 += assignmentStatement.getName();
        arg2 += " = ";
        arg2 = expr.visit(this, arg2);
        arg2 = arg2 + ";" + "\n";
        arg += (String)arg2;
        return arg;
    }

    //Assignment 6
    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        Imports.add("import java.awt.Color;\n");
        Imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
        /*arg += "(new ColorTuple(ColorTuple.unpack(Color.";
        arg += colorConstExpr.getText();
        arg += ".getRGB())))";*/
        arg += "ColorTuple.toColorTuple(Color.";
        arg += colorConstExpr.getText();
        arg += ")";

        return arg;
    }

    //Assignment 6
    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        Expr red = colorExpr.getRed();
        Expr green = colorExpr.getGreen();
        Expr blue = colorExpr.getBlue();


        arg += "new ColorTuple(";
        arg = red.visit(this, arg);
        arg += ", ";
        arg = green.visit(this, arg);
        arg += ", ";
        arg = blue.visit(this, arg);
        arg += ")";

        Imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");

        return arg;
    }

    //Assignment 6
    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        Object arg2 = "";
        arg2 = dimension.getWidth().visit(this, arg2);
        arg2 += ", ";
        arg2 = dimension.getHeight().visit(this, arg2);
        arg += (String)arg2;
        return arg;
    }

    //Assignment 6
    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {


        return arg;
    }

    //Assignment 6
    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        Imports.add("import java.awt.image.BufferedImage;\n");
        Object arg2 = "";
        arg2 += "ColorTuple.unpack(";
        arg2 += unaryExprPostfix.getExpr().getText();
        arg2 += ".getRGB(";
        arg2 += unaryExprPostfix.getSelector().getX().getText();
        arg2 += ", ";
        arg2 += unaryExprPostfix.getSelector().getY().getText();
        arg2 += "))";
        arg += (String)arg2;
        return arg;
    }

    //Assignment 6
    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {



        String Type = "";

        if (nameDefWithDim.getType() == IMAGE) {
            Type = "BufferedImage";

            Imports.add("import java.awt.image.BufferedImage;\n");


        }
        else if (nameDefWithDim.getType() == COLOR) {
            Type = "ColorTuple";


        }
        else {
            throw new IllegalArgumentException("Compiler bug Unexpected value: " + nameDefWithDim.getType());
        }

        arg = Type + " " + (nameDefWithDim.getName()) + arg;
        return arg;
    }
}


