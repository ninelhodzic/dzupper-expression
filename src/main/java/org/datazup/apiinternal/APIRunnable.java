package org.datazup.apiinternal;

import org.datazup.pathextractor.PathExtractor;

/**
 * Created by admin@datazup on 11/22/17.
 */
public interface APIRunnable {
    Object run(Object params, PathExtractor context);
    String getName();
}
