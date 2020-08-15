package org.datazup.expression.context;

import org.datazup.expression.context.exceptions.ExpressionContextException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FutureContextWrapper implements ContextWrapper {

    private Future result;

    public FutureContextWrapper(Future object) {
        this.result = object;
    }

    @Override
    public Object get() {
        if (null != this.result) {
            try {
                Object r = this.result.get();
                return r;
            } catch (InterruptedException | ExecutionException e) {
                throw new ExpressionContextException(e);
            }

        }else {
            return null;
        }
    }
}
