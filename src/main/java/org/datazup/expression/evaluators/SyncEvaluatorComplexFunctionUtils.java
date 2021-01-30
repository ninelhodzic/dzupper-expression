package org.datazup.expression.evaluators;

import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.AbstractEvaluator;
import org.datazup.expression.Function;
import org.datazup.expression.Token;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.expression.exceptions.ExpressionValidationException;
import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.pathextractor.PathExtractor;
import org.datazup.utils.GroupByUtils;
import org.datazup.utils.TypeUtils;

import java.util.*;

public class SyncEvaluatorComplexFunctionUtils extends EvaluatorBase {
    private AbstractEvaluator evaluator;
    public SyncEvaluatorComplexFunctionUtils(AbstractEvaluator abstractEvaluator, AbstractResolverHelper mapListResolver, ExecutionContext executionContext) {
        super(mapListResolver, executionContext);
        this.evaluator = abstractEvaluator;
    }

    public ContextWrapper getForeach(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper sourceC = operands.next();
        argumentList.pop();

        Object source = sourceC.get();

        if (null == source) {
            return wrap(null);
        }

        ContextWrapper value2C = operands.next();
        argumentList.pop();
        Object value2 = value2C.get();
        if (null == value2) {
            return wrap(source);
        }

        PathExtractor evaluationContext = (PathExtractor)abstractVariableSet;

        Object parentObj = evaluationContext.getDataObject();
        if (operands.hasNext()) {
            parentObj = operands.next().get();
            argumentList.pop();
        }

        String expressionToExecute = TypeUtils.resolveString(value2);
        if (null == expressionToExecute || expressionToExecute.isEmpty()) {
            return wrap(source);
        }

        Map sourceMap = mapListResolver.resolveToMap(source);
        if (null == sourceMap) {
            Collection l = mapListResolver.resolveToCollection(source);
            if (null == l) {
                return wrap(source);
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
                        ContextWrapper evaluated = evaluate(expressionToExecute, new PathExtractor(map, mapListResolver));
                        Object res = evaluated.get();
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

                return wrap(newList);
            }
        } else {
            try {
                Map map = new HashMap();
                map.put("_current", sourceMap);
                if (null != parentObj) {
                    map.put("_parent", parentObj);
                }

                ContextWrapper evaluated = evaluate(expressionToExecute, new PathExtractor(map, mapListResolver));
                /*Object res = evaluated.get();
                return res;*/
                return evaluated;
            } catch (EvaluatorException e) {
                throw new ExpressionValidationException("Second parameter expression cannot be executed: " + expressionToExecute, e);
            }
        }
    }


    public ContextWrapper getStep(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {
        ContextWrapper sourceC = operands.next();
        argumentList.pop();

        Object source = sourceC.get();

        if (null == source) {
            return wrap(null);
        }

        List<String> steps = new ArrayList<>();
        Iterator<Token> iterator = argumentList.iterator();
        while (iterator.hasNext()) {
            Token stepObj = iterator.next();
            String step = (String) stepObj.getContent();
            steps.add(step);
            //argumentList.pop();
            iterator.remove();
        }

        Object stepResult = source;
        if (steps.size() > 0) {
            PathExtractor evaluationContext = (PathExtractor)abstractVariableSet;

            for (String step : steps) {
                Map context = new HashMap();
                context.put("_current", stepResult);
                context.put("_parent", evaluationContext.getDataObject());

                PathExtractor pathExtractor = new PathExtractor(context, mapListResolver);
                try {
                    if (step.startsWith("'#") && step.endsWith("#'")) {
                        step = step.substring(2, step.length() - 2);
                    }
                    ContextWrapper evaluatedResult = evaluate(step, pathExtractor);
                    stepResult = evaluatedResult.get();
                } catch (EvaluatorException e) {
                    //LOG.error("Cannot execute Step expression: " + step, e);
                    //return wrap(null);
                    throw new ExpressionValidationException("Cannot execute Step expression: " + step, e);
                }
            }
        }

        return wrap(stepResult);
    }

    public ContextWrapper getGroupBy(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object abstractVariableSet) {

        ContextWrapper sourceC = operands.next();
        argumentList.pop();

        Object source = sourceC.get();

        if (null == source) {
            return wrap(null);
        }

        List list = mapListResolver.resolveToList(source);
        if (null == list) {
            throw new ExpressionValidationException("Source should be of List type");
        }

        if (!operands.hasNext())
            return wrap(source);

        Object propertySource = operands.next().get();
        argumentList.pop();

        Object childrenKey = null;
        if (operands.hasNext()) {
            childrenKey = operands.next().get();
            argumentList.pop();
        }

        Map properties = mapListResolver.resolveToMap(propertySource);
        List<Map<String, Object>> propertyList = new ArrayList();
        //List<Map<String, Object>> properties = mapListResolver.resolveToList(propertySource);
        if (null == properties) {
            String propertyName = (String) propertySource;
            if (null == propertyName)
                throw new ExpressionValidationException("Property to group by is not String nor properly defined Map");
            Map newMap = new HashMap();
            newMap.put("propertyName", propertyName);
            propertyList.add(newMap);
        } else {
            Object propertiesObject = properties.get("properties");
            propertyList = mapListResolver.resolveToList(propertiesObject);
        }

        GroupByUtils groupByUtils = new GroupByUtils(evaluator, mapListResolver);
        if (null == childrenKey) {
            // return Map { prop1Val:[] ... } - we'll use only first Property from properties list
            Map<Object, List<Object>> result = groupByUtils.groupByProperty(list, propertyList.get(0));
            return wrap(result);
        } else {
            // return List of Maps [{ propName:propValue, propName1: propVale1, children: [] }] - we'll use only first Property from properties list
            String childrenKeyStr = "children";
            Map childrenKeyMap = mapListResolver.resolveToMap(childrenKey);
            if (null != childrenKeyMap) {
                Object chTmp = childrenKeyMap.get("childrenKey");
                if (null != chTmp) {
                    childrenKeyStr = (String) chTmp;
                }
            } else if (childrenKey instanceof String) {
                childrenKeyStr = (String) childrenKey;
            }

            List<Map<Object, List<Object>>> result = groupByUtils.groupByProperties(list, propertyList, childrenKeyStr);
            return wrap(result);
        }
    }


    private ContextWrapper evaluate(String expressionToExecute, PathExtractor pathExtractor) throws EvaluatorException{
        return evaluator.evaluate(expressionToExecute, pathExtractor);
    }

}
