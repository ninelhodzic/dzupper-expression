package org.datazup.expression;


import java.util.Map;

/**
 * Created by admin@datazup on 3/14/16.
 */
public class Token {
    public static final Token FUNCTION_ARG_SEPARATOR;
    private final Token.Kind kind;
    private final Object content;

    static Token buildLiteral(String literal) {
        return new Token(Token.Kind.LITERAL, literal);
    }

    public static Token buildLiteralValue(String literal) {
        return new Token(Token.Kind.LITERAL_VALUE, literal);
    }

    public static Token buildNumber(Number number) {
        return new Token(Kind.NUMBER, number);
    }

    public static Token buildOperator(Operator ope) {
        return new Token(Token.Kind.OPERATOR, ope);
    }

    public static Token buildFunction(Function function) {
        return new Token(Token.Kind.FUNCTION, function);
    }

    public static Token buildOpenToken(BracketPair pair) {
        return new Token(Token.Kind.OPEN_BRACKET, pair);
    }

    public static Token buildCloseToken(BracketPair pair) {
        return new Token(Token.Kind.CLOSE_BRACKET, pair);
    }

    private Token(Token.Kind kind, Object content) {
        if ((!kind.equals(Token.Kind.OPERATOR) || content instanceof Operator)
                && (!kind.equals(Token.Kind.FUNCTION) || content instanceof Function)
                && (!kind.equals(Token.Kind.LITERAL) || content instanceof String)
                && (!kind.equals(Kind.LITERAL_VALUE) || content instanceof String)
                && (!kind.equals(Kind.FUNCTION_LITERAL) || content instanceof Map)
                && (!kind.equals(Kind.LOOKUP_LITERAL) || content instanceof String)
                && (!kind.equals(Kind.NUMBER) || content instanceof Number)) {
            this.kind = kind;
            this.content = content;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public BracketPair getBrackets() {
        return (BracketPair) this.content;
    }

    public Operator getOperator() {
        return (Operator) this.content;
    }

    public Function getFunction() {
        return (Function) this.content;
    }

    Token.Kind getKind() {
        return this.kind;
    }

    public boolean isOperator() {
        return this.kind.equals(Token.Kind.OPERATOR);
    }

    public boolean isFunction() {
        return this.kind.equals(Token.Kind.FUNCTION);
    }

    public boolean isOpenBracket() {
        return this.kind.equals(Token.Kind.OPEN_BRACKET);
    }

    public boolean isCloseBracket() {
        return this.kind.equals(Token.Kind.CLOSE_BRACKET);
    }

    public boolean isFunctionArgumentSeparator() {
        return this.kind.equals(Token.Kind.FUNCTION_SEPARATOR);
    }

    public boolean isLiteral() {
        return this.kind.equals(Token.Kind.LITERAL);
    }

    public boolean isLookupLiteral() {
        return this.kind.equals(Kind.LOOKUP_LITERAL);
    }

    public boolean isFunctionLiteral() {
        return this.kind.equals(Kind.FUNCTION_LITERAL);
    }

    public Operator.Associativity getAssociativity() {
        return this.getOperator().getAssociativity();
    }

    public int getPrecedence() {
        return this.getOperator().getPrecedence();
    }

    public String getLiteral() {
        if (!this.kind.equals(Token.Kind.LITERAL)) {
            throw new IllegalArgumentException();
        } else {
            return (String) this.content;
        }
    }

    static {
        FUNCTION_ARG_SEPARATOR = new Token(Token.Kind.FUNCTION_SEPARATOR, null);
    }

    public boolean isLiteralValue() {
        return this.kind.equals(Kind.LITERAL_VALUE);
    }

    public boolean isNumber() {
        return this.kind.equals(Kind.NUMBER);
    }

    public Number getNumber() {
        if (!this.kind.equals(Kind.NUMBER)) {
            throw new IllegalArgumentException();
        } else {
            return (Number) this.content;
        }
    }

    public String getLiteralValue() {
        if (!this.kind.equals(Kind.LITERAL_VALUE)) {
            throw new IllegalArgumentException();
        } else {
            return (String) this.content;
        }
    }

    public String getLookupLiteralValue() {
        if (!this.kind.equals(Kind.LOOKUP_LITERAL)) {
            throw new IllegalArgumentException();
        } else {
            return (String) this.content;
        }
    }

    public String toString() {
        return "("+kind+": "+content.toString()+")";
    }

    public Object getContent() {
        return content;
    }

    public static Token buildFunctionLiteralToken(Map<String, String> content) {
        return new Token(Kind.FUNCTION_LITERAL, content);
    }

    public static Token buildLookupLiteral(String literal) {
        return new Token(Kind.LOOKUP_LITERAL, literal);
    }

    private enum Kind {
        OPEN_BRACKET,
        CLOSE_BRACKET,
        FUNCTION_SEPARATOR,
        FUNCTION,
        FUNCTION_LITERAL,
        OPERATOR,
        LITERAL,
        NUMBER,
        LOOKUP_LITERAL,
        LITERAL_VALUE;

        Kind() {
        }
    }
}