package org.datazup.expression;

import java.util.*;

/**
 * Created by admin@datazup on 3/14/16.
 */

public class Parameters {
    private String functionSeparator;
    private final List<Operator> operators = new ArrayList();
    private final List<Function> functions = new ArrayList();
    private final List<Constant> constants = new ArrayList();
    private final Map<String, String> translations = new HashMap();
    private final List<BracketPair> expressionBrackets = new ArrayList();
    private final List<BracketPair> functionBrackets = new ArrayList();

    public Parameters() {
        this.setFunctionArgumentSeparator(',');
    }

    public Collection<Operator> getOperators() {
        return this.operators;
    }

    public Collection<Function> getFunctions() {
        return this.functions;
    }

    public Collection<Constant> getConstants() {
        return this.constants;
    }

    public Collection<BracketPair> getExpressionBrackets() {
        return this.expressionBrackets;
    }

    public Collection<BracketPair> getFunctionBrackets() {
        return this.functionBrackets;
    }

    public void addOperators(Collection<Operator> operators) {
        this.operators.addAll(operators);
    }

    public void add(Operator operator) {
        this.operators.add(operator);
    }

    public void addFunctions(Collection<Function> functions) {
        this.functions.addAll(functions);
    }

    public void add(Function function) {
        this.functions.add(function);
    }

    public void addConstants(Collection<Constant> constants) {
        this.constants.addAll(constants);
    }

    public void add(Constant constant) {
        this.constants.add(constant);
    }

    public void addExpressionBracket(BracketPair pair) {
        this.expressionBrackets.add(pair);
    }

    public void addExpressionBrackets(Collection<BracketPair> brackets) {
        this.expressionBrackets.addAll(brackets);
    }

    public void addFunctionBracket(BracketPair pair) {
        this.functionBrackets.add(pair);
    }

    public void addFunctionBrackets(Collection<BracketPair> brackets) {
        this.functionBrackets.addAll(brackets);
    }

    public void setTranslation(Function function, String translatedName) {
        this.setTranslation(function.getName(), translatedName);
    }

    public void setTranslation(Constant constant, String translatedName) {
        this.setTranslation(constant.getName(), translatedName);
    }

    private void setTranslation(String name, String translatedName) {
        this.translations.put(name, translatedName);
    }

    public String getTranslation(String originalName) {
        String translation = this.translations.get(originalName);
        return translation == null?originalName:translation;
    }

    public void setFunctionArgumentSeparator(char separator) {
        this.functionSeparator = String.valueOf(separator);
    }

    public String getFunctionArgumentSeparator() {
        return this.functionSeparator;
    }
}
