package org.datazup.expression.evaluators;

import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.pathextractor.AbstractResolverHelper;

public abstract class EvaluatorBase {
    protected AbstractResolverHelper mapListResolver;
    protected ExecutionContext executionContext;

    public EvaluatorBase(AbstractResolverHelper mapListResolver, ExecutionContext executionContext) {
        this.mapListResolver = mapListResolver;
        this.executionContext = executionContext;
    }

    protected ContextWrapper wrap(Object object){
        return executionContext.create(object);
    }
}
