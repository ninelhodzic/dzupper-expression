package org.datazup.expression.context;

import java.util.concurrent.Future;

public class FutureExecutionContext implements ExecutionContext<Future> {

    public ContextWrapper create(Future object){
        FutureContextWrapper contextWrapper = new FutureContextWrapper(object);
        return contextWrapper;
    }
}
