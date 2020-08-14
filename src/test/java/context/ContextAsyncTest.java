package context;

import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.expression.context.FutureExecutionContext;
import org.datazup.utils.TypeUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ContextAsyncTest {
    ExecutionContext executionContext =  new FutureExecutionContext();

    @Test
    public void simpleTest(){
        Integer obj = 6;
        CompletableFuture completableFuture = CompletableFuture.supplyAsync((Supplier<Object>) () -> obj);

        ContextWrapper contextWrapper = executionContext.create(completableFuture);
        Object result = contextWrapper.get();
        Assert.assertTrue(null!=result);
        Assert.assertTrue(result instanceof Integer);
        Assert.assertTrue(TypeUtils.resolveInteger(result)==obj);
    }
}
