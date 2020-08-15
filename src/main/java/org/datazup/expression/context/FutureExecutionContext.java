package org.datazup.expression.context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class FutureExecutionContext implements ExecutionContext {

    public ContextWrapper create(Object object){
        Future future = null;
        if(object instanceof Future){
            future = (Future)object;
        }else{
            final Object futureObject = object;
            future = CompletableFuture.supplyAsync(() -> {
                return object;
            });
        }
        FutureContextWrapper contextWrapper = new FutureContextWrapper(future);
        return contextWrapper;
    }
}
