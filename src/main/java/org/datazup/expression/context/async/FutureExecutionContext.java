package org.datazup.expression.context.async;

import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class FutureExecutionContext implements ExecutionContext {

    public ContextWrapper create(Object object){
        Future future = null;
        if(object instanceof Future){
            future = (Future)object;
        }else{
            final Object futureObject = object;
            future = CompletableFuture.supplyAsync(() -> object);
        }
        FutureContextWrapper contextWrapper = new FutureContextWrapper(future);
        return contextWrapper;
    }

   /* @Override
    public AsyncContextWrapper createAsync(Object object) {
        CompletableFuture completableFuture = new CompletableFuture();
        completableFuture.complete(object);
        return new FutureContextWrapper(completableFuture);
    }*/
}
