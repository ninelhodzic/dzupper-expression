package org.datazup.expression;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ninel on 7/3/17.
 */
public abstract class AbstractMapListResolver {

    protected abstract Map resolveToMap(Object o);
    protected abstract List resolveToList(Object o);
    protected abstract Collection resolveToCollection(Object o);

}
