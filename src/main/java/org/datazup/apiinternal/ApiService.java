package org.datazup.apiinternal;

import org.datazup.pathextractor.PathExtractor;

/**
 * Created by admin@datazup on 11/22/17.
 */

public interface ApiService {
    Boolean contains(String apiName);
    Object execute(String apiName, Object params, PathExtractor context);
    void add(APIRunnable apiRunnable);
}
