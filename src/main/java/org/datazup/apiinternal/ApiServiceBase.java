package org.datazup.apiinternal;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin@datazup on 11/22/17.
 */
public abstract class ApiServiceBase implements ApiService {

    protected Map<String,APIRunnable> apis = new HashMap<>();

    public void add(APIRunnable apiRunnable){
        apis.put(apiRunnable.getName(), apiRunnable);
    }

    public Boolean contains(String apiName){
        return apis.containsKey(apiName);
    }

    public Object execute(String apiName, Object params){

        if (apis.containsKey(apiName)){
            APIRunnable api = apis.get(apiName);
            Object apiResponse = api.run(params);
            return apiResponse;
        }
        return null;
    }

    public abstract void load();
}
