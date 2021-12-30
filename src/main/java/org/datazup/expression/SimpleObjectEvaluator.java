package org.datazup.expression;

import org.apache.commons.lang3.StringUtils;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.expression.evaluators.FunctionEvaluateUtils;
import org.datazup.expression.evaluators.OperatorEvaluateUtils;
import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.pathextractor.AbstractVariableSet;
import org.datazup.pathextractor.SimpleResolverHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.Iterator;

/**
 * Created by admin@datazup on 3/14/16.
 */
public class SimpleObjectEvaluator extends AbstractEvaluator {
    protected static final Logger LOG = LoggerFactory.getLogger(SimpleObjectEvaluator.class);

    public final static Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 3);
    private static final Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
    public final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);

    public final static Operator NOT_EQUAL = new Operator("!=", 2, Operator.Associativity.LEFT, 4);
    public final static Operator EQUAL = new Operator("==", 2, Operator.Associativity.LEFT, 5);
    public final static Operator GREATER_THEN = new Operator(">", 2, Operator.Associativity.LEFT, 6);
    public final static Operator GREATER_THEN_OR_EQUAL = new Operator(">=", 2, Operator.Associativity.LEFT, 6);

    public final static Operator LOWER_THEN = new Operator("<", 2, Operator.Associativity.LEFT, 7);
    public final static Operator LOWER_THEN_OR_EQUAL = new Operator("<=", 2, Operator.Associativity.LEFT, 7);


    public final static Operator PLUS = new Operator("+", 2, Operator.Associativity.LEFT, 8);
    public final static Operator MINUS = new Operator("-", 2, Operator.Associativity.LEFT, 9);
    public final static Operator MULTIPLY = new Operator("*", 2, Operator.Associativity.LEFT, 10);
    public final static Operator DIVIDE = new Operator("/", 2, Operator.Associativity.LEFT, 11);

    public final static Operator MODULO = new Operator("%", 2, Operator.Associativity.LEFT, 12);
    public final static Operator POW = new Operator("^", 2, Operator.Associativity.LEFT, 13);

    public final static Function IS_NULL = new Function("IS_NULL", 1);
    public final static Function SET_NULL = new Function("SET_NULL", 1);
    public final static Function SIZE_OF = new Function("SIZE_OF", 1);
    public final static Function TYPE_OF = new Function("TYPE_OF", 1);
    public final static Function IS_OF_TYPE = new Function("IS_OF_TYPE", 2);

    public final static Function IF = new Function("IF", 3);

    // date functions
    public final static Function NOW = new Function("NOW", 0);
    public final static Function DATE_NOW = new Function("DATE_NOW", 0);
    public final static Function STR_TO_DATE_TIMESTAMP = new Function("STR_TO_DATE_TIMESTAMP", 2);

    public final static Function MINUTE = new Function("MINUTE", 1);
    public final static Function HOUR = new Function("HOUR", 1);
    public final static Function DAY = new Function("DAY", 1);
    public final static Function WEEK = new Function("WEEK", 1);
    public final static Function WEEK_OF_YEAR = new Function("WEEK_OF_YEAR", 1);

    public final static Function MONTH = new Function("MONTH", 1);
    public final static Function YEAR = new Function("YEAR", 1);

    public final static Function TRUE = new Function("TRUE", 0);
    public final static Function FALSE = new Function("FALSE", 0);

    public final static Function DATE_DIFF = new Function("DATE_DIFF", 3);//firstDate, secondDate, TimeUnit

    public final static Function DATE_MINUS = new Function("DATE_MINUS", 3);//firstDate, secondDate, TimeUnit
    public final static Function DATE_PLUS = new Function("DATE_PLUS", 3);//firstDate, secondDate, TimeUnit
    public final static Function DATE_START = new Function("DATE_START", 1);//firstDate, secondDate, TimeUnit
    public final static Function DATE_END = new Function("DATE_END", 1);//firstDate, secondDate, TimeUnit

    public final static Function TO_DATE = new Function("TO_DATE", 1, 3);
    public final static Function TO_INT = new Function("TO_INT", 1);
    public final static Function TO_LONG = new Function("TO_LONG", 1);
    public final static Function TO_DOUBLE = new Function("TO_DOUBLE", 1);
    public final static Function TO_STRING = new Function("TO_STRING", 1, 2);
    public final static Function TO_BOOLEAN = new Function("TO_BOOLEAN", 1);

    public final static Function ROUND = new Function("ROUND", 1, 2);
    public final static Function CEIL = new Function("CEIL", 1, 2);


    public final static Function TO_LOWERCASE = new Function("TO_LOWERCASE", 1);
    public final static Function TO_UPPERCASE = new Function("TO_UPPERCASE", 1);

    public final static Function ABS = new Function("ABS", 1);


    public final static Function REGEX_MATCH = new Function("REGEX_MATCH", 2);
    public final static Function REGEX_EXTRACT = new Function("REGEX_EXTRACT", 2, 3);
    public final static Function REGEX_REPLACE = new Function("REGEX_REPLACE", 3);
    public final static Function EXTRACT = new Function("EXTRACT", 2);

    public final static Function STRING_FORMAT = new Function("STRING_FORMAT", 2, Integer.MAX_VALUE);
    public final static Function REPLACE_ALL = new Function("REPLACE_ALL", 3, 4);

    public final static Function CONTAINS = new Function("CONTAINS", 2, Integer.MAX_VALUE);

    public final static Function RANDOM_NUM = new Function("RANDOM_NUM", 0, 2);
    public final static Function RANDOM_SENTENCE = new Function("RANDOM_SENTENCE", 0, 1);
    public final static Function RANDOM_WORD = new Function("RANDOM_WORD", 0, 1);
    public final static Function RANDOM_ALPHABETIC = new Function("RANDOM_ALPHABETIC", 0, 1);

    public final static Function RANDOM_ALPHANUMERIC = new Function("RANDOM_ALPHANUMERIC", 0, 1);
    public final static Function RANDOM_NUMERIC = new Function("RANDOM_NUMERIC", 0, 1);
    public final static Function RANDOM_STRING = new Function("RANDOM_STRING", 0, 1);
    public final static Function RANDOM_ASCII = new Function("RANDOM_ASCII", 0, 1);
    public final static Function RANDOM_GRAPH = new Function("RANDOM_GRAPH", 0, 1);


    public final static Function RANDOM_CHAR = new Function("RANDOM_CHAR", 0, 1);

    public final static Function SPLITTER = new Function("SPLITTER", 1, 4);
    public final static Function SUBSTRING = new Function("SUBSTRING", 2, 3);
    public final static Function INDEX_OF = new Function("INDEX_OF", 2);
    // public final static Function DATE = new Function("DATE", 2);

    protected static final Parameters PARAMETERS;

    static {
        // Create the evaluator's parameters
        PARAMETERS = new Parameters();
        // Add the supported operators
        PARAMETERS.add(AND);
        PARAMETERS.add(OR);
        PARAMETERS.add(NEGATE);

        PARAMETERS.add(NOT_EQUAL);
        PARAMETERS.add(EQUAL);
        PARAMETERS.add(GREATER_THEN);
        PARAMETERS.add(GREATER_THEN_OR_EQUAL);
        PARAMETERS.add(LOWER_THEN_OR_EQUAL);
        PARAMETERS.add(LOWER_THEN);

        PARAMETERS.add(PLUS);
        PARAMETERS.add(MINUS);
        PARAMETERS.add(MULTIPLY);
        PARAMETERS.add(DIVIDE);

        PARAMETERS.add(MODULO);
        PARAMETERS.add(POW);


        PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
        PARAMETERS.addFunctionBracket(BracketPair.PARENTHESES);

        PARAMETERS.add(NOW);
        PARAMETERS.add(DATE_NOW);
        PARAMETERS.add(IS_NULL);
        PARAMETERS.add(SET_NULL);
        PARAMETERS.add(SIZE_OF);
        PARAMETERS.add(TYPE_OF);
        PARAMETERS.add(IS_OF_TYPE);

        PARAMETERS.add(IF);

        PARAMETERS.add(STR_TO_DATE_TIMESTAMP);

        PARAMETERS.add(MINUTE);
        PARAMETERS.add(HOUR);
        PARAMETERS.add(DAY);
        PARAMETERS.add(WEEK);
        PARAMETERS.add(WEEK_OF_YEAR);

        PARAMETERS.add(MONTH);
        PARAMETERS.add(YEAR);

        PARAMETERS.add(DATE_DIFF);

        PARAMETERS.add(DATE_MINUS);
        PARAMETERS.add(DATE_PLUS);

        PARAMETERS.add(DATE_START);
        PARAMETERS.add(DATE_END);


        PARAMETERS.add(TO_DATE);
        PARAMETERS.add(TO_INT);
        PARAMETERS.add(TO_LONG);
        PARAMETERS.add(TO_DOUBLE);
        PARAMETERS.add(TO_STRING);
        PARAMETERS.add(TO_BOOLEAN);


        PARAMETERS.add(TRUE);
        PARAMETERS.add(FALSE);

        PARAMETERS.add(TO_LOWERCASE);
        PARAMETERS.add(TO_UPPERCASE);

        PARAMETERS.add(ABS);
        PARAMETERS.add(ROUND);
        PARAMETERS.add(CEIL);

        PARAMETERS.add(STRING_FORMAT);
        PARAMETERS.add(REPLACE_ALL);
        PARAMETERS.add(SPLITTER);
        PARAMETERS.add(SUBSTRING);

        PARAMETERS.add(INDEX_OF);

        PARAMETERS.add(REGEX_MATCH);
        PARAMETERS.add(REGEX_EXTRACT);
        PARAMETERS.add(REGEX_REPLACE);
        PARAMETERS.add(EXTRACT);


        PARAMETERS.add(CONTAINS);
        PARAMETERS.add(RANDOM_NUM);
        PARAMETERS.add(RANDOM_SENTENCE);
        PARAMETERS.add(RANDOM_WORD);
        PARAMETERS.add(RANDOM_CHAR);

        PARAMETERS.add(RANDOM_ALPHABETIC);

        PARAMETERS.add(RANDOM_ALPHANUMERIC);
        PARAMETERS.add(RANDOM_NUMERIC);
        PARAMETERS.add(RANDOM_STRING);
        PARAMETERS.add(RANDOM_ASCII);
        PARAMETERS.add(RANDOM_GRAPH);

    }

    private FunctionEvaluateUtils functionEvaluateUtils;
    private OperatorEvaluateUtils operatorEvaluateUtils;
    private static SimpleObjectEvaluator INSTANCE;

    public static SimpleObjectEvaluator getInstance(ExecutionContext executionContext, AbstractResolverHelper mapListResolver) {
        synchronized (SimpleObjectEvaluator.class) {
            if (null == INSTANCE) {
                synchronized (SimpleObjectEvaluator.class) {
                    if (null == INSTANCE)
                        INSTANCE = new SimpleObjectEvaluator(executionContext, mapListResolver);
                }
            }
        }
        return INSTANCE;
    }

    private SimpleObjectEvaluator(ExecutionContext executionContext) {
        super(executionContext, PARAMETERS, new SimpleResolverHelper());
        functionEvaluateUtils = new FunctionEvaluateUtils(mapListResolver, executionContext);
        operatorEvaluateUtils = new OperatorEvaluateUtils(mapListResolver, executionContext);
    }

    protected SimpleObjectEvaluator(ExecutionContext executionContext, AbstractResolverHelper mapListResolver) {
        super(executionContext, PARAMETERS, mapListResolver);
        functionEvaluateUtils = new FunctionEvaluateUtils(mapListResolver, executionContext);
        operatorEvaluateUtils = new OperatorEvaluateUtils(mapListResolver, executionContext);
    }

    @Override
    protected ContextWrapper toValue(String literal, Object evaluationContext) {
        if (evaluationContext instanceof AbstractVariableSet) {
            AbstractVariableSet abs = (AbstractVariableSet) evaluationContext;
            Object o = abs.get(literal);
            if (null == o) {
                return executionContext.create(new NullObject());
            } else {
                return executionContext.create(o);
            }
        }
        return executionContext.create(literal);
    }


    @Override
    protected ContextWrapper evaluate(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                      Object evaluationContext) {

        if (function == INDEX_OF) {
            return functionEvaluateUtils.getIndexOf(function, operands, argumentList, evaluationContext); //getIndexOf(function, operands, argumentList, evaluationContext);
        } else if (function == IF) {
            //return ifTrueFalse(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getIf(function, operands, argumentList, evaluationContext);
        } else if (function == DATE_DIFF) {
            //return timeDiff(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getDateDiff(function, operands, argumentList, evaluationContext);
        } else if (function == DATE_MINUS) {
            //return getDateMinus(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getDateMinus(function, operands, argumentList, evaluationContext);
        } else if (function == DATE_PLUS) {
            //return getDatePlus(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getDatePlus(function, operands, argumentList, evaluationContext);
        } else if (function == DATE_START) {
            //return getDateStart(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getDateStart(function, operands, argumentList, evaluationContext);
        } else if (function == DATE_END) {
            //return getDateEnd(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getDateEnd(function, operands, argumentList, evaluationContext);
        } else if (function == ABS) {
            //return abs(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getAbs(function, operands, argumentList, evaluationContext);
        } else if (function == ROUND) {
            //return getRound(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getRound(function, operands, argumentList, evaluationContext);
        } else if (function == CEIL) {
            //return getCeil(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getCeil(function, operands, argumentList, evaluationContext);
        } else if (function == STRING_FORMAT) {
            //return stringFormat(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getStringFormat(function, operands, argumentList, evaluationContext);
        } else if (function == TO_DATE) {
            //return toDate(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getToDate(function, operands, argumentList, evaluationContext);
        } else if (function == TO_INT) {
            //return toInteger(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getToInt(function, operands, argumentList, evaluationContext);
        } else if (function == TO_DOUBLE) {
            //return toDouble(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getToDouble(function, operands, argumentList, evaluationContext);
        } else if (function == TO_LONG) {
            //return toLong(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getToLong(function, operands, argumentList, evaluationContext);
        } else if (function == TO_STRING) {
            //return toStringValue(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getToString(function, operands, argumentList, evaluationContext);
        } else if (function == TO_BOOLEAN) {
            //return toBooleanValue(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getToBoolean(function, operands, argumentList, evaluationContext);
        } else if (function == TRUE) {
            return functionEvaluateUtils.getTrue(function, operands, argumentList, evaluationContext);
        } else if (function == FALSE) {
            return functionEvaluateUtils.getFalse(function, operands, argumentList, evaluationContext);
        } else if (function == TO_LOWERCASE) {
            return functionEvaluateUtils.getToLowercase(function, operands, argumentList, (AbstractVariableSet) evaluationContext);
        } else if (function == TO_UPPERCASE) {
            return functionEvaluateUtils.getToUpperCase(function, operands, argumentList, (AbstractVariableSet) evaluationContext);
        } else if (function == STR_TO_DATE_TIMESTAMP) {
            return functionEvaluateUtils.getStrToDateTimestamp(function, operands, argumentList, evaluationContext);
        } else if (function == MINUTE) {
            return functionEvaluateUtils.getMinute(function, operands, argumentList, evaluationContext);
        } else if (function == HOUR) {
            return functionEvaluateUtils.getHour(function, operands, argumentList, evaluationContext);
        } else if (function == DAY) {
            return functionEvaluateUtils.getDay(function, operands, argumentList, evaluationContext);
        } else if (function == WEEK) {
            return functionEvaluateUtils.getWeek(function, operands, argumentList, evaluationContext);
        } else if (function == WEEK_OF_YEAR) {
            return functionEvaluateUtils.getWeekOfYear(function, operands, argumentList, evaluationContext);
        } else if (function == MONTH) {
            return functionEvaluateUtils.getMonth(function, operands, argumentList, evaluationContext);
        } else if (function == YEAR) {
            return functionEvaluateUtils.getYear(function, operands, argumentList, evaluationContext);
        } else if (function == NOW) {
            return functionEvaluateUtils.getNow(function, operands, argumentList, evaluationContext);
        } else if (function == DATE_NOW) {
            return functionEvaluateUtils.getDateNow(function, operands, argumentList, evaluationContext);
        } else if (function == SET_NULL) {
            return functionEvaluateUtils.getSetNull(function, operands, argumentList, evaluationContext);
        } else if (function == IS_NULL) {
            return functionEvaluateUtils.getIsNull(function, operands, argumentList, evaluationContext);
        } else if (function == IS_OF_TYPE) {
            return functionEvaluateUtils.getIsOfType(function, operands, argumentList, evaluationContext);
        } else if (function == TYPE_OF) {
            return functionEvaluateUtils.getTypeOf(function, operands, argumentList, evaluationContext);
        } else if (function == SIZE_OF) {
            return functionEvaluateUtils.getSizeOf(function, operands, argumentList, evaluationContext);
        } else if (function == REGEX_MATCH) {
            //return regexMatch(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getRegexMatch(function, operands, argumentList, evaluationContext);
        } else if (function == REGEX_EXTRACT) {
            //return regexExtract(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getRegexExtract(function, operands, argumentList, evaluationContext);
        } else if (function == REGEX_REPLACE) {
            //return regexReplace(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRegexReplace(function, operands, argumentList, evaluationContext);
        } else if (function == EXTRACT) {
            //return extract(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getExtract(function, operands, argumentList, evaluationContext);
        } else if (function == REPLACE_ALL) {
            //return replaceAll(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getReplaceAll(function, operands, argumentList, evaluationContext);
        } else if (function == SPLITTER) {
            //return splitter(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getSplitter(function, operands, argumentList, evaluationContext);
        } else if (function == CONTAINS) {
            //return evaluateContainsFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getContains(function, operands, argumentList, evaluationContext);
        } else if (function == RANDOM_NUM) {
            //return evaluateRandomNumFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRandomNum(function, operands, argumentList, evaluationContext);
        } else if (function == RANDOM_SENTENCE) {
            //return evaluateRandomSentenceFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRandomSentence(function, operands, argumentList, evaluationContext);
        } else if (function == RANDOM_WORD) {
            //return evaluateRandomWordFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRandomWord(function, operands, argumentList, evaluationContext);
        } else if (function == RANDOM_ALPHABETIC) {
            //return evaluateRandomAlphabeticFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRandomAlphabetic(function, operands, argumentList, evaluationContext);
        } else if (function == RANDOM_ALPHANUMERIC) {
            //return evaluateRandomAlphanumericFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRandomAlphanumeric(function, operands, argumentList, evaluationContext);
        } else if (function == RANDOM_STRING) {
            //return evaluateRandomFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRandomString(function, operands, argumentList, evaluationContext);
        } else if (function == RANDOM_NUMERIC) {
            //return evaluateRandomNumericFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRandomNumeric(function, operands, argumentList, evaluationContext);
        } else if (function == RANDOM_ASCII) {
            //return evaluateRandomAsciiFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRandomAscii(function, operands, argumentList, evaluationContext);
        } else if (function == RANDOM_GRAPH) {
            //return evaluateRandomGraphFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRandomGraph(function, operands, argumentList, evaluationContext);
        } else if (function == RANDOM_CHAR) {
            // return evaluateRandomCharFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
            return functionEvaluateUtils.getRandomChar(function, operands, argumentList, evaluationContext);
        } else if (function == SUBSTRING) {
            //return substring(function, operands, argumentList, evaluationContext);
            return functionEvaluateUtils.getSubstring(function, operands, argumentList, evaluationContext);
        } else {
            return super.evaluate(function, operands, argumentList, evaluationContext);
        }
    }

    protected ContextWrapper nextFunctionEvaluate(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                                  Object evaluationContext) {
        return super.evaluate(function, operands, argumentList, evaluationContext);
    }


    @Override
    protected ContextWrapper evaluate(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {

        if (operator == POW) {
            return operatorEvaluateUtils.getPow(operator, operands, evaluationContext);
        } else if (operator == MODULO) {
            return operatorEvaluateUtils.getModulo(operator, operands, evaluationContext);
        } else if (operator == DIVIDE) {
            return operatorEvaluateUtils.getDivide(operator, operands, evaluationContext);
        } else if (operator == MULTIPLY) {
            return operatorEvaluateUtils.getMultiply(operator, operands, evaluationContext);
        } else if (operator == NEGATE) {
            return operatorEvaluateUtils.getNegate(operator, operands, evaluationContext);
        } else if (operator == NOT_EQUAL) {
            return operatorEvaluateUtils.getNotEqual(operator, operands, evaluationContext);
        } else if (operator == EQUAL) {
            return operatorEvaluateUtils.getEqual(operator, operands, evaluationContext);
        } else if (operator == LOWER_THEN_OR_EQUAL) {
            return operatorEvaluateUtils.getLowerThenOrEqual(operator, operands, evaluationContext);
        } else if (operator == GREATER_THEN_OR_EQUAL) {
            return operatorEvaluateUtils.getGreaterThenOrEqual(operator, operands, evaluationContext);
        } else if (operator == GREATER_THEN) {
            return operatorEvaluateUtils.getGreaterThen(operator, operands, evaluationContext);

        } else if (operator == LOWER_THEN) {
            return operatorEvaluateUtils.getLowerThen(operator, operands, evaluationContext);
        } else if (operator == AND) {
            return operatorEvaluateUtils.getAnd(operator, operands, evaluationContext);
        } else if (operator == OR) {
            return operatorEvaluateUtils.getOr(operator, operands, evaluationContext);
        } else if (operator == PLUS) {
            return operatorEvaluateUtils.getPlus(operator, operands, evaluationContext);
        } else if (operator == MINUS) {
            return operatorEvaluateUtils.getMinus(operator, operands, evaluationContext);
        } else {
            return nextOperatorEvaluate(operator, operands, evaluationContext);
        }
        //return false;
    }

    protected ContextWrapper nextOperatorEvaluate(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        return super.evaluate(operator, operands, evaluationContext);
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


}
