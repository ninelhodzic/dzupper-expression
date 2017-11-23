package org.datazup.expression.exceptions;

/**
 * Created by admin@datazup on 3/19/16.
 */
public class InvalidActionListDefinition extends RuntimeException {

    private String json;

    public InvalidActionListDefinition(String message, String json){
        super(message);
        this.json = json;
    }

    public InvalidActionListDefinition(String message, String json, Throwable e){
        super(message, e);
        this.json = json;
    }
}
