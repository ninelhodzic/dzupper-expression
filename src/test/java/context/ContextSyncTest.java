package context;

import org.datazup.expression.context.ConcurrentExecutionContext;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.utils.TypeUtils;
import org.junit.Assert;
import org.junit.Test;

public class ContextSyncTest {

    ExecutionContext executionContext = new ConcurrentExecutionContext();

    @Test
    public void simpleTest(){
        Integer obj = 6;
        ContextWrapper contextWrapper = executionContext.create(obj);
        Object result = contextWrapper.get();
        Assert.assertTrue(null!=result);
        Assert.assertTrue(result instanceof Integer);
        Assert.assertTrue(TypeUtils.resolveInteger(result)==obj);

    }
}
