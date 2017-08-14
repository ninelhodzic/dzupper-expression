package org.datazup.expression;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ninel on 7/3/17.
 */
public abstract class AbstractMapListResolver {

    public abstract Map resolveToMap(Object o);
    public abstract List resolveToList(Object o);
    public abstract Collection resolveToCollection(Object o);


    protected String cleanStartEndHash(String s) {
        if (s.startsWith("#") && s.endsWith("#")){
            s = s.substring(1, s.length()-1);
        }
        return s;
    }

}
