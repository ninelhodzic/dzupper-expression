package org.datazup.exceptions;

public class EvaluatorException extends Exception {

    private Object context;
    private String expression;
    public EvaluatorException(String message){
        super(message);
    }

    public EvaluatorException(String message, Throwable e){
        super(message, e);
    }


    public EvaluatorException(String message, String expression, Object context, Throwable e){
        super(message, e);
        this.expression = expression;
        this.context = context;
    }
}
