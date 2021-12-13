/*
package org.datazup.expression.context.async;

import org.apache.commons.lang3.NotImplementedException;
import org.datazup.expression.context.exceptions.ExpressionContextException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FutureContextWrapper<T> implements AsyncContextWrapper<T> {

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


    @Override
    public void handle(FutureHandler<T> handler) {
        throw new NotImplementedException("Not implemented");
    }
}
*/
