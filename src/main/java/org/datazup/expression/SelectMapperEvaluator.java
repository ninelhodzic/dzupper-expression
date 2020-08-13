package org.datazup.expression;


import org.apache.commons.lang3.math.NumberUtils;
import org.datazup.apiinternal.ApiService;
import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.expression.exceptions.ExpressionValidationException;
import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.pathextractor.PathExtractor;
import org.datazup.utils.GroupByUtils;
import org.datazup.utils.ListPartition;
import org.datazup.utils.SortingUtils;
import org.datazup.utils.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin@datazup on 3/21/16.
 */
public class SelectMapperEvaluator extends SimpleObjectEvaluator {


    private static final Logger LOG = LoggerFactory.getLogger(SelectMapperEvaluator.class);

    public final static Function SELECT = new Function("SELECT", 1, Integer.MAX_VALUE);
    public final static Function LIST = new Function("LIST", 0, Integer.MAX_VALUE);
    public final static Function SORTED_SET = new Function("SORTED_SET", 1, Integer.MAX_VALUE);
    public final static Function LIST_PARTITION = new Function("LIST_PARTITION", 1, 2);
    public final static Function LIST_UNPARTITION = new Function("LIST_UNPARTITION", 1);


    public final static Function MAP = new Function("MAP", 0, Integer.MAX_VALUE);
    public final static Function REMAP = new Function("REMAP", 2, 2);
    public final static Function FOREACH = new Function("FOREACH", 2, 3);
    public final static Function STEP = new Function("STEP", 2, Integer.MAX_VALUE);

    public final static Function REMOVE = new Function("REMOVE", 1);
    public final static Function THIS = new Function("THIS", 0);
    public final static Function COPY = new Function("COPY", 1);
    public final static Function UNION = new Function("UNION", 1, Integer.MAX_VALUE);
    public final static Function EXTEND = new Function("EXTEND", 1, Integer.MAX_VALUE);
    public final static Function KEYS = new Function("KEYS", 1);
    public final static Function VALUES = new Function("VALUES", 1);
    public final static Function FIELD = new Function("FIELD", 2, 3);
    public final static Function TEMPLATE = new Function("T", 1, 2);
    public final static Function EXCLUDE_FIELDS = new Function("EXCLUDE_FIELDS", 1, Integer.MAX_VALUE);
    public final static Function API = new Function("API", 2, 2);

    public final static Function GROUP_BY = new Function("GROUP_BY", 2, 3);


    public final static Function GET = new Function("GET", 2, 3);
    public final static Function ADD = new Function("ADD", 2, Integer.MAX_VALUE);
    public final static Function PUT = new Function("PUT", 3, 3);

    public final static Function SORT = new Function("SORT", 2, 3);
    public final static Function LIMIT = new Function("LIMIT", 2, 2);

    public final static Function SUM = new Function("SUM", 1);
    public final static Function AVG = new Function("AVG", 1);
    public final static Function MAX = new Function("MAX", 1);
    public final static Function MIN = new Function("MIN", 1);


    private static SelectMapperEvaluator INSTANCE;

    public static SelectMapperEvaluator getInstance(ExecutionContext executionContext, AbstractResolverHelper mapListResolver) {
        synchronized (SelectMapperEvaluator.class) {
            if (null == INSTANCE) {
                synchronized (SelectMapperEvaluator.class) {
                    if (null == INSTANCE)
                        INSTANCE = new SelectMapperEvaluator(executionContext,mapListResolver);
                }
            }
        }
        return INSTANCE;
    }

    public SelectMapperEvaluator(ExecutionContext executionContext, AbstractResolverHelper mapListResolver) {
        super(executionContext,mapListResolver);
    }

    private ApiService apiService;

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
    }

    static {

        // SimpleObjectEvaluator.PARAMETERS.add(FOREACH);
        SimpleObjectEvaluator.PARAMETERS.add(SELECT);
        SimpleObjectEvaluator.PARAMETERS.add(LIST);
        SimpleObjectEvaluator.PARAMETERS.add(SORTED_SET);
        SimpleObjectEvaluator.PARAMETERS.add(LIST_PARTITION);
        SimpleObjectEvaluator.PARAMETERS.add(LIST_UNPARTITION);


        SimpleObjectEvaluator.PARAMETERS.add(MAP);
        SimpleObjectEvaluator.PARAMETERS.add(REMAP);
        SimpleObjectEvaluator.PARAMETERS.add(FOREACH);
        SimpleObjectEvaluator.PARAMETERS.add(STEP);
        SimpleObjectEvaluator.PARAMETERS.add(GROUP_BY);

        SimpleObjectEvaluator.PARAMETERS.add(FIELD);
        SimpleObjectEvaluator.PARAMETERS.add(REMOVE);
        SimpleObjectEvaluator.PARAMETERS.add(COPY);
        SimpleObjectEvaluator.PARAMETERS.add(THIS);
        SimpleObjectEvaluator.PARAMETERS.add(UNION);
        SimpleObjectEvaluator.PARAMETERS.add(EXTEND);
        SimpleObjectEvaluator.PARAMETERS.add(KEYS);
        SimpleObjectEvaluator.PARAMETERS.add(VALUES);
        SimpleObjectEvaluator.PARAMETERS.add(TEMPLATE);
        SimpleObjectEvaluator.PARAMETERS.add(EXCLUDE_FIELDS);
        SimpleObjectEvaluator.PARAMETERS.add(API);

        SimpleObjectEvaluator.PARAMETERS.add(GET);
        SimpleObjectEvaluator.PARAMETERS.add(ADD);
        SimpleObjectEvaluator.PARAMETERS.add(PUT);

        SimpleObjectEvaluator.PARAMETERS.add(SORT);
        SimpleObjectEvaluator.PARAMETERS.add(LIMIT);

        SimpleObjectEvaluator.PARAMETERS.add(SUM);
        SimpleObjectEvaluator.PARAMETERS.add(AVG);
        SimpleObjectEvaluator.PARAMETERS.add(MIN);
        SimpleObjectEvaluator.PARAMETERS.add(MAX);

    }

    @Override
    protected Object nextFunctionEvaluate(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {
        if (function == MAX) {
            return getMax(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == MIN) {
            return getMin(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == AVG) {
            return getAvg(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == SUM) {
            return getSum(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == LIMIT) {
            return getLimit(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == SORT) {
            return getSort(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == PUT) {
            return getPut(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == GET) {
            return getGet(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == ADD) {
            return getAdd(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == TEMPLATE) {
            return getTemplate(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == LIST) {
            return getList(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == SORTED_SET) {
            return getSortedSet(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == LIST_PARTITION) {
            return getListPartition(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == LIST_UNPARTITION) {
            return getListUnPartition(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == MAP) {
            return getMap(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == REMAP) {
            return getReMap(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == FOREACH) {
            return getForeach(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == STEP) {
            return getStep(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == GROUP_BY) {
            return getGroupBy(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == SELECT) {
            return getSelect(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == REMOVE) {
            return getRemove(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == FIELD) {
            return getField(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == COPY) {
            return getCopy(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == THIS) {
            return getThis(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == UNION) {
            return getUnion(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == EXTEND) {
            return getExtend(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == KEYS) {
            return getKeys(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == VALUES) {
            return getValues(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == EXCLUDE_FIELDS) {
            return getExcludeFields(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == API) {
            return evaluateApiFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else {
            return superFunctionEvaluate(function, operands, argumentList, evaluationContext);
        }
    }

    private Object getMax(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1)
            return null;

        Collection collection = mapListResolver.resolveToCollection(value1);
        if (null == collection) {
            throw new ExpressionValidationException("Value should be of collection type");
        }

        Number max = null;
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Number number = TypeUtils.resolveNumber(iterator.next());
            if (null != number) {
                if (null == max) {
                    max = number;
                } else {
                    max = Math.max(max.doubleValue(), number.doubleValue());
                }
            }
        }
        return max;
    }

    private Object getMin(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1)
            return null;

        Collection collection = mapListResolver.resolveToCollection(value1);
        if (null == collection) {
            throw new ExpressionValidationException("Value should be of collection type");
        }

        Number min = null;
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Number number = TypeUtils.resolveNumber(iterator.next());
            if (null != number) {
                if (null == min) {
                    min = number;
                } else {
                    min = Math.min(min.doubleValue(), number.doubleValue());
                }
            }
        }
        return min;
    }

    private Number sum(Collection collection) {
        Number sum = null;
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Number number = TypeUtils.resolveNumber(iterator.next());
            if (null != number) {
                if (null == sum) {
                    sum = number;
                } else {
                    sum = sum.doubleValue() + number.doubleValue();
                }
            }
        }
        return sum;
    }

    private Object getSum(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1)
            return null;

        Collection collection = mapListResolver.resolveToCollection(value1);
        if (null == collection) {
            throw new ExpressionValidationException("Value should be of collection type");
        }

        return sum(collection);
    }

    private Object getAvg(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1)
            return null;

        Collection collection = mapListResolver.resolveToCollection(value1);
        if (null == collection) {
            throw new ExpressionValidationException("Value should be of collection type");
        }

        Number sum = sum(collection);
        if (null != sum) {
            return sum.doubleValue() / collection.size();
        }
        return null;
    }

    private Object getLimit(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1)
            return null;

        Integer limitSize = null;
        if (operands.hasNext()) {
            limitSize = TypeUtils.resolveInteger(operands.next());
            argumentList.pop();
        }


        List list = mapListResolver.resolveToList(value1);
        if (null == list) {
            Map<Object, Object> map = mapListResolver.resolveToMap(value1);
            if (null == map) {
                Collection set = mapListResolver.resolveToCollection(value1);
                if (null == set) {
                    return value1;
                } else {
                    return set.stream().limit(limitSize).collect(Collectors.toSet());
                }
            } else {
                Map limitedMap = map.entrySet().stream().limit(limitSize).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                return limitedMap;

            }
        } else {
            List limitedList = (List) list.stream().limit(limitSize).collect(Collectors.toList());
            return limitedList;
        }
    }

    private Object getSort(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1)
            return null;

        // we can sort List or Map
        // if List Object can be simple Number or simple String/char/boolean or complext Map
        String direction = "DESC";
        if (operands.hasNext()) {
            Object dirO = operands.next();
            argumentList.pop();
            direction = (String) dirO;
        }
        final String finalDirection = direction;

        String sortingComponent = "BY_KEY";
        if (operands.hasNext()) {
            sortingComponent = (String) operands.next();
            argumentList.pop();
        }
        final String finalSortingComponent = sortingComponent;

        List list = mapListResolver.resolveToList(value1);
        if (null == list) {
            Map m = mapListResolver.resolveToMap(value1);
            if (null == m) {
                return null;
            } else {
                Map sortedMap = SortingUtils.sortMap(m, finalDirection, finalSortingComponent);
                return sortedMap;
            }
        } else {

            List sortedList = SortingUtils.sortList(list, finalDirection, finalSortingComponent);
            return sortedList;
        }
    }

    private Object getListUnPartition(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1)
            return null;

        List l = mapListResolver.resolveToList(value1);
        if (null == l) {
            return value1;
        }

        List newList = new ArrayList();
        for (Object oList : l) {
            Collection lTmp = (Collection) oList;
            if (null != lTmp) {
                Iterator iTmp = lTmp.iterator();
                while (iTmp.hasNext()) {
                    Object t = iTmp.next();
                    newList.add(t);
                }
            } else {
                // TODO do we need to do something here??? - what if list contains some other type of objcts?
            }
        }
        return newList;
    }

    private Object getListPartition(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1)
            return null;

        List l = mapListResolver.resolveToList(value1);
        if (null == l) {
            throw new ExpressionValidationException("First item should be list");
        }

        Object value2 = operands.next();
        Token token2 = argumentList.pop();
        if (null == value2) {
            return value1;
        }

        Number n = 0;
        if (value2 instanceof Number) {
            n = (Number) value2;
        } else if (value2 instanceof String) {
            try {
                n = NumberUtils.createNumber((String) value2);
            } catch (Exception e) {
                throw new ExpressionValidationException("Second argument is not a number");
            }
        }

        List<List<Object>> lists = new ListPartition<>(l, n.intValue());

        return lists;
    }


    private Object evaluateApiFunction(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1) {
            throw new ExpressionValidationException("Key as first parameter cannot be null or empty");
        }
        if (!(value1 instanceof String)) {
            throw new ExpressionValidationException("Key as first parameter has to be type of String: " + value1.getClass().getName() + ", value: " + value1);
        }

        String apiKeyName = (String) value1;

        Object value2 = operands.next();
        Token token3 = argumentList.pop();

        if (null == apiService)
            return null;

        if (apiService.contains(apiKeyName)) {
            Object result = apiService.execute(apiKeyName, value2, evaluationContext);
            return result;
        }
        return null;
    }


   /* @Deprecated
    private Object foreach(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object fieldForeachValue = operands.next();
        Token tokenFieldValue = argumentList.pop();

        String actionValue = (String) operands.next();
        Token tokenAction = argumentList.pop();

       *//* switch (actionValue){
            case "TO_MAP"
        }
*//*

        return null;
    }*/

    private Object getPut(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1) {
            throw new ExpressionValidationException("Key as first parameter cannot be null or empty");
        }

        Map map = mapListResolver.resolveToMap(value1);
        if (null != map) {
            Object key = operands.next();
            argumentList.pop();

            if (null == key) {
                throw new ExpressionValidationException("Key as second parameter cannot be null or empty");
            }

            Object value = operands.next();
            argumentList.pop();

            map.put(key, value);
            return map;
        }
        return value1;
    }

    private Object getAdd(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1) {
            throw new ExpressionValidationException("Key as first parameter cannot be null or empty");
        }

        Collection col = mapListResolver.resolveToCollection(value1);
        if (null != col) {
            while (operands.hasNext()) {
                Object o = operands.next();
                argumentList.pop();
                col.add(o);
            }
            return col;
        }
        return value1;
    }

    private Object getGet(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1) {
            //throw new ExpressionValidationException("Key as first parameter cannot be null or empty");
            return null;
        }

        Object value2 = operands.next();
        Token token2 = argumentList.pop();

        if (null == value2) {
            return null;//throw new ExpressionValidationException("Value as second parameter cannot be null or empty");
        }

        Map map = mapListResolver.resolveToMap(value2);
        if (null != map) {
            if (map.containsKey(value1)) {
                return map.get(value1);
            }
        } else {
            List list = mapListResolver.resolveToList(value2);
            if (null != list) {
                Number n = null;
                if (value1 instanceof Number) {
                    n = (Number) value1;
                } else if (value1 instanceof String) {
                    n = NumberUtils.createNumber((String) value1);
                } else {
                    return null;//throw new ExpressionValidationException("In case of List, first parameter has to be Integer");
                }
                if (null != n && n.doubleValue() > -1) {
                    return list.get(n.intValue());
                }
            }
        }
        return null;
    }

    private Object getExcludeFields(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        while (operands.hasNext()) {
            Object value = operands.next();
            Token token = argumentList.removeLast();

            String key = token.getLookupLiteralValue();
            evaluationContext.remove(key);
        }

        return evaluationContext.getDataObject();
    }

    private Object getGroupBy(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object source = operands.next();
        argumentList.pop();

        if (null == source) {
            return null;
        }

        List list = mapListResolver.resolveToList(source);
        if (null == list) {
            throw new ExpressionValidationException("Source should be of List type");
        }

        if (!operands.hasNext())
            return source;

        Object propertySource = operands.next();
        argumentList.pop();

        Object childrenKey = null;
        if (operands.hasNext()) {
            childrenKey = operands.next();
            argumentList.pop();
        }

        Map properties = mapListResolver.resolveToMap(propertySource);
        List<Map<String,Object>> propertyList = new ArrayList();
        //List<Map<String, Object>> properties = mapListResolver.resolveToList(propertySource);
        if (null == properties) {
            String propertyName = (String) propertySource;
            if (null == propertyName)
                throw new ExpressionValidationException("Property to group by is not String nor properly defined Map");
            Map newMap = new HashMap();
            newMap.put("propertyName", propertyName);
            propertyList.add(newMap);
        }else{
            Object propertiesObject = properties.get("properties");
            propertyList = mapListResolver.resolveToList(propertiesObject);
        }

        GroupByUtils groupByUtils = new GroupByUtils(this, mapListResolver);
        if (null == childrenKey) {
            // return Map { prop1Val:[] ... } - we'll use only first Property from properties list
            Map<Object, List<Object>> result = groupByUtils.groupByProperty(list, propertyList.get(0));
            return result;
        } else {
            // return List of Maps [{ propName:propValue, propName1: propVale1, children: [] }] - we'll use only first Property from properties list
            String childrenKeyStr = "children";
            Map childrenKeyMap = mapListResolver.resolveToMap(childrenKey);
            if (null!=childrenKeyMap){
                Object chTmp = childrenKeyMap.get("childrenKey");
                if(null!=chTmp){
                    childrenKeyStr = (String)chTmp;
                }
            }else if (childrenKey instanceof String){
                childrenKeyStr = (String)childrenKey;
            }

            List<Map<Object, List<Object>>> result = groupByUtils.groupByProperties(list, propertyList, childrenKeyStr);
            return result;
        }
    }

    private Object getStep(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object source = operands.next();
        argumentList.pop();

        if (null == source) {
            return null;
        }

        List<String> steps = new ArrayList<>();
        Iterator<Token> iterator = argumentList.iterator();
        while (iterator.hasNext()) {
            Token stepObj = iterator.next();
            String step = (String) stepObj.getContent();
            steps.add(step);
            argumentList.pop();
        }

        Object stepResult = source;
        if (steps.size() > 0) {
            for (String step : steps) {
                Map context = new HashMap();
                context.put("_current", stepResult);
                context.put("_parent", evaluationContext.getDataObject());

                PathExtractor pathExtractor = new PathExtractor(context, mapListResolver);
                try {
                    if (step.startsWith("'#") && step.endsWith("#'")) {
                        step = step.substring(2, step.length() - 2);
                    }
                    stepResult = evaluate(step, pathExtractor);
                } catch (EvaluatorException e) {
                    LOG.error("Cannot execute Step expression: " + step, e);
                    return null;
                }
            }
        }

        return stepResult;

    }

    private Object getForeach(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object source = operands.next();
        argumentList.pop();

        if (null == source) {
            return null;
        }

        Object value2 = operands.next();
        argumentList.pop();
        if (null == value2) {
            return source;
        }

        Object parentObj = evaluationContext.getDataObject();
        if (operands.hasNext()) {
            parentObj = operands.next();
            argumentList.pop();
        }


        String expressionToExecute = TypeUtils.resolveString(value2);
        if (null == expressionToExecute || expressionToExecute.isEmpty()) {
            return source;
        }

        Map sourceMap = mapListResolver.resolveToMap(source);
        if (null == sourceMap) {
            Collection l = mapListResolver.resolveToCollection(source);
            if (null == l) {
                return source;
            } else {
                List newList = new ArrayList(l);

                ListIterator iterator = newList.listIterator();// l.iterator();
                int index = 0;
                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    Map<String, Object> map = new HashMap<>();
                    map.put("_index", index);
                    map.put("_current", next);
                    map.put("_prev", index > 0 ? newList.get(index - 1) : null);
                    map.put("_next", index == newList.size() - 1 ? null : newList.get(index + 1));
                    map.put("_parent", parentObj);

                    try {
                        Object res = evaluate(expressionToExecute, new PathExtractor(map, mapListResolver));
                        Map resMap = mapListResolver.resolveToMap(res);
                        if (null != resMap) {
                            resMap.remove("_index");
                            resMap.remove("_current");
                            resMap.remove("_prev");
                            resMap.remove("_next");
                            resMap.remove("_parent");
                            iterator.set(resMap);
                        } else {
                            iterator.set(res);
                        }
                        //newList.add(res);
                    } catch (EvaluatorException e) {
                        throw new ExpressionValidationException("Second parameter expression cannot be executed: " + expressionToExecute, e);
                    }
                    ++index;
                }

                return newList;
            }
        } else {
            try {
                Map map = new HashMap();
                map.put("_current", sourceMap);
                if (null != parentObj) {
                    map.put("_parent", parentObj);
                }

                Object res = evaluate(expressionToExecute, new PathExtractor(map, mapListResolver));
                return res;
            } catch (EvaluatorException e) {
                throw new ExpressionValidationException("Second parameter expression cannot be executed: " + expressionToExecute, e);
            }
        }
    }

    private Object getReMap(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object source = operands.next();
        argumentList.pop();

        if (null == source) {
            throw new ExpressionValidationException("First parameter cannot be null or empty");
        }

        Object value2 = operands.next();
        argumentList.pop();
        if (null == value2) {
            return source;
        }

        Map mapper = mapListResolver.resolveDeepMap(value2);
        if (null == mapper) {
            throw new ExpressionValidationException("Second parameter has to be Map type");
        }


        Map sourceMap = mapListResolver.resolveToMap(source);
        if (null == sourceMap) {
            List sourceList = mapListResolver.resolveToList(source);
            if (null == sourceList) {
                return source;
            } else {
                // handle list of Maps
                List newList = new ArrayList();
                for (Object o : sourceList) {
                    Map sourceListMap = mapListResolver.resolveToMap(o);
                    if (null == sourceListMap) {
                        newList.add(o);
                    } else {
                        Map mappedObject = remapMap(sourceListMap, mapper);
                        newList.add(mappedObject);
                    }
                }
                return newList;
            }
        } else {
            // handle single map
            return remapMap(sourceMap, mapper);
        }
    }

    private Map remapMap(Map<String, Object> source, Map<String, String> mapper) {
        Map<String, Object> newMap = new HashMap<>();
        for (String mapperKey : mapper.keySet()) {
            Object res = new PathExtractor(source, mapListResolver).extractObjectValue(mapperKey);
            newMap.put(mapper.get(mapperKey), res);
        }

        return newMap;
    }

    private Object getMap(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        Map<String, Object> map = new LinkedHashMap<>();
        // Token token = argumentList.pop();
        while (operands.hasNext()) {
            Object value = operands.next();
            argumentList.removeLast();
            if (null != value) {
                Map newMap = mapListResolver.resolveToMap(value);
               /* if (null == newMap)
                    throw new ExpressionValidationException("Value in MAP expression should be of Map type");*/

                if (null != newMap) {
                    map.putAll(newMap);
                }
            }
        }
        return map;

    }

    private Object getSortedSet(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Set<Object> set;
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null == value1) {
            while (operands.hasNext()) {
                operands.next();
                argumentList.pop();
            }
            return null;
        }

        String fieldsToSortBy = null;
        if (value1 instanceof String && ((String) value1).startsWith("FIELD_")) {
            fieldsToSortBy = (String) value1;
            String[] f = ((String) value1).split("_");
            String fieldName = f[1];
            String fieldTypeTmp = null;
            if (f.length > 2)
                fieldTypeTmp = f[2];
            String fieldType = fieldTypeTmp;

            Comparator<Object> comp = new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    if (null == fieldType) {
                        if (fieldName.equals("NUMBER")) {
                            Number n1 = (Number) o1;
                            Number n2 = (Number) o2;
                            return (new Double(n1.doubleValue())).compareTo(new Double(n2.doubleValue()));
                        } else {
                            String s1 = o1.toString();
                            String s2 = o2.toString();
                            return s1.compareTo(s2);
                        }
                    } else {
                        Map m1 = mapListResolver.resolveToMap(o1);
                        if (null != m1) {
                            Map m2 = mapListResolver.resolveToMap(o2);
                            if (null != m2) {
                                if (fieldType.equals("NUMBER")) {
                                    Number n1 = (Number) m1.get(fieldName);
                                    Number n2 = (Number) m2.get(fieldName);
                                    return (new Double(n1.doubleValue())).compareTo(new Double(n2.doubleValue()));
                                } else {
                                    String s1 = m1.get(fieldName).toString();
                                    String s2 = m2.get(fieldName).toString();
                                    return s1.compareTo(s2);
                                }
                            }
                        } else {
                            List l1 = mapListResolver.resolveToList(o1);
                            if (null != l1) {
                                List l2 = mapListResolver.resolveDeepList(o2);
                                if (l2 != null) {
                                    boolean b = l1.equals(l2);
                                    if (b)
                                        return 0;
                                    else return -1;
                                }
                            }
                        }
                    }

                    return 0;
                }
            };
            set = new TreeSet<>(comp);

        } else {
            set = new HashSet<>();
            if (value1 instanceof Collection) {
                set.addAll((Collection) value1);
            } else {
                set.add(value1);
            }
        }

        while (operands.hasNext()) {
            Object value = operands.next();
            argumentList.removeLast();

            if (value instanceof Collection) {
                set.addAll((Collection) value);
            } else {
                set.add(value);
            }
        }

        return new ArrayList(set);
    }


    private Object getList(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        List<Object> list = new ArrayList<>();
        while (operands.hasNext()) {
            Object value = operands.next();
            argumentList.removeLast();
            Collection listChild = mapListResolver.resolveToCollection(value);
            if (null != listChild) {
                list.addAll(listChild);
            } else {
                list.add(value);
            }
        }
        return list;
    }

    private Object getTemplate(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (operands.hasNext()) {
            Object value2 = operands.next(); // this could be used in future to define template engine
            Token token2 = argumentList.pop();
        }

        if (value1 instanceof String) {

            try {
                Object evaluated = evaluationContext.compileString((String) value1);
                if (evaluated instanceof String) {
                    String s = (String) evaluated;
                    if (s.startsWith("#") && s.endsWith("#")) {
                        s = s.trim().substring(1, s.length() - 1).trim();
                        return s;
                    }
                }
                return evaluated;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return value1;
    }

    private Object getField(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Map<Object, Object> map = new LinkedHashMap<>();
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        Object value2 = operands.next();
        Token token2 = argumentList.pop();

        boolean shouldIncludeNull = true;
        if (argumentList.size() > 0 && operands.hasNext()) {
            Object value3 = operands.next();
            Token token3 = argumentList.pop();

            if (null != value3) {
                if (value3 instanceof String) {
                    String s = (String) value3;
                    shouldIncludeNull = Boolean.parseBoolean(s);

                } else if (value3 instanceof Boolean) {
                    shouldIncludeNull = (Boolean) value3;
                }
            }
        }

        if (shouldIncludeNull) {
            map.put(value1, value2);
        } else {
            if (null != value2) {
                Collection collection = mapListResolver.resolveToCollection(value2);
                if (null != collection) {
                    if (collection.size() > 0) {
                        map.put(value1, value2);
                    }
                } else {
                    map.put(value1, value2);
                }
            }
        }


        return map;
    }

    private Object getValues(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value = operands.next();
        Token token = argumentList.pop();
        Map newMap = mapListResolver.resolveToMap(value);
        if (null != newMap) {
            List list = new ArrayList<>();

            Collection c = ((Map) value).values();
            List cL = new ArrayList<>(c);
            list.addAll(cL);
            return list;
        }
        return null;
    }

    private Object getKeys(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value = operands.next();
        Token token = argumentList.pop();
        Map newMap = mapListResolver.resolveToMap(value);
        if (null != newMap) {
            List list = new ArrayList<>();
            Collection c = ((Map) value).keySet();
            List cL = new ArrayList<>(c);
            for (Object o : cL) {
                list.add(o);
            }
            return list;
        }
        return null;
    }

    private Object getExtend(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Map<String, Object> map = new LinkedHashMap<>();
        while (operands.hasNext()) {
            Object value = operands.next();
            Token token = argumentList.pop();
            Map newMap = mapListResolver.resolveToMap(value);
            if (null != newMap) {
                map.putAll((Map) newMap);
            }

        }
        return map;
    }

    private Object getUnion(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        List<Object> list = new ArrayList<>();

        while (operands.hasNext()) {
            Object value = operands.next();
            if (argumentList.size() > 0) {
                Token token = argumentList.pop(); // just pop if exist - it will not exists if function was called before. Sample: union(keys($item$), values($item$))
            }
            Collection lst = mapListResolver.resolveToCollection(value);

            if (null != lst) {
                list.addAll(lst);
            } else {
                if (null != value)
                    list.add(value);
            }
        }
        return list;
    }

    private Object getThis(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        return evaluationContext.getDataObject();
    }

    private Object getCopy(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value = operands.next();
        Token token = argumentList.pop();

        Object newObject = value;
        Map newMap = mapListResolver.resolveToMap(value);
        if (null != newMap) {
            newObject = new LinkedHashMap<>((Map) value);
        } else {
            List newList = mapListResolver.resolveToList(value);
            if (null != newList) {
                newObject = new ArrayList(newList);
            }
        }

        return newObject;
    }

    private Object getRemove(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value = operands.next();
        Token token = argumentList.pop();
        String argumentName = (String) token.getContent();
        Object o = evaluationContext.remove(argumentName);
        return o;
    }


    private Object getSelect(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Map<String, Object> map = new LinkedHashMap<>();
        // Token token = argumentList.pop();
        while (operands.hasNext()) {
            Object value = operands.next();
            Token token = argumentList.removeLast();
            String argumentName = token.getContent().toString();

            String fieldName = argumentName;
            if (token.isLookupLiteral())
                fieldName = normalizeTokenName(argumentName);

            map.put(fieldName, value);
        }
        return map;
    }


}
