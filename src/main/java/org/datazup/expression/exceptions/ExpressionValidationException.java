package org.datazup.expression.exceptions;

/**
 * Created by admin@datazup on 11/25/16.
 */
public class ExpressionValidationException extends RuntimeException {

    public ExpressionValidationException(String message){
        super(message);
    }
    public ExpressionValidationException(String message, Throwable e){
        super(message, e);
    }
}
