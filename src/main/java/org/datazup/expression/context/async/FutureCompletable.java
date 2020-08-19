package org.datazup.expression.context.async;

public interface FutureCompletable<T> {
    void complete(T object);
    void fail(Throwable t);
}
