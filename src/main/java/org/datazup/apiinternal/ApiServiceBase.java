package org.datazup.apiinternal;

import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.pathextractor.PathExtractor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin@datazup on 11/22/17.
 */
public abstract class ApiServiceBase implements ApiService {

    protected Map<String, APICallable> apis = new HashMap<>();

    public void add(APICallable apiCallable) {
        apis.put(apiCallable.getName(), apiCallable);
    }

    public Boolean contains(String apiName) {
        return apis.containsKey(apiName);
    }

    public ContextWrapper execute(String apiName, ContextWrapper params, PathExtractor context) throws EvaluatorException {
        if (apis.containsKey(apiName)) {
            APICallable api = apis.get(apiName);
            ContextWrapper apiResponse = api.run(params, context);
            return apiResponse;
        }
        return null;
    }

    public abstract void load();
}
