package org.datazup.expression.context.exceptions;

/**
 * Created by admin@datazup on 11/25/16.
 */
public class ExpressionContextException extends RuntimeException {

    public ExpressionContextException(String message){
        super(message);
    }
    public ExpressionContextException(Throwable e){
        super(e);
    }
    public ExpressionContextException(String message, Throwable e){
        super(message, e);
    }
}
