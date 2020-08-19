package org.datazup.expression.context.async;

public interface FutureHandler<T> {
    void handle(FutureCompletable<T> result);
}
