package org.datazup.apiinternal.exception;

/**
 * Created by ninel on 11/30/17.
 */
public class APIRunnableException extends RuntimeException {

    public APIRunnableException(String message){
        super(message);
    }
    public APIRunnableException(String message, Throwable e){
        super(message, e);
    }
}
