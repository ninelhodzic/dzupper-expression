package org.datazup.expression.context.async;

import org.datazup.expression.context.ContextWrapper;

public interface AsyncContextWrapper<T> extends ContextWrapper {
    void handle(FutureHandler<T> handler);
}
