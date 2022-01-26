package org.datazup.expression;

/**
 * Created by admin@datazup on 3/14/16.
 */
public class BracketPair {
    public static final BracketPair PARENTHESES = new BracketPair('(', ')');
    public static final BracketPair BRACKETS = new BracketPair('[', ']');
    public static final BracketPair BRACES = new BracketPair('{', '}');
    public static final BracketPair ANGLES = new BracketPair('<', '>');
    private final String open;
    private final String close;

    public BracketPair(char open, char close) {
        this.open = String.valueOf(open);
        this.close = String.valueOf(close);
    }

    public String getOpen() {
        return this.open;
    }

    public String getClose() {
        return this.close;
    }

    public String toString() {
        return this.open + this.close;
    }
}
