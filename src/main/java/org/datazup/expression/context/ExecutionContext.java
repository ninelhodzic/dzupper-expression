package org.datazup.expression.context;

public interface ExecutionContext {
    ContextWrapper create(Object object);
}
