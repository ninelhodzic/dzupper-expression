package org.datazup.exceptions;

public class EvaluatorException extends Exception {

    public EvaluatorException(String message){
        super(message);
    }

    public EvaluatorException(String message, Throwable e){
        super(message, e);
    }
}
