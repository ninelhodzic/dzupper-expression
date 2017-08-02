package org.datazup.expression;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ninel on 7/3/17.
 */
public class SimpleMapListResolver extends AbstractMapListResolver {
    @Override
    public Map resolveToMap(Object o) {
        if (o instanceof Map)
            return (Map) o;
        return null;
    }

    @Override
    public List resolveToList(Object o) {
        if (o instanceof List)
            return (List) o;
        return null;
    }

    @Override
    public Collection resolveToCollection(Object o) {
        if (o instanceof Collection)
            return (Collection) o;
        return null;
    }
}
