package org.datazup.apiinternal;

import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.pathextractor.PathExtractor;

/**
 * Created by admin@datazup on 11/22/17.
 */

public interface ApiService {
    Boolean contains(String apiName);
    ContextWrapper execute(String apiName, ContextWrapper params, PathExtractor context) throws EvaluatorException;
    void add(APICallable apiCallable);
}
