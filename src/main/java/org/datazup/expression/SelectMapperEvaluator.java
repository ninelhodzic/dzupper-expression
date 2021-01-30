package org.datazup.expression;


import org.apache.commons.lang3.math.NumberUtils;
import org.datazup.builders.mongo.MongoJsonQueryBuilder;
import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.context.ConcurrentExecutionContext;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.expression.evaluators.ComplexFunctionEvaluateUtils;
import org.datazup.expression.evaluators.FunctionEvaluateUtils;
import org.datazup.expression.evaluators.SyncEvaluatorComplexFunctionUtils;
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
    public final static Function SUBLIST = new Function("SUBLIST", 2, 3);


    public final static Function MAP = new Function("MAP", 0, Integer.MAX_VALUE);
    public final static Function REMAP = new Function("REMAP", 2, 2);
    public final static Function FOREACH = new Function("FOREACH", 2, 3);
    public final static Function STEP = new Function("STEP", 2, Integer.MAX_VALUE);

    public final static Function THIS = new Function("THIS", 0);
    public final static Function COPY = new Function("COPY", 1);
    public final static Function UNION = new Function("UNION", 1, Integer.MAX_VALUE);
    public final static Function EXTEND = new Function("EXTEND", 1, Integer.MAX_VALUE);
    public final static Function KEYS = new Function("KEYS", 1);
    public final static Function VALUES = new Function("VALUES", 1);
    public final static Function FIELD = new Function("FIELD", 2, 3);
    public final static Function TEMPLATE = new Function("T", 1, 2);
    public final static Function EXCLUDE_FIELDS = new Function("EXCLUDE_FIELDS", 1, Integer.MAX_VALUE);
    //public final static Function API = new Function("API", 2, 2);

    public final static Function GROUP_BY = new Function("GROUP_BY", 2, 3);


    public final static Function GET = new Function("GET", 2, 3);
    public final static Function ADD = new Function("ADD", 2, Integer.MAX_VALUE);
    public final static Function PUT = new Function("PUT", 3, 3);
    public final static Function REMOVE = new Function("REMOVE", 1, Integer.MAX_VALUE);

    public final static Function SORT = new Function("SORT", 2, 3);
    public final static Function LIMIT = new Function("LIMIT", 2, 2);

    public final static Function SUM = new Function("SUM", 1);
    public final static Function AVG = new Function("AVG", 1);
    public final static Function MAX = new Function("MAX", 1);
    public final static Function MIN = new Function("MIN", 1);


    public final static Function TO_DB_QUERY = new Function("TO_DB_QUERY", 1, 2);

    private ComplexFunctionEvaluateUtils complexFunctionEvaluateUtils;
    private SyncEvaluatorComplexFunctionUtils syncEvaluatorComplexFunctionUtils;

    private static SelectMapperEvaluator INSTANCE;
    static ConcurrentExecutionContext executionContext = new ConcurrentExecutionContext();

    public static SelectMapperEvaluator getInstance(AbstractResolverHelper mapListResolver) {
        return getInstance(executionContext, mapListResolver);
    }

    public static SelectMapperEvaluator getInstance(ExecutionContext executionContext, AbstractResolverHelper mapListResolver) {
        synchronized (SelectMapperEvaluator.class) {
            if (null == INSTANCE) {
                synchronized (SelectMapperEvaluator.class) {
                    if (null == INSTANCE)
                        INSTANCE = new SelectMapperEvaluator(executionContext, mapListResolver);
                }
            }
        }
        return INSTANCE;
    }

    public SelectMapperEvaluator(ExecutionContext executionContext, AbstractResolverHelper mapListResolver) {
        super(executionContext, mapListResolver);
        complexFunctionEvaluateUtils = new ComplexFunctionEvaluateUtils(mapListResolver, executionContext);
        syncEvaluatorComplexFunctionUtils = new SyncEvaluatorComplexFunctionUtils(this, mapListResolver, executionContext);
    }

    static {

        SimpleObjectEvaluator.PARAMETERS.add(SELECT);
        SimpleObjectEvaluator.PARAMETERS.add(LIST);
        SimpleObjectEvaluator.PARAMETERS.add(SORTED_SET);
        SimpleObjectEvaluator.PARAMETERS.add(LIST_PARTITION);
        SimpleObjectEvaluator.PARAMETERS.add(LIST_UNPARTITION);
        SimpleObjectEvaluator.PARAMETERS.add(SUBLIST);


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

        SimpleObjectEvaluator.PARAMETERS.add(GET);
        SimpleObjectEvaluator.PARAMETERS.add(ADD);
        SimpleObjectEvaluator.PARAMETERS.add(PUT);

        SimpleObjectEvaluator.PARAMETERS.add(SORT);
        SimpleObjectEvaluator.PARAMETERS.add(LIMIT);

        SimpleObjectEvaluator.PARAMETERS.add(SUM);
        SimpleObjectEvaluator.PARAMETERS.add(AVG);
        SimpleObjectEvaluator.PARAMETERS.add(MIN);
        SimpleObjectEvaluator.PARAMETERS.add(MAX);

        SimpleObjectEvaluator.PARAMETERS.add(TO_DB_QUERY);


    }

    @Override
    protected ContextWrapper evaluate(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {

        //evaluate("asd", evaluationContext);

        if (function == TO_DB_QUERY) {
            //return jsonToDbQuery(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getToDbQuery(function, operands, argumentList, evaluationContext);
        } else if (function == MAX) {
            // return getMax(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getMax(function, operands, argumentList, evaluationContext);
        } else if (function == MIN) {
            //return getMin(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getMin(function, operands, argumentList, evaluationContext);
        } else if (function == AVG) {
            //return getAvg(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getAvg(function, operands, argumentList, evaluationContext);
        } else if (function == SUM) {
            //return getSum(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getSum(function, operands, argumentList, evaluationContext);
        } else if (function == LIMIT) {
            //return getLimit(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getLimit(function, operands, argumentList, evaluationContext);
        } else if (function == SORT) {
            //return getSort(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getSort(function, operands, argumentList, evaluationContext);
        } else if (function == PUT) {
            // return getPut(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getPut(function, operands, argumentList, evaluationContext);
        } else if (function == GET) {
            // return getGet(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getGet(function, operands, argumentList, evaluationContext);
        } else if (function == ADD) {
            //  return getAdd(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getAdd(function, operands, argumentList, evaluationContext);
        } else if (function == TEMPLATE) {
            //return getTemplate(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getTemplate(function, operands, argumentList, evaluationContext);
        } else if (function == LIST) {
            //return getList(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getList(function, operands, argumentList, evaluationContext);
        } else if (function == SORTED_SET) {
            // return getSortedSet(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getSortedList(function, operands, argumentList, evaluationContext);
        } else if (function == LIST_PARTITION) {
            //return getListPartition(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getListPartition(function, operands, argumentList, evaluationContext);
        } else if (function == LIST_UNPARTITION) {
            //return getListUnPartition(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getListUnpartition(function, operands, argumentList, evaluationContext);
        } else if (function == SUBLIST) {
            // return getListSublist(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getSublist(function, operands, argumentList, evaluationContext);
        } else if (function == MAP) {
            //return getMap(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getMap(function, operands, argumentList, evaluationContext);
        } else if (function == REMAP) {
            //     return getReMap(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getRemap(function, operands, argumentList, evaluationContext);
        } else if (function == FOREACH) {
            //return getForeach(function, operands, argumentList, (PathExtractor) evaluationContext);
            return syncEvaluatorComplexFunctionUtils.getForeach(function, operands, argumentList, evaluationContext);
        } else if (function == STEP) {
            //    return getStep(function, operands, argumentList, (PathExtractor) evaluationContext);
            return syncEvaluatorComplexFunctionUtils.getStep(function, operands, argumentList, evaluationContext);
        } else if (function == GROUP_BY) {
            //   return getGroupBy(function, operands, argumentList, (PathExtractor) evaluationContext);
            return syncEvaluatorComplexFunctionUtils.getGroupBy(function, operands, argumentList, evaluationContext);
        } else if (function == SELECT) {
            //return getSelect(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getSelect(function, operands, argumentList, evaluationContext);
        } else if (function == REMOVE) {
            //   return getRemove(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getRemove(function, operands, argumentList, evaluationContext);
        } else if (function == FIELD) {
            // return getField(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getField(function, operands, argumentList, evaluationContext);
        } else if (function == COPY) {
            // return getCopy(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getCopy(function, operands, argumentList, evaluationContext);
        } else if (function == THIS) {
            // return getThis(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getThis(function, operands, argumentList, evaluationContext);
        } else if (function == UNION) {
            //  return getUnion(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getUnion(function, operands, argumentList, evaluationContext);
        } else if (function == EXTEND) {
            // return getExtend(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getExtend(function, operands, argumentList, evaluationContext);
        } else if (function == KEYS) {
            // return getKeys(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getKeys(function, operands, argumentList, evaluationContext);
        } else if (function == VALUES) {
            //  return getValues(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getValues(function, operands, argumentList, evaluationContext);
        } else if (function == EXCLUDE_FIELDS) {
            //  return getExcludeFields(function, operands, argumentList, (PathExtractor) evaluationContext);
            return complexFunctionEvaluateUtils.getExcludeFields(function, operands, argumentList, evaluationContext);
        } else {
            return super.evaluate(function, operands, argumentList, evaluationContext);
        }
    }

}
