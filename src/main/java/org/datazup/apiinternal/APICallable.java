package org.datazup.apiinternal;

import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.pathextractor.PathExtractor;

/**
 * Created by admin@datazup on 11/22/17.
 */
public interface APICallable {
    ContextWrapper run(ContextWrapper paramsContext, PathExtractor context) throws EvaluatorException;
    String getName();
}
