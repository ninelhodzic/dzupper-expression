package org.datazup.expression.evaluators;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.datazup.builders.mongo.MongoJsonQueryBuilder;
import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.Function;
import org.datazup.expression.SelectMapperEvaluator;
import org.datazup.expression.Token;
import org.datazup.expression.context.ContextWrapper;
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

public class ComplexFunctionEvaluateUtils  extends EvaluatorBase {
    private static final Logger LOG = LoggerFactory.getLogger(ComplexFunctionEvaluateUtils.class);

    public ComplexFunctionEvaluateUtils(AbstractResolverHelper mapListResolver, ExecutionContext executionContext) {
        super(mapListResolver, executionContext);
    }

    public ContextWrapper getSelect(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        Map<String, Object> map = new LinkedHashMap<>();
        // Token token = argumentList.pop();
        while (operands.hasNext()) {
            ContextWrapper valueC = operands.next();
            Token token = argumentList.removeLast();
            String argumentName = token.getContent().toString();

            Object value = valueC.get();

            String fieldName = argumentName;
            if (token.isLookupLiteral())
                fieldName = normalizeTokenName(argumentName);

            map.put(fieldName, value);
        }
        return wrap(map);
    }

    protected String normalizeTokenName(String tokenName) {
        if (tokenName.startsWith("$") && tokenName.endsWith("$")) {
            tokenName = tokenName.substring(1, tokenName.length() - 1);
        }
        if (tokenName.contains(".")) {
            StringBuilder builder = new StringBuilder();
            String[] splitted = tokenName.split("\\.");
            int counter = 0;
            for (String s : splitted) {
                if (counter == 0) {
                    builder.append(s);
                } else {
                    String sc = StringUtils.capitalize(s);
                    builder.append(sc);
                }
                counter++;
            }
            tokenName = builder.toString();
        }
        return tokenName;
    }

    public ContextWrapper getList(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        List<Object> list = new ArrayList<>();
        while (operands.hasNext()) {
            ContextWrapper contextWrapper = operands.next();
            Object value = contextWrapper.get();
            argumentList.removeLast();
            list.add(value);
        }
        return wrap(list);
    }

    public ContextWrapper getSortedList(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        Set<Object> set;
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        Object value1 = value1C.get();

        if (null == value1) {
            while (operands.hasNext()) {
                operands.next(); // do we need this here???
                argumentList.pop();
            }
            return wrap(null);
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
            Object value = operands.next().get();
            argumentList.removeLast();

            if (value instanceof Collection) {
                set.addAll((Collection) value);
            } else {
                set.add(value);
            }
        }

        return wrap(new ArrayList(set));
    }

    public ContextWrapper getListPartition(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        Object value1 = value1C.get();

        if (null == value1)
            return wrap(null);

        List l = mapListResolver.resolveToList(value1);
        if (null == l) {
            throw new ExpressionValidationException("First item should be list");
        }

        Object value2 = operands.next().get();
        Token token2 = argumentList.pop();
        if (null == value2) {
            return wrap(value1);
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

        return wrap(lists);
    }

    public ContextWrapper getListUnpartition(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();
        Object value1 = value1C.get();

        if (null == value1)
            return wrap(null);

        List l = mapListResolver.resolveToList(value1);
        if (null == l) {
            return wrap(value1);
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
        return wrap(newList);
    }

    public ContextWrapper getSublist(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        argumentList.pop();
        Object listVal = value1C.get();

        ContextWrapper fromC = operands.next();
        argumentList.pop();

        ContextWrapper toC = null;
        if (operands.hasNext()) {
            toC = operands.next();
            argumentList.pop();
        }

        List list = mapListResolver.resolveToList(listVal);
        if (null == list) {
            return value1C;
        }

        Integer from = TypeUtils.resolveInteger(fromC.get());
        if (null == from) {
            return value1C;
        }

        Integer to = list.size();
        if (null != toC) {
            to = TypeUtils.resolveInteger(toC.get(), to);
        }

        List rangeList = new ArrayList(list.subList(from, to));

        return wrap(rangeList);

    }

    public ContextWrapper getMap(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {

        Map<String, Object> map = new LinkedHashMap<>();
        // Token token = argumentList.pop();
        while (operands.hasNext()) {
            ContextWrapper valueC = operands.next();
            argumentList.removeLast();
            Object value = valueC.get();

            if (null != value) {
                Map newMap = mapListResolver.resolveToMap(value);
                if (null != newMap) {
                    map.putAll(newMap);
                } else {
                    List list = mapListResolver.resolveToList(value);
                    if (null != list) {
                        for (Object o : list) {
                            Map tmp = mapListResolver.resolveToMap(o);
                            if (null != tmp) {
                                map.putAll(tmp);
                            }
                        }
                    }
                }
            }
        }

        return wrap(map);

    }

    public ContextWrapper getRemap(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper sourceC = operands.next();
        argumentList.pop();

        Object source = sourceC.get();

        if (null == source) {
            throw new ExpressionValidationException("First parameter cannot be null or empty");
        }

        ContextWrapper value2C = operands.next();
        argumentList.pop();

        Object value2 = value2C.get();

        if (null == value2) {
            return wrap(source);
        }

        Map mapper = mapListResolver.resolveDeepMap(value2);
        if (null == mapper) {
            throw new ExpressionValidationException("Second parameter has to be Map type");
        }


        Map sourceMap = mapListResolver.resolveToMap(source);
        if (null == sourceMap) {
            List sourceList = mapListResolver.resolveToList(source);
            if (null == sourceList) {
                return wrap(source);
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
                return wrap(newList);
            }
        } else {
            // handle single map
            return wrap(remapMap(sourceMap, mapper));
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

    public ContextWrapper getField(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        Map<Object, Object> map = new LinkedHashMap<>();
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        ContextWrapper value2C = operands.next();
        Token token2 = argumentList.pop();

        Object value1 = value1C.get();
        Object value2 = value2C.get();

        boolean shouldIncludeNull = true;
        if (argumentList.size() > 0 && operands.hasNext()) {
            ContextWrapper value3C = operands.next();
            Token token3 = argumentList.pop();
            Object value3 = value3C.get();

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
        return wrap(map);
    }

    public ContextWrapper getRemove(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper valueC = operands.next();

        Object valueO = valueC.get();
        if (null == valueO) {
            return wrap(null);
        }

        if (!operands.hasNext()) {
            return valueC;
        }

        Map valueMap = mapListResolver.resolveToMap(valueO);
        if (null == valueMap) {
            List valueList = mapListResolver.resolveToList(valueO);
            if (null == valueList) {
                return valueC;
            } else {
                while (operands.hasNext()) {
                    ContextWrapper tmpC = operands.next();
                    argumentList.pop();
                    Integer index = TypeUtils.resolveInteger(tmpC.get());
                    if (null != index) {
                        valueList.remove(index);
                    }
                }
                return wrap(valueList);
            }
        } else {
            while (operands.hasNext()) {
                ContextWrapper tmpC = operands.next();
                argumentList.pop();
                Object keyToRemove = tmpC.get();
                valueMap.remove(keyToRemove);
            }
            return wrap(valueMap);
        }
    }

    public ContextWrapper getCopy(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper valueC = operands.next();
        Token token = argumentList.pop();

        Object value = valueC.get();


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

        return wrap(newObject);
    }

    public ContextWrapper getThis(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        PathExtractor evaluationContext = (PathExtractor)abstractVariableSet;
        return wrap(evaluationContext.getDataObject());
    }

    public ContextWrapper getUnion(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        List<Object> list = new ArrayList<>();

        while (operands.hasNext()) {
            ContextWrapper valueC = operands.next();
            if (argumentList.size() > 0) {
                Token token = argumentList.pop(); // just pop if exist - it will not exists if function was called before. Sample: union(keys($item$), values($item$))
            }
            Object value = valueC.get();
            Collection lst = mapListResolver.resolveToCollection(value);

            if (null != lst) {
                list.addAll(lst);
            } else {
                if (null != value)
                    list.add(value);
            }
        }
        return wrap(list);
    }

    public ContextWrapper getExtend(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        Map<String, Object> map = new LinkedHashMap<>();
        while (operands.hasNext()) {
            ContextWrapper valueC = operands.next();
            Token token = argumentList.pop();
            Object value = valueC.get();

            Map newMap = mapListResolver.resolveToMap(value);
            if (null != newMap) {
                map.putAll((Map) newMap);
            }

        }
        return wrap(map);
    }

    public ContextWrapper getKeys(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper valueC = operands.next();
        Token token = argumentList.pop();
        Object value = valueC.get();
        Map newMap = mapListResolver.resolveToMap(value);
        if (null != newMap) {
            List list = new ArrayList<>();
            Collection c = ((Map) value).keySet();
            List cL = new ArrayList<>(c);
            for (Object o : cL) {
                list.add(o);
            }
            return wrap(list);
        }
        return wrap(null);
    }

    public ContextWrapper getValues(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper valueC = operands.next();
        Token token = argumentList.pop();
        Object value = valueC.get();

        Map newMap = mapListResolver.resolveToMap(value);
        if (null != newMap) {
            List list = new ArrayList<>();

            Collection c = ((Map) value).values();
            List cL = new ArrayList<>(c);
            list.addAll(cL);
            return wrap(list);
        }
        return wrap(null);
    }

    public ContextWrapper getTemplate(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        Object value1 = value1C.get();

        if (operands.hasNext()) {
            Object value2 = operands.next(); // this could be used in future to define template engine
            Token token2 = argumentList.pop();
        }

        if (value1 instanceof String) {
            try {
                PathExtractor evaluationContext = (PathExtractor)abstractVariableSet;

                Object evaluated = evaluationContext.compileString((String) value1);
                if (evaluated instanceof String) {
                    String s = (String) evaluated;
                    if (s.startsWith("#") && s.endsWith("#")) {
                        s = s.trim().substring(1, s.length() - 1).trim();
                        return wrap(s);
                    }
                }
                return wrap(evaluated);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return wrap(value1);
    }

    public ContextWrapper getExcludeFields(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        PathExtractor evaluationContext = (PathExtractor)abstractVariableSet;
        while (operands.hasNext()) {
            Object value = operands.next(); // WE NEED TO CALL NEXT
            Token token = argumentList.removeLast();

            String key = token.getLookupLiteralValue();
            evaluationContext.remove(key);
        }

        return wrap(evaluationContext.getDataObject());
    }

    public ContextWrapper getGet(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();
        Object value1 = value1C.get();

        if (null == value1) {
            //throw new ExpressionValidationException("Key as first parameter cannot be null or empty");
            return wrap(null);
        }

        ContextWrapper value2C = operands.next();
        Token token2 = argumentList.pop();

        Object value2 = value2C.get();

        if (null == value2) {
            return wrap(null);//throw new ExpressionValidationException("Value as second parameter cannot be null or empty");
        }

        Map map = mapListResolver.resolveToMap(value2);
        if (null != map) {
            if (map.containsKey(value1)) {
                return wrap(map.get(value1));
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
                    return wrap(null);//throw new ExpressionValidationException("In case of List, first parameter has to be Integer");
                }
                if (null != n && n.doubleValue() > -1) {
                    return wrap(list.get(n.intValue()));
                }
            }
        }
        return wrap(null);
    }

    public ContextWrapper getPut(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        argumentList.pop();
        Object value1 = value1C.get();

        if (null == value1) {
            throw new ExpressionValidationException("Key as first parameter cannot be null or empty");
        }

        Map map = mapListResolver.resolveToMap(value1);
        if (null != map) {
            Object key = operands.next().get();
            argumentList.pop();

            if (null == key) {
                throw new ExpressionValidationException("Key as second parameter cannot be null or empty");
            }

            Object value = operands.next().get();
            argumentList.pop();

            map.put(key, value);
            return wrap(map);
        }
        return wrap(value1);
    }

    public ContextWrapper getAdd(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {

        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();
        Object value1 = value1C.get();

        if (null == value1) {
            throw new ExpressionValidationException("Key as first parameter cannot be null or empty");
        }

        Collection col = mapListResolver.resolveToCollection(value1);
        if (null != col) {
            while (operands.hasNext()) {
                Object o = operands.next().get();
                argumentList.pop();
                col.add(o);
            }
            return wrap(col);
        }
        return wrap(value1);
    }

    public ContextWrapper getSort(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        Object value1 = value1C.get();

        if (null == value1)
            return wrap(null);

        // we can sort List or Map
        // if List Object can be simple Number or simple String/char/boolean or complext Map
        String direction = "DESC";
        if (operands.hasNext()) {
            Object dirO = operands.next().get();
            argumentList.pop();
            direction = (String) dirO;
        }
        final String finalDirection = direction;

        String sortingComponent = "BY_KEY";
        if (operands.hasNext()) {
            sortingComponent = (String) operands.next().get();
            argumentList.pop();
        }
        final String finalSortingComponent = sortingComponent;

        List list = mapListResolver.resolveToList(value1);
        if (null == list) {
            Map m = mapListResolver.resolveToMap(value1);
            if (null == m) {
                return wrap(null);
            } else {
                Map sortedMap = SortingUtils.sortMap(m, finalDirection, finalSortingComponent);
                return wrap(sortedMap);
            }
        } else {

            List sortedList = SortingUtils.sortList(list, finalDirection, finalSortingComponent);
            return wrap(sortedList);
        }
    }

    public ContextWrapper getLimit(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        Object value1 = value1C.get();

        if (null == value1)
            return wrap(null);

        Integer limitSize = null;
        if (operands.hasNext()) {
            limitSize = TypeUtils.resolveInteger(operands.next().get());
            argumentList.pop();
        }

        List list = mapListResolver.resolveToList(value1);
        if (null == list) {
            Map<Object, Object> map = mapListResolver.resolveToMap(value1);
            if (null == map) {
                Collection set = mapListResolver.resolveToCollection(value1);
                if (null == set) {
                    return wrap(value1);
                } else {
                    return wrap(set.stream().limit(limitSize).collect(Collectors.toSet()));
                }
            } else {
                Map limitedMap = map.entrySet().stream().limit(limitSize).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                return wrap(limitedMap);

            }
        } else {
            List limitedList = (List) list.stream().limit(limitSize).collect(Collectors.toList());
            return wrap(limitedList);
        }

    }

    public ContextWrapper getSum(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        Object value1 = value1C.get();

        if (null == value1)
            return wrap(null);

        Collection collection = mapListResolver.resolveToCollection(value1);
        if (null == collection) {
            throw new ExpressionValidationException("Value should be of collection type");
        }

        return wrap(sum(collection));
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

    public ContextWrapper getAvg(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        Object value1 = value1C.get();

        if (null == value1)
            return wrap(null);

        Collection collection = mapListResolver.resolveToCollection(value1);
        if (null == collection) {
            throw new ExpressionValidationException("Value should be of collection type");
        }

        Number sum = sum(collection);
        if (null != sum) {
            return wrap(sum.doubleValue() / collection.size());
        }
        return wrap(null);
    }

    public ContextWrapper getMax(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        Object value1 = value1C.get();

        if (null == value1)
            return wrap(null);

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
        return wrap(max);
    }

    public ContextWrapper getMin(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        Object value1 = value1C.get();

        if (null == value1)
            return wrap(null);

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
        return wrap(min);
    }

    public ContextWrapper getToDbQuery(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper value1C = operands.next();
        Token token1 = argumentList.pop();

        Object value1 = value1C.get();

        if (null == value1)
            return wrap(null);

        String queryType = "MONGO_DB";
        Map<String, Object> jsonQuery = null;
        if (value1 instanceof String) {
            String value1S = (String) value1;
            Map<String, Object> tmp = mapListResolver.resolveToMap(value1);
            if (tmp != null) {
                jsonQuery = tmp;
            } else {
                queryType = value1S;
            }
        } else {
            jsonQuery = mapListResolver.resolveToMap(value1);
        }
        if (null == jsonQuery) {
            ContextWrapper value2C = operands.next();
            Token token2 = argumentList.pop();

            Object value2 = value2C.get();

            if (null == value2)
                return wrap(null);

            jsonQuery = mapListResolver.resolveToMap(value2);
        }
        if (null == jsonQuery) {
            return wrap(null);
        }

        switch (queryType) {
            case "MONGO_DB":
                MongoJsonQueryBuilder queryBuilder = new MongoJsonQueryBuilder(jsonQuery, mapListResolver);
                Map query = queryBuilder.execute();
                return wrap(query);
            default:
                throw new ExpressionValidationException("Only MongoDb query is supported");
        }

    }

}
