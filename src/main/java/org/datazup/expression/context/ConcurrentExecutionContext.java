package org.datazup.expression.context;

public class ConcurrentExecutionContext implements ExecutionContext<Object> {
    public ContextWrapper create(Object object){
        SyncContextWrapper contextWrapper = new SyncContextWrapper(object);
        return contextWrapper;
    }
}
