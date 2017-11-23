package org.datazup.expression.exceptions;

/**
 * Created by admin@datazup on 3/15/16.
 */
public class NotBooleanExpression extends RuntimeException {
    public NotBooleanExpression(String message){
        super(message);
    }
}
