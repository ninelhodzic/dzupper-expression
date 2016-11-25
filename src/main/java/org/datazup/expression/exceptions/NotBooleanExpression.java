package org.datazup.expression.exceptions;

/**
 * Created by ninel on 3/15/16.
 */
public class NotBooleanExpression extends RuntimeException {
    public NotBooleanExpression(String message){
        super(message);
    }
}
