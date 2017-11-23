package org.datazup.expression;

/**
 * Created by admin@datazup on 11/25/16.
 */
public class NullObject {
    public Object get(){
        return null;
    }
    public boolean isNull() { return true; }

    public NullObject(){}
    public NullObject(Object n){}
    public String toString(){
        return "{}";
    }
}
