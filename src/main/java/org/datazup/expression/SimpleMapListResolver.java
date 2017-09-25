package org.datazup.expression;

import org.datazup.utils.JsonUtils;

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
        else if (o instanceof String){
            String s = (String)o;
            s = s.trim();
            s = cleanStartEndHash(s);
            if (s.startsWith("{") && s.endsWith("}")) {
                Map m = JsonUtils.getMapFromJson(s);
                return m;
            }
        }
        return null;
    }

    @Override
    public List resolveToList(Object o) {
        if (o instanceof List)
            return (List) o;
        else if (o instanceof List){
            String s = (String)o;
            s = s.trim();
            s = cleanStartEndHash(s);
            if (s.startsWith("[")  && s.endsWith("]")) {
                List l = JsonUtils.getListFromJson((String) o);
                return l;
            }
        }
        return null;
    }

    @Override
    public Collection resolveToCollection(Object o) {
        if (o instanceof Collection)
            return (Collection) o;
        else if (o instanceof String){
            List l = resolveToList(o);
            return l;
        }
        return null;
    }
}
