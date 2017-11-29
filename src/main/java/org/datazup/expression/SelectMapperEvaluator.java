package org.datazup.expression;


import org.apache.commons.lang3.math.NumberUtils;
import org.datazup.apiinternal.ApiService;
import org.datazup.expression.exceptions.ExpressionValidationException;
import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.pathextractor.PathExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by admin@datazup on 3/21/16.
 */
public class SelectMapperEvaluator extends SimpleObjectEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(SelectMapperEvaluator.class);

    @Deprecated
    public final static Function FOREACH = new Function("FOREACH", 3, Integer.MAX_VALUE);

    public final static Function SELECT = new Function("SELECT", 1, Integer.MAX_VALUE);
    public final static Function LIST = new Function("LIST", 1, Integer.MAX_VALUE);
    public final static Function MAP = new Function("MAP", 1, Integer.MAX_VALUE);
    public final static Function REMOVE = new Function("REMOVE", 1);
    public final static Function THIS = new Function("THIS", 0);
    public final static Function COPY = new Function("COPY", 1);
    public final static Function UNION = new Function("UNION", 1, Integer.MAX_VALUE);
    public final static Function EXTEND = new Function("EXTEND", 1, Integer.MAX_VALUE);
    public final static Function KEYS = new Function("KEYS", 1);
    public final static Function VALUES = new Function("VALUES", 1);
    public final static Function FIELD = new Function("FIELD",  2, 3);
    public final static Function TEMPLATE = new Function("T", 1, 2);
    public final static Function EXCLUDE_FIELDS = new Function("EXCLUDE_FIELDS",  1, Integer.MAX_VALUE);
    public final static Function API = new Function("API", 2, 2);

    public final static Function GET = new Function("GET",  2, 3);

    private static SelectMapperEvaluator INSTANCE;

    public static SelectMapperEvaluator getInstance(AbstractResolverHelper mapListResolver){
        synchronized (SelectMapperEvaluator.class){
            if (null==INSTANCE){
                synchronized (SelectMapperEvaluator.class){
                    if (null==INSTANCE)
                        INSTANCE = new SelectMapperEvaluator(mapListResolver);
                }
            }
        }
        return INSTANCE;
    }

    public SelectMapperEvaluator(AbstractResolverHelper mapListResolver){
        super(mapListResolver);
    }

    private ApiService apiService;

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
    }

    static {

        SimpleObjectEvaluator.PARAMETERS.add(FOREACH);
        SimpleObjectEvaluator.PARAMETERS.add(SELECT);
        SimpleObjectEvaluator.PARAMETERS.add(LIST);
        SimpleObjectEvaluator.PARAMETERS.add(MAP);
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

    }

    @Override
    protected Object nextFunctionEvaluate(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {

        //Random random = new Random();

        if (function == FOREACH){
            return foreach(function, operands, argumentList, (PathExtractor)evaluationContext);
        }else if (function == GET) {
            return getGet(function, operands, argumentList, (PathExtractor)evaluationContext);
        }else if (function == TEMPLATE) {
            return getTemplate(function, operands, argumentList, (PathExtractor) evaluationContext);
        }else if (function == LIST) {
            return getList(function, operands, argumentList, (PathExtractor) evaluationContext);
        }else if (function == MAP) {
            return getMap(function, operands, argumentList, (PathExtractor) evaluationContext);
        }else  if (function == SELECT) {
            return getSelect(function, operands, argumentList, (PathExtractor) evaluationContext);
        }
        else if (function == REMOVE) {
            return getRemove(function, operands, argumentList, (PathExtractor) evaluationContext);
        }else if (function == FIELD) {
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
        } else if (function == API){
            return evaluateApiFunction(function, operands, argumentList, (PathExtractor)evaluationContext);
        }
        else {
            return superFunctionEvaluate(function, operands, argumentList, evaluationContext);
        }

    }

    private Object evaluateApiFunction(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null==value1){
            throw new ExpressionValidationException("Key as first parameter cannot be null or empty");
        }
        if (!(value1 instanceof String)){
            throw new ExpressionValidationException("Key as first parameter has to be type of String: "+value1.getClass().getName()+", value: "+value1);
        }

        String apiKeyName = (String)value1;

        Object value2 = operands.next();
        Token token3 = argumentList.pop();

        if (null==apiService)
            return null;

        if (apiService.contains(apiKeyName)){
            return apiService.execute(apiKeyName, value2);
        }
        return null;
    }


    @Deprecated
    private Object foreach(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object fieldForeachValue = operands.next();
        Token tokenFieldValue = argumentList.pop();

        String actionValue = (String) operands.next();
        Token tokenAction = argumentList.pop();

       /* switch (actionValue){
            case "TO_MAP"
        }
*/

        return null;
    }

    private Object getGet(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (null==value1){
            throw new ExpressionValidationException("Key as first parameter cannot be null or empty");
        }

        Object value2 = operands.next();
        Token token2 = argumentList.pop();

        if (null==value2){
            throw new ExpressionValidationException("Value as second parameter cannot be null or empty");
        }

        Map map = mapListResolver.resolveToMap(value2);
        if (null!=map){
            if (map.containsKey(value1)){
                return map.get(value1);
            }
        }else{
            List list = mapListResolver.resolveToList(value2);
            if (null!=list){
                Number n = null;
                if (value1 instanceof Number){
                    n = (Number)value1;
                }else if (value1 instanceof String){
                    n = NumberUtils.createNumber((String) value1);
                }else{
                    throw new ExpressionValidationException("In case of List, first parameter has to be Integer");
                }
                if (null!=n){
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

    private Object getMap(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        Map<String, Object> map = new HashMap<>();
        // Token token = argumentList.pop();
        while (operands.hasNext()) {
            Object value = operands.next();
            argumentList.removeLast();
            if (null!=value) {
                Map newMap = mapListResolver.resolveToMap(value);
                if (null==newMap)
                    throw new ExpressionValidationException("Value in MAP expression should be of Map type");

                if (null != newMap) {
                    map.putAll(newMap);
                }
            }
        }
        return map;

    }

    private Object getList(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
       List<Object> list = new ArrayList<>();
        while (operands.hasNext()) {
            Object value = operands.next();
            argumentList.removeLast();
            List listChild = mapListResolver.resolveToList(value);
            if (null!=listChild){
                list.addAll(listChild);
            }else {
                list.add(value);
            }
        }
        return list;
    }

    private Object getTemplate(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (operands.hasNext()){
            Object value2 = operands.next(); // this could be used in future to define template engine
            Token token2 = argumentList.pop();
        }

        if (value1 instanceof String){

            try {
                Object evaluated = evaluationContext.compileString((String)value1);
                if (evaluated instanceof String){
                    String s = (String)evaluated;
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
        Map<Object, Object> map = new HashMap<>();
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        Object value2 = operands.next();
        Token token2 = argumentList.pop();

        boolean shouldIncludeNull = true;
        if (argumentList.size()>0 && operands.hasNext()){
            Object value3 = operands.next();
            Token token3 = argumentList.pop();

            if (null!=value3){
                if (value3 instanceof String){
                    String s = (String)value3;
                    shouldIncludeNull = Boolean.parseBoolean(s);

                }else if (value3 instanceof Boolean) {
                    shouldIncludeNull = (Boolean) value3;
                }
            }
        }

        if (shouldIncludeNull){
            map.put(value1, value2);
        }else{
            if (null!=value2){
                Collection collection = mapListResolver.resolveToCollection(value2);
                if (null!=collection){
                    if (collection.size()>0){
                        map.put(value1, value2);
                    }
                }else{
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
        if (null!=newMap) {
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
        if (null!=newMap) {
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
        Map<String, Object> map = new HashMap<>();
        while (operands.hasNext()) {
            Object value = operands.next();
            Token token = argumentList.pop();
            Map newMap = mapListResolver.resolveToMap(value);
            if (null!=newMap) {
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
            List lst = mapListResolver.resolveToList(value);

            if (null!=lst) {
                List l = (List)value;
                if (null!=l)
                    list.addAll((List) value);
            }else{
                if (null!=value)
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
        if (null!=newMap){
            newObject = new HashMap((Map)value);
        }else{
            List newList = mapListResolver.resolveToList(value);
            if (null!=newList){
                newObject = new ArrayList(newList);
            }
        }

        return newObject;
    }

    private Object getRemove(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value = operands.next();
        Token token = argumentList.pop();
        String argumentName = (String)token.getContent();
        Object o = evaluationContext.remove(argumentName);
        return o;
    }


    private Object getSelect(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Map<String, Object> map = new HashMap<>();
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
