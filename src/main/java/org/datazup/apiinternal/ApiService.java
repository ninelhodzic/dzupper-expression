package org.datazup.apiinternal;

/**
 * Created by admin@datazup on 11/22/17.
 */

public interface ApiService {
    Boolean contains(String apiName);
    Object execute(String apiName, Object params);
    void add(APIRunnable apiRunnable);
}
