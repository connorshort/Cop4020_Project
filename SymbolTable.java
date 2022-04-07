package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Types;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    //TODO:  Implement a symbol table class that is appropriate for this language.
    Map<String, Declaration> symbols = new HashMap<>();

    public boolean add(String name, Declaration declaration){
        return (symbols.putIfAbsent(name, declaration)==null);
    }
    public Declaration remove(String name){return symbols.remove(name);}

    public Declaration getDeclaration(String name){
        return symbols.get(name);
    }
}