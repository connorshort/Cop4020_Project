package edu.ufl.cise.plc;

import java.lang.StringBuilder;

public class CodeGenStringBuilder {
    StringBuilder arg;


    public CodeGenStringBuilder append(String arg) {
        this.arg.append(arg);
        return this;
    }

    public CodeGenStringBuilder append(CodeGenStringBuilder arg) {
        this.arg.append(arg.arg);
        return this;
    }

    public CodeGenStringBuilder semi() {
        this.arg.append(";");
        return this;
    }

    public CodeGenStringBuilder newline() {
        this.arg.append("\n");
        return this;
    }

    public CodeGenStringBuilder lpar() {
        this.arg.append("(");
        return this;
    }

    public CodeGenStringBuilder rpar() {
        this.arg.append(")");
        return this;
    }

    public CodeGenStringBuilder comma() {
        this.arg.append(",");
        return this;
    }

    public CodeGenStringBuilder quotes() {
        this.arg.append("\"\"\"");
        return this;
    }
}
