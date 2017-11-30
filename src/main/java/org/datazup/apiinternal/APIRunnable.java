package org.datazup.apiinternal;

/**
 * Created by admin@datazup on 11/22/17.
 */
public interface APIRunnable {
    Object run(Object params);
    String getName();
    //void init();
}
