package org.datazup.expression.context;

public interface ExecutionContext<T> {
    ContextWrapper create(T object);
}
