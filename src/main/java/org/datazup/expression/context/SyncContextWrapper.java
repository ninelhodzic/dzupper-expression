package org.datazup.expression.context;

public class SyncContextWrapper implements ContextWrapper {
    private Object result;
    public SyncContextWrapper(Object object) {
        this.result = object;
    }
    public SyncContextWrapper(){}

    @Override
    public Object get() {
        return result;
    }
}
