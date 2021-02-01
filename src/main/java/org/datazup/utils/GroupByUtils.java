package org.datazup.utils;

import com.github.underscore.U;
import org.apache.commons.lang3.StringUtils;
import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.AbstractEvaluator;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.pathextractor.PathExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupByUtils {
    private AbstractResolverHelper mapListResolver;
    private AbstractEvaluator mapperEvaluator;

    public GroupByUtils(AbstractEvaluator mapperEvaluator, AbstractResolverHelper mapListResolver) {
        this.mapperEvaluator = mapperEvaluator;
        this.mapListResolver = mapListResolver;
    }

    public Map<Object, List<Object>> groupByProperty(List list, Map<String, Object> stringObjectMap) {
        String propertyName = (String) stringObjectMap.get("propertyName");

        Map<Object, List<Object>> result = U.groupBy(list, o -> {
            Map m = mapListResolver.resolveToMap(o);
            if (null != m) {
                if (propertyName.startsWith("#") && propertyName.endsWith("#")) {
                    try {
                        ContextWrapper contextWrapper = mapperEvaluator.evaluate(propertyName.substring(1, propertyName.length() - 1), new PathExtractor(m, mapListResolver));
                        return contextWrapper.get();
                    } catch (EvaluatorException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    return m.get(propertyName);
                }
            }
            return null;
        });

        return result;
    }

    public List<Map<Object, List<Object>>> groupByProperties(List list, List<Map<String, Object>> properties, String childrenKey) {
        Map<Map, List<Object>> resultMap = U.groupBy(list, o -> {
            Map m = mapListResolver.resolveToMap(o);
            Map newResultMap = new HashMap();
            if (null != m) {
                if (null != properties) {
                    U.each(properties, prop -> {
                        Object origPropVal = null;
                        String origPropExpression = (String) prop.get("property");

                        if (StringUtils.containsAny(origPropExpression, " ", "(", ")", "$", ".")) {
                            try {
                                ContextWrapper contextWrapper = mapperEvaluator.evaluate(origPropExpression, new PathExtractor(m, mapListResolver));
                                origPropVal = contextWrapper.get();
                            } catch (EvaluatorException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            origPropVal = m.get(origPropExpression);
                        }

                        String newPropName = (String) prop.get("propertyNewName");

                        if (null != newPropName) {
                            origPropExpression = newPropName;
                        }
                        newResultMap.put(origPropExpression, origPropVal);
                    });
                    return newResultMap;
                }
            }
            return null;
        });

        List<Map<Object, List<Object>>> result = new ArrayList<>();
        resultMap.forEach((map, objects) -> {
            if (null != map) {
                Map newMap = new HashMap(map);
                newMap.put(childrenKey, objects);
                result.add(newMap);
            }
        });

        return result;
    }
}
