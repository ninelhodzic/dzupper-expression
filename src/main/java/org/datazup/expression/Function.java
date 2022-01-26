package org.datazup.expression;

/**
 * Created by admin@datazup on 3/14/16.
 */

public class Function {
    private final String name;
    private final int minArgumentCount;
    private final int maxArgumentCount;

    public Function(String name, int argumentCount) {
        this(name, argumentCount, argumentCount);
    }

    public Function(String name, int minArgumentCount, int maxArgumentCount) {
        if(minArgumentCount >= 0 && minArgumentCount <= maxArgumentCount) {
            if(name != null && name.length() != 0) {
                this.name = name;
                this.minArgumentCount = minArgumentCount;
                this.maxArgumentCount = maxArgumentCount;
            } else {
                throw new IllegalArgumentException("Invalid function name");
            }
        } else {
            throw new IllegalArgumentException("Invalid argument count");
        }
    }

    public String getName() {
        return this.name;
    }

    public int getMinimumArgumentCount() {
        return this.minArgumentCount;
    }

    public int getMaximumArgumentCount() {
        return this.maxArgumentCount;
    }
}
