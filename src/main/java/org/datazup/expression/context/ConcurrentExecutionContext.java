package org.datazup.expression.context;

public class ConcurrentExecutionContext implements ExecutionContext {
    public ContextWrapper create(Object object){
        SyncContextWrapper contextWrapper = new SyncContextWrapper(object);
        return contextWrapper;
    }

    /*@Override
    public AsyncContextWrapper createAsync(Object object) {
        CompletableFuture completableFuture = new CompletableFuture();
        completableFuture.complete(object);
        return new FutureContextWrapper(completableFuture);
    }*/
}
