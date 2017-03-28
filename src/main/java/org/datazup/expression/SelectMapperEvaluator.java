package org.datazup.expression;


import org.datazup.expression.exceptions.ExpressionValidationException;
import org.datazup.pathextractor.PathExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by ninel on 3/21/16.
 */
public class SelectMapperEvaluator extends SimpleObjectEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(SelectMapperEvaluator.class);

    public final static Function SELECT = new Function("SELECT", 1, Integer.MAX_VALUE);
    public final static Function LIST = new Function("LIST", 1, Integer.MAX_VALUE);
    public final static Function MAP = new Function("MAP", 1, Integer.MAX_VALUE);
    public final static Function REMOVE = new Function("REMOVE", 1);
    public final static Function COPY = new Function("COPY", 1);
    /* public final static Function REMOVE = new Function("remove", 1);*/
    public final static Function UNION = new Function("UNION", 1, Integer.MAX_VALUE);
    public final static Function EXTEND = new Function("EXTEND", 1, Integer.MAX_VALUE);
    public final static Function KEYS = new Function("KEYS", 1);
    public final static Function VALUES = new Function("VALUES", 1);
    public final static Function FIELD = new Function("FIELD",  2, 3);
    public final static Function TEMPLATE = new Function("T", 1, 2);

    private static SelectMapperEvaluator INSTANCE;

    public static SelectMapperEvaluator getInstance(){
        synchronized (SelectMapperEvaluator.class){
            if (null==INSTANCE){
                synchronized (SelectMapperEvaluator.class){
                    if (null==INSTANCE)
                        INSTANCE = new SelectMapperEvaluator();
                }
            }
        }
        return INSTANCE;
    }


    static {
        SimpleObjectEvaluator.PARAMETERS.add(SELECT);
        SimpleObjectEvaluator.PARAMETERS.add(LIST);
        SimpleObjectEvaluator.PARAMETERS.add(MAP);
        SimpleObjectEvaluator.PARAMETERS.add(FIELD);
        SimpleObjectEvaluator.PARAMETERS.add(REMOVE);
        SimpleObjectEvaluator.PARAMETERS.add(COPY);
        SimpleObjectEvaluator.PARAMETERS.add(UNION);
        SimpleObjectEvaluator.PARAMETERS.add(EXTEND);
        SimpleObjectEvaluator.PARAMETERS.add(KEYS);
        SimpleObjectEvaluator.PARAMETERS.add(VALUES);
        SimpleObjectEvaluator.PARAMETERS.add(TEMPLATE);
    }

    @Override
    protected Object nextFunctionEvaluate(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {
        if (function == TEMPLATE) {
            return getTemplate(function, operands, argumentList, (PathExtractor) evaluationContext);
        }
        if (function == LIST) {
            return getList(function, operands, argumentList, (PathExtractor) evaluationContext);
        }
        if (function == MAP) {
            return getMap(function, operands, argumentList, (PathExtractor) evaluationContext);
        }
        if (function == SELECT) {
            return getSelect(function, operands, argumentList, (PathExtractor) evaluationContext);
        }
        else if (function == REMOVE) {
            return getRemove(function, operands, argumentList, (PathExtractor) evaluationContext);
        }else if (function == FIELD) {
            return getField(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == COPY) {
            return getCopy(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == UNION) {
            return getUnion(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == EXTEND) {
            return getExtend(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == KEYS) {
            return getKeys(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == VALUES) {
            return getValues(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else {
            return superFunctionEvaluate(function, operands, argumentList, evaluationContext);
        }
    }

    private Object getMap(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        Map<String, Object> map = new HashMap<>();
        // Token token = argumentList.pop();
        while (operands.hasNext()) {
            Object value = operands.next();
            argumentList.removeLast();
            if (!(value instanceof Map)){
                throw new ExpressionValidationException("Value in MAP expression should be of Map type");
            }
            Map tmp = (Map)value;
            if (null!=tmp){
                map.putAll(tmp);
            }
        }
        return map;

    }

    private Object getList(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
       List<Object> list = new ArrayList<>();
        while (operands.hasNext()) {
            Object value = operands.next();
            list.add(value);
        }
        return list;
    }

    private Object getTemplate(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value1 = operands.next();
        Token token1 = argumentList.pop();

        if (operands.hasNext()){
            Object value2 = operands.next(); // this could be used in future do define template engine
            Token token2 = argumentList.pop();
        }

        if (value1 instanceof String){

            try {
                Object evaluated = evaluationContext.compileString((String)value1);
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
                    shouldIncludeNull = (Boolean) shouldIncludeNull;
                }
            }
        }

        if (shouldIncludeNull){
            map.put(value1, value2);
        }else{
            if (null!=value2){
                if (value2 instanceof List || value2 instanceof Map){
                    Collection c = (Collection)value2;
                    if (c.size()>0){
                        map.put(value1, value2);
                    }
                }else {
                    map.put(value1, value2);
                }
            }
        }


        return map;
    }

    private Object getValues(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value = operands.next();
        Token token = argumentList.pop();
        if (value instanceof Map) {
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
        if (value instanceof Map) {
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

            if (value instanceof Map) {
                map.putAll((Map) value);
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
            if (value instanceof List) {
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

    private Object getCopy(Function function, Iterator<Object> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        Object value = operands.next();
        Token token = argumentList.pop();
        return value;
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
