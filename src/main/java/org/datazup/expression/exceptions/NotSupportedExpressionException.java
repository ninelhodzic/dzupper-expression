package org.datazup.expression.exceptions;

/**
 * Created by admin@datazup on 11/25/16.
 */
public class NotSupportedExpressionException extends RuntimeException {
    public NotSupportedExpressionException(String message){
        super(message);
    }
    public NotSupportedExpressionException(String message, Throwable e){
        super(message, e);
    }
}
