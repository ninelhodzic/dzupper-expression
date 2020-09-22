package org.datazup.expression;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.expression.exceptions.ExpressionValidationException;
import org.datazup.expression.exceptions.NotSupportedExpressionException;
import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.pathextractor.AbstractVariableSet;
import org.datazup.pathextractor.PathExtractor;
import org.datazup.pathextractor.SimpleResolverHelper;
import org.datazup.utils.DateTimeUtils;
import org.datazup.utils.Tuple;
import org.datazup.utils.TypeUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public final static Function RANDOM_ALPHANUMERIC= new Function("RANDOM_ALPHANUMERIC", 0, 1);
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
    }

    protected SimpleObjectEvaluator(ExecutionContext executionContext, AbstractResolverHelper mapListResolver) {
        super(executionContext, PARAMETERS, mapListResolver);
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
            return getIndexOf(function, operands, argumentList, evaluationContext);
        } else if (function == IF) {
            return ifTrueFalse(function, operands, argumentList, evaluationContext);
        } else if (function == DATE_DIFF) {
            return timeDiff(function, operands, argumentList, evaluationContext);
        } else if (function == DATE_MINUS) {
            return getDateMinus(function, operands, argumentList, evaluationContext);
        } else if (function == DATE_PLUS) {
            return getDatePlus(function, operands, argumentList, evaluationContext);
        } else if (function == ABS) {
            return abs(function, operands, argumentList, evaluationContext);
        } else if (function == ROUND) {
            return getRound(function, operands, argumentList, evaluationContext);
        } else if (function == CEIL) {
            return getCeil(function, operands, argumentList, evaluationContext);
        } else if (function == STRING_FORMAT) {
            return stringFormat(function, operands, argumentList, evaluationContext);
        } else if (function == TO_DATE) {
            return toDate(function, operands, argumentList, evaluationContext);
        } else if (function == TO_INT) {
            return toInteger(function, operands, argumentList, evaluationContext);
        } else if (function == TO_DOUBLE) {
            return toDouble(function, operands, argumentList, evaluationContext);
        } else if (function == TO_LONG) {
            return toLong(function, operands, argumentList, evaluationContext);
        } else if (function == TO_STRING) {
            return toStringValue(function, operands, argumentList, evaluationContext);
        } else if (function == TO_BOOLEAN) {
            return toBooleanValue(function, operands, argumentList, evaluationContext);
        } else if (function == TRUE) {
            return executionContext.create(true);
        } else if (function == FALSE) {
            return executionContext.create(false);
        } else if (function == TO_LOWERCASE) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();
            String val = TypeUtils.resolveString(op1.get());
            if (StringUtils.isEmpty(val))
                return op1;
            else
                return executionContext.create(val.toLowerCase());

        } else if (function == TO_UPPERCASE) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();

            String val = TypeUtils.resolveString(op1);
            if (StringUtils.isEmpty(val))
                return op1;
            else
                return executionContext.create(val.toUpperCase());

        } else if (function == STR_TO_DATE_TIMESTAMP) {
            return strToDateTimeStamp(function, operands, argumentList, evaluationContext);
        } else if (function == MINUTE) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1.get());
            return executionContext.create(DateTimeUtils.getMinute(dt));
        } else if (function == HOUR) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1.get());
            return executionContext.create(DateTimeUtils.getHour(dt));
        } else if (function == DAY) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1.get());
            return executionContext.create(DateTimeUtils.getDayOfMonth(dt));
        } else if (function == WEEK) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1.get());
            LocalDateTime localDate = LocalDateTime.ofInstant(dt, ZoneId.systemDefault());
            Integer res = localDate.get(WeekFields.of(Locale.ENGLISH).weekOfMonth()); //DateTimeUtils.getDayOfMonth(dt) % 7;
            return executionContext.create(res);
        } else if (function == WEEK_OF_YEAR) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1.get());
            LocalDateTime localDate = LocalDateTime.ofInstant(dt, ZoneId.systemDefault());
            Integer res = localDate.get(WeekFields.of(Locale.ENGLISH).weekOfYear()); //DateTimeUtils.getDayOfMonth(dt) % 7;

            return executionContext.create(res);
        } else if (function == MONTH) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1.get());
            return executionContext.create(DateTimeUtils.getMonth(dt));
        } else if (function == YEAR) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1.get());
            return executionContext.create(DateTimeUtils.getYear(dt));
        } else if (function == NOW) {
            return executionContext.create(System.currentTimeMillis()); //Instant.now();
        } else if (function == SET_NULL) {
            Object op1 = operands.next();
            argumentList.pop();
            return executionContext.create(null); // getNullObject();
        } else if (function == IS_NULL) {
            ContextWrapper op1 = operands.next();
            Token token = argumentList.pop();
            Object resolved = op1.get();
            if (null == resolved || resolved instanceof NullObject || resolved.toString() == token.getContent().toString()) {
                return executionContext.create(true);
            } else if (resolved instanceof String) {
                return executionContext.create(((String) resolved).isEmpty());
            }
            return executionContext.create(resolved == null);

        } else if (function == IS_OF_TYPE) {
            return getIsOfType(function, operands, argumentList, evaluationContext);
        } else if (function == TYPE_OF) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();

            Object resolved = op1.get();

            if (null != resolved && !(resolved instanceof NullObject)) {
                String name = resolved.getClass().getSimpleName();
                switch (name) {
                    case "TreeList":
                    case "JsonArray":
                    case "ArrayList":
                        name = "List";
                        break;
                    case "TreeSet":
                    case "LinkedHashSet":
                    case "HashSet":
                        name = "Set";
                        break;
                    case "JsonObject":
                    case "HashMap":
                    case "TreeMap":
                    case "LinkedHashMap":
                        name = "Map";
                        break;
                }
                return executionContext.create(name);
            } else {
                return executionContext.create(null);
            }
        } else if (function == SIZE_OF) {
            ContextWrapper op1 = operands.next();
            argumentList.pop();
            Object resolved = op1.get();
            if (null != resolved && !(resolved instanceof NullObject)) {

                if (resolved instanceof String) {
                    return executionContext.create(((String) resolved).length());
                } else {
                    Map m = mapListResolver.resolveToMap(resolved);
                    if (null == m) {
                        Collection c = mapListResolver.resolveToCollection(resolved);
                        if (null == c) {
                            List l = mapListResolver.resolveToList(resolved);
                            if (null != l) {
                                return executionContext.create(l.size());
                            }
                        } else {
                            return executionContext.create(c.size());
                        }
                    } else {
                        return executionContext.create(m.size());
                    }
                }

                throw new NotSupportedExpressionException(
                        "SizeOf function not supported for instance of \"" + resolved.getClass() + "\"");


            }
            return executionContext.create(0);
        } else if (function == REGEX_MATCH) {
            return regexMatch(function, operands, argumentList, evaluationContext);
        } else if (function == REGEX_EXTRACT) {
            return regexExtract(function, operands, argumentList, evaluationContext);
        } else if (function == REGEX_REPLACE) {
            return regexReplace(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == EXTRACT) {
            return extract(function, operands, argumentList, evaluationContext);
        } else if (function == REPLACE_ALL) {
            return replaceAll(function, operands, argumentList, evaluationContext);
        } else if (function == SPLITTER) {
            return splitter(function, operands, argumentList, evaluationContext);
        } else if (function == CONTAINS) {
            return evaluateContainsFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == RANDOM_NUM) {
            return evaluateRandomNumFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == RANDOM_SENTENCE) {
            return evaluateRandomSentenceFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == RANDOM_WORD) {
            return evaluateRandomWordFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == RANDOM_ALPHABETIC) {
            return evaluateRandomAlphabeticFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        }else if (function == RANDOM_ALPHANUMERIC) {
            return evaluateRandomAlphanumericFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        }else if (function == RANDOM_STRING) {
            return evaluateRandomFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        }else if (function == RANDOM_NUMERIC) {
            return evaluateRandomNumericFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        }else if (function == RANDOM_ASCII) {
            return evaluateRandomAsciiFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        }else if (function == RANDOM_GRAPH) {
            return evaluateRandomGraphFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        }
        else if (function == RANDOM_CHAR) {
            return evaluateRandomCharFunction(function, operands, argumentList, (PathExtractor) evaluationContext);
        } else if (function == SUBSTRING) {
            return substring(function, operands, argumentList, evaluationContext);
        }
        /*else {
            return nextFunctionEvaluate(function, operands, argumentList, evaluationContext);
        }*/
        else {
            return super.evaluate(function, operands, argumentList, evaluationContext);
        }
    }

    private Object evaluateRandomFunction(String type, Integer size) {
        Object res = null;
        switch (type) {
            case "randomAlphabetic":
                res = RandomStringUtils.randomAlphabetic(size);
                break;
            case "randomAlphanumeric":
                res = RandomStringUtils.randomAlphanumeric(size);
                break;
            case "randomAscii":
                res = RandomStringUtils.randomAscii(size);
                break;
            case "randomNumeric":
                res = RandomStringUtils.randomNumeric(size);
                break;
            case "randomGraph":
                res = RandomStringUtils.randomGraph(size);
                break;
            case "random":
                res = RandomStringUtils.random(size);
                break;
        }

        return res;
    }

    private ContextWrapper evaluateRandomFunction(String type, Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        if (operands.hasNext()) {
            ContextWrapper countC = operands.next();
            argumentList.pop();
            Number num = TypeUtils.resolveNumber(countC.get());
            if (null != num) {
                return wrap(evaluateRandomFunction(type, num.intValue()));
            }
        } else {
            return wrap(evaluateRandomFunction(type, 10));
        }
        return wrap(null);
    }

    private ContextWrapper evaluateRandomAlphabeticFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        return evaluateRandomFunction("randomAlphabetic", function, operands, argumentList, evaluationContext);
    }
    private ContextWrapper evaluateRandomAlphanumericFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        return evaluateRandomFunction("randomAlphanumeric", function, operands, argumentList, evaluationContext);
    }

    private ContextWrapper evaluateRandomNumericFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        return evaluateRandomFunction("randomNumeric", function, operands, argumentList, evaluationContext);
    }

    private ContextWrapper evaluateRandomFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        return evaluateRandomFunction("random", function, operands, argumentList, evaluationContext);
    }

    private ContextWrapper evaluateRandomGraphFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        return evaluateRandomFunction("randomGraph", function, operands, argumentList, evaluationContext);
    }

    private ContextWrapper evaluateRandomAsciiFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        return evaluateRandomFunction("randomAscii", function, operands, argumentList, evaluationContext);
    }

    private Boolean isOfTypeRecursively(Class objClass, String type) {
        if (null == objClass) {
            return false;
        }
        Boolean isOfType = false;

        Class[] interfaces = objClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            if (anInterface.getSimpleName().equalsIgnoreCase(type)) {
                isOfType = true;
                break;
            } else {
                isOfType = isOfTypeRecursively(anInterface, type);
            }
        }
        if (null != isOfType && isOfType)
            return true;

        if (objClass.getSimpleName().equalsIgnoreCase(type)) {
            isOfType = true;
        } else {
            isOfType = isOfTypeRecursively(objClass.getSuperclass(), type);
        }
        return isOfType;
    }


    private ContextWrapper getIsOfType(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper valueObj = operands.next();
        argumentList.pop();
        Object valueObjResolved = valueObj.get();


        ContextWrapper typeStrObj = operands.next();
        argumentList.pop();

        if (null == valueObjResolved) {
            return executionContext.create(false);
        }

        Object typeStrObjResolved = typeStrObj.get();

        String typeStr = TypeUtils.resolveString(typeStrObjResolved);
        if (StringUtils.isEmpty(typeStr)) {
            return executionContext.create(false);
        }

        Boolean isOfType = isOfTypeRecursively(valueObjResolved.getClass(), typeStr);

        return executionContext.create(isOfType);
    }

    private ContextWrapper toBooleanValue(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper valueObjC = operands.next();
        argumentList.pop();
        Object valueObj = valueObjC.get();
        if (null == valueObj) {
            return executionContext.create(false);
        }
        return executionContext.create(TypeUtils.resolveBoolean(valueObj));
    }

    private ContextWrapper getIndexOf(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper valueObj = operands.next();
        argumentList.pop();

        ContextWrapper indexObj = operands.next();
        argumentList.pop();

        Object valueObjResolved = valueObj.get();
        Object indexObjResolved = indexObj.get();

        List list = mapListResolver.resolveToList(valueObjResolved);
        if (null != list) {
            return executionContext.create(list.indexOf(indexObjResolved));
        } else {
            if (valueObjResolved instanceof String) {
                String strValue = (String) valueObjResolved;
                return executionContext.create(strValue.indexOf(indexObjResolved.toString()));
            } else {
                return executionContext.create(null);
            }
        }
    }

    private ContextWrapper getRound(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper firstObj = operands.next();
        argumentList.pop();

        Object firstObjResolved = firstObj.get();

        Number number = TypeUtils.resolveNumber(firstObjResolved);
        if (null == number)
            return executionContext.create(firstObjResolved);

        Number decimalPlaces = null;
        if (operands.hasNext()) {
            ContextWrapper nextObj = operands.next();
            Object nextObjResolbed = nextObj.get();
            decimalPlaces = TypeUtils.resolveNumber(nextObjResolbed);
            argumentList.pop();
        }

        String format = "#";

        if (null != decimalPlaces) {
            format = format + "." + StringUtils.repeat("#", decimalPlaces.intValue());

            DecimalFormat df = new DecimalFormat(format.trim());
            df.setRoundingMode(RoundingMode.FLOOR);
            Number newNum = TypeUtils.resolveNumber(df.format(number));
            return executionContext.create(newNum.floatValue());
        } else {
            return executionContext.create(Math.round(number.doubleValue()));
        }
    }

    private ContextWrapper getCeil(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper firstObj = operands.next();
        argumentList.pop();

        Object firstObjResolved = firstObj.get();

        Number number = TypeUtils.resolveNumber(firstObjResolved);
        if (null == number)
            return executionContext.create(firstObjResolved);

        Number decimalPlaces = null;
        if (operands.hasNext()) {
            ContextWrapper c = operands.next();
            Object resolved = c.get();
            decimalPlaces = TypeUtils.resolveNumber(resolved);
            argumentList.pop();
        }

        String format = "#";

        if (null != decimalPlaces) {
            format = format + "." + StringUtils.repeat("#", decimalPlaces.intValue());

            DecimalFormat df = new DecimalFormat(format.trim());
            df.setRoundingMode(RoundingMode.CEILING);
            Number newNum = TypeUtils.resolveNumber(df.format(number));
            return executionContext.create(newNum.floatValue());
        } else {
            return executionContext.create(Math.ceil(number.doubleValue()));
        }
    }

    private ContextWrapper abs(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper firstObjContext = operands.next();
        argumentList.pop();

        if (operands.hasNext()) {
            throw new ExpressionValidationException("We expect three arguments for ABS expression");
        }

        Object firstObj = firstObjContext.get();


        if (firstObj instanceof String) {
            String v = (String) firstObj;
            Number n = NumberUtils.createNumber(v);
            firstObj = n;
        }

        if (firstObj instanceof Integer) {
            firstObj = Math.abs((Integer) firstObj);
        } else if (firstObj instanceof Long) {
            firstObj = Math.abs((Long) firstObj);
        } else if (firstObj instanceof Float) {
            firstObj = Math.abs((Float) firstObj);
        } else if (firstObj instanceof Double) {
            firstObj = Math.abs((Double) firstObj);
        } else if (firstObj instanceof Number) {
            Number n = (Number) firstObj;
            Double d = n.doubleValue();
            if (d < 0) {
                d = -d;
            }
            firstObj = d;
        } else {
            firstObj = null;
        }
        return executionContext.create(firstObj);
    }

    private ContextWrapper manageDateAddSubtract(String operation, Iterator<ContextWrapper> operands, Deque<Token> argumentList) {
        ContextWrapper firstDTObjContext = operands.next();
        argumentList.pop();
        ContextWrapper durationObjContext = operands.next();
        argumentList.pop();
        ContextWrapper timeUnitObjContext = operands.next();
        argumentList.pop();

        Object firstDTObj = firstDTObjContext.get();


        Instant firstDt = DateTimeUtils.resolve(firstDTObj);
        if (null == firstDt) {
            return executionContext.create(firstDTObj);
        }

        Object durationObj = durationObjContext.get();
        Object timeUnitObj = timeUnitObjContext.get();

        Long duration = TypeUtils.resolveLong(durationObj);
        if (null == duration) {
            duration = 0l;
        }

        String timeUnitString = TypeUtils.resolveString(timeUnitObj);
        if (null == timeUnitString || timeUnitString.isEmpty()) {
            timeUnitString = "Minutes";
        }
        ChronoUnit timeUnit = ChronoUnit.valueOf(timeUnitString.toUpperCase());

        Instant result = firstDt;
        switch (operation) {
            case "PLUS":
                result = firstDt.plus(duration, timeUnit);
                break;
            case "MINUS":
                result = firstDt.minus(duration, timeUnit);
                break;
        }

        return executionContext.create(result);
    }

    private ContextWrapper getDatePlus(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return manageDateAddSubtract("PLUS", operands, argumentList);
    }

    private ContextWrapper getDateMinus(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return manageDateAddSubtract("MINUS", operands, argumentList);
    }

    private ContextWrapper timeDiff(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper firstDTObjC = operands.next();
        argumentList.pop();
        ContextWrapper secondDTObjC = operands.next();
        argumentList.pop();
        ContextWrapper timeUnitObjC = operands.next();
        argumentList.pop();

        if (operands.hasNext()) {
            throw new ExpressionValidationException("We expect three arguments for TimeDiff expression");
        }

        Object firstDTObj = firstDTObjC.get();
        Object secondDTObj = secondDTObjC.get();
        Object timeUnitObj = timeUnitObjC.get();

        Instant firstDt = DateTimeUtils.resolve(firstDTObj);
        Instant secondDt = DateTimeUtils.resolve(secondDTObj);
        String timeUnitString = null;
        if (timeUnitObj instanceof String) {
            timeUnitString = (String) timeUnitObj;
        } else {
            timeUnitString = timeUnitObj.toString();
        }
        if (StringUtils.isEmpty(timeUnitString)) {
            throw new ExpressionValidationException("We cannot recognize TimeUnit value: " + timeUnitString);
        }
        ChronoUnit timeUnit = ChronoUnit.valueOf(timeUnitString.toUpperCase());

        long timeDiff = timeUnit.between(firstDt, secondDt);


        return executionContext.create(timeDiff);
    }

    private ContextWrapper splitter(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        if (operands.hasNext()) {
            ContextWrapper stringToSplitObjC = operands.next();
            argumentList.pop();

            Object stringToSplitObj = stringToSplitObjC.get();

            if (null == stringToSplitObj || !(stringToSplitObj instanceof String)) {
                while (operands.hasNext()) {
                    operands.next();
                    if (argumentList.size() > 0)
                        argumentList.pop(); // just to clean in case there is more arguments
                }

                return wrap(stringToSplitObj);
            }

            String stringToSplit = (String) stringToSplitObj;
            if (operands.hasNext()) {
                ContextWrapper splitterObjC = operands.next();
                argumentList.pop();
                Object splitterObj = splitterObjC.get();
                String splitter = (String) splitterObj;

                String language = "en";
                if (operands.hasNext()) {
                    ContextWrapper splitterLangC = operands.next();
                    argumentList.pop();
                    Object splitterLang = splitterLangC.get();
                    language = (String) splitterLang;
                }

                Boolean removeEmpty = false;
                if (operands.hasNext()) {
                    ContextWrapper removeEmptyObjC = operands.next();
                    argumentList.pop();
                    Object removeEmptyObj = removeEmptyObjC.get();
                    removeEmpty = TypeUtils.resolveBoolean(removeEmptyObj);
                }

                switch (language) {
                    case "en":
                        String[] splitted = stringToSplit.split(splitter);
                        List<String> l = new ArrayList<>();
                        for (int i = 0; i < splitted.length; i++) {
                            if (removeEmpty) {
                                if (!StringUtils.isEmpty(splitted[i])) {
                                    l.add(splitted[i]);
                                }
                            } else {
                                l.add(splitted[i]);
                            }
                        }

                        return wrap(l);
                    default:
                        throw new ExpressionValidationException("There is no splitter for language: " + language + " implemented");
                }

            } else {
                return wrap(stringToSplit);
            }

        }
        return wrap(null);
    }

    private ContextWrapper substring(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        if (operands.hasNext()) {
            String result = "";
            ContextWrapper inputC = operands.next();
            Object input = inputC.get();

            Token token1 = argumentList.pop();
            if (input == null || !(input instanceof String)) {
                while (operands.hasNext()) {
                    operands.next();
                    argumentList.pop();
                }
                return wrap(input);
            }
            String inputText = input.toString();
            if (operands.hasNext()) {
                int startIndex = (int) Double.parseDouble(operands.next().get().toString());
                token1 = argumentList.pop();
                if (operands.hasNext()) {
                    int endIndex = (int) Double.parseDouble(operands.next().get().toString());
                    token1 = argumentList.pop();
                    result = inputText.substring(startIndex, endIndex);
                } else {
                    result = inputText.substring(startIndex);
                }
            }
            return wrap(result);
        } else {
            return wrap(null);
        }
    }

    private ContextWrapper evaluateRandomCharFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {

        if (operands.hasNext()) {
            ContextWrapper containerOrStringC = operands.next();
            Token token1 = argumentList.pop();
            Object containerOrString = containerOrStringC.get();

            if (null == containerOrString || !(containerOrString instanceof String)) {
                return wrap(RandomStringUtils.random(1));
            } else {
                String s = (String) containerOrString;
                if (s.contains(" ")) {
                    s = s.replaceAll(" ", "").trim();
                }
                int random = RandomUtils.nextInt(0, s.length());
                if (s.length() > random) {
                    return wrap(String.valueOf(s.charAt(random)));
                } else {
                    return wrap(containerOrString);
                }
            }
        } else {
            return wrap(RandomStringUtils.random(1));
        }
    }

    private ContextWrapper evaluateRandomWordFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        if (operands.hasNext()) {
            ContextWrapper containerOrStringC = operands.next();
            Token token1 = argumentList.pop();
            Object containerOrString = containerOrStringC.get();

            if (null == containerOrString || !(containerOrString instanceof String)) {
                Number num = TypeUtils.resolveNumber(containerOrString);
                if (null != num) {
                    return wrap(RandomStringUtils.random(num.intValue()));
                } else {
                    return wrap(RandomStringUtils.random(10));
                }
            } else {
                String words = (String) containerOrString;
                if (words.contains(" ")) {
                    String[] s = words.split(" ");
                    List<String> clean = new ArrayList();
                    for (int i = 0; i < s.length; i++) {
                        if (!StringUtils.isEmpty(s[i])) {
                            clean.add(s[i].trim());
                        }
                    }
                    int random = RandomUtils.nextInt(0, clean.size());
                    return wrap(clean.get(random));
                } else {
                    return wrap(words);
                }
            }
        } else {
            return wrap(RandomStringUtils.random(5));
        }
    }

    private ContextWrapper evaluateRandomSentenceFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        if (operands.hasNext()) {
            ContextWrapper containerOrStringC = operands.next();
            Token token1 = argumentList.pop();
            Object containerOrString = containerOrStringC.get();

            if (null == containerOrString || !(containerOrString instanceof String)) {
                return wrap(RandomStringUtils.random(10));
            } else {
                String sentences = (String) containerOrString;
                if (sentences.contains(".")) {
                    String[] s = sentences.split("\\.");
                    List clean = new ArrayList();
                    for (int i = 0; i < s.length; i++) {
                        if (!StringUtils.isEmpty(s[i])) {
                            clean.add(s[i].trim());
                        }
                    }
                    int random = RandomUtils.nextInt(0, clean.size());
                    return wrap(clean.get(random));
                } else {
                    return wrap(sentences);
                }
            }
        }
        return wrap(RandomStringUtils.random(10));
    }

    private ContextWrapper evaluateRandomNumFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        if (operands.hasNext()) {
            ContextWrapper containerOrStringC = operands.next();
            Token token1 = argumentList.pop();

            Object containerOrString = containerOrStringC.get();

            if (null == containerOrString) {
                return wrap(RandomUtils.nextInt());
            } else {
                Number n = TypeUtils.resolveNumber(containerOrString);
                if (null == n) {
                    return wrap(RandomUtils.nextInt());
                }

                if (operands.hasNext()) {
                    Object max = operands.next();
                    Token t2 = argumentList.pop();

                    Number maxN = TypeUtils.resolveNumber(max);
                    if (null == maxN) {
                        return wrap(RandomUtils.nextInt(n.intValue(), Integer.MAX_VALUE));
                    }

                    // we'll support nextLong only if both values are specified
                    return wrap(RandomUtils.nextLong(n.intValue(), maxN.intValue()));

                } else {
                    return wrap(RandomUtils.nextInt(0, n.intValue()));
                }
            }
        } else {
            return wrap(RandomUtils.nextInt());
        }
    }

    private ContextWrapper evaluateContainsFunction(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                                    Object evaluationContext) {

        ContextWrapper containerOrStringC = operands.next();
        Token token1 = argumentList.pop();
        Object containerOrString = containerOrStringC.get();

        if (null == containerOrString || containerOrString instanceof NullObject) {
            while (operands.hasNext()) {
                operands.next();
                argumentList.pop();
            }

            return wrap(false);
        }

        ContextWrapper containsTypeC = operands.next();
        Token token2 = argumentList.pop();

        Object containsType = containsTypeC.get();

        Object value = null;
        String allOrAnyType = "ALL"; // can be ALL, ANY, ALL_INSENSITIVE, ANY_INSENSITIVE
        Boolean hasMore = false;
        if (!operands.hasNext()) {
            value = containsType;
        } else {
            hasMore = true;
            allOrAnyType = containsType.toString().toUpperCase();
        }

        if (containerOrString instanceof String) {
            if (!hasMore) {
                return wrap(((String) containerOrString).contains(value.toString()));
            } else {

                List<Boolean> bList = new ArrayList<>();
                while (operands.hasNext()) {
                    ContextWrapper oC = operands.next();
                    argumentList.pop();
                    Object o = oC.get();

                    if (allOrAnyType.contains("INSENSITIVE")) {
                        bList.add(((String) containerOrString).toLowerCase().contains(o.toString().toLowerCase()));
                    } else {
                        bList.add(((String) containerOrString).contains(o.toString()));
                    }

                }
                if (allOrAnyType.startsWith("ALL")) {
                    return wrap(BooleanUtils.and(bList.toArray(new Boolean[bList.size()])));
                } else {
                    return wrap(BooleanUtils.or(bList.toArray(new Boolean[bList.size()])));
                }
            }
        } else {
            List l = mapListResolver.resolveToList(containerOrString);
            if (null != l) {
                if (!hasMore) {
                    return wrap(l.contains(value));
                } else {

                    List<Boolean> bList = new ArrayList<>();
                    while (operands.hasNext()) {
                        ContextWrapper oC = operands.next();
                        argumentList.pop();
                        Object o = oC.get();

                        bList.add(l.contains(o));
                    }
                    if (allOrAnyType.startsWith("ALL")) {
                        return wrap(BooleanUtils.and(bList.toArray(new Boolean[bList.size()])));
                    } else {
                        return wrap(BooleanUtils.or(bList.toArray(new Boolean[bList.size()])));
                    }
                }
            } else {
                Map m = mapListResolver.resolveToMap(containerOrString);
                if (null != l) {
                    if (!hasMore) {
                        return wrap(m.containsKey(value));
                    }
                } else {

                    List<Boolean> bList = new ArrayList<>();
                    while (operands.hasNext()) {
                        ContextWrapper oC = operands.next();
                        argumentList.pop();
                        Object o = oC.get();
                        bList.add(m.containsKey(o));
                    }
                    if (allOrAnyType.startsWith("ALL")) {
                        return wrap(BooleanUtils.and(bList.toArray(new Boolean[bList.size()])));
                    } else {
                        return wrap(BooleanUtils.or(bList.toArray(new Boolean[bList.size()])));
                    }
                }
            }
        }


        return wrap(false);
    }


    private ContextWrapper replaceAll(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                      Object evaluationContext) {

        ContextWrapper objectFieldValueC = operands.next();
        Token token = argumentList.pop();

        ContextWrapper regexObjC = operands.next();
        Token token1 = argumentList.pop();

        ContextWrapper valueToReplaceInC = operands.next();
        Token token2 = argumentList.pop();

        Object objectFieldValue = objectFieldValueC.get();
        Object regexObj = regexObjC.get();
        Object valueToReplaceIn = valueToReplaceInC.get();

        if (objectFieldValue instanceof String) {
            String strRegex = regexObj.toString();
            if (strRegex.startsWith("#") && strRegex.endsWith("#")) {
                strRegex = strRegex.substring(1, strRegex.length() - 1);
            }
            String objectValue = (String) objectFieldValue;
            String replaced = objectValue.replaceAll(strRegex, valueToReplaceIn.toString());
            return wrap(replaced);
        }

        return wrap(null);
    }

    private ContextWrapper ifTrueFalse(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                       Object evaluationContext) {

        ContextWrapper trueFalseObjectC = operands.next();
        argumentList.pop();

        ContextWrapper leftValueC = operands.next();
        argumentList.pop();

        ContextWrapper rightValueC = operands.next();
        argumentList.pop();

        Object trueFalseObject = trueFalseObjectC.get();
        Object leftValue = leftValueC.get();
        Object rightValue = rightValueC.get();

        Boolean isTrue = true;
        if (null == trueFalseObject) {
            isTrue = false;
        } else if (trueFalseObject instanceof Boolean) {
            isTrue = (Boolean) trueFalseObject;
        } else if (trueFalseObject instanceof String) {
            try {
                isTrue = Boolean.parseBoolean((String) trueFalseObject);
            } catch (Exception e) {
                LOG.warn("Error processing Boolean in ifTrueFalse - value is: " + trueFalseObject, e);
            }
        } else if (trueFalseObject instanceof Number) {
            Number n = (Number) trueFalseObject;
            if (null != n) {
                isTrue = n.intValue() > 0;
            }
        }
        if (isTrue) {
            return wrap(leftValue);
        } else {
            return wrap(rightValue);
        }
    }

    private ContextWrapper stringFormat(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                        Object evaluationContext) {
        ContextWrapper valueObjectC = operands.next();
        argumentList.pop();

        Object valueObject = valueObjectC.get();

        List<Object> payload = new ArrayList<>();
        while (operands.hasNext()) {
            ContextWrapper valC = operands.next();
            argumentList.pop();
            Object val = valC.get();
            payload.add(val);
        }

        if (null != valueObject) {
            if (valueObject instanceof String) {
                Object[] arr = payload.toArray(new Object[payload.size()]);
                String valueResult = String.format((String) valueObject, arr);
                if (!StringUtils.isEmpty(valueResult) && valueResult.startsWith("#") && valueResult.endsWith("#")) {
                    valueResult = valueResult.substring(1, valueResult.length() - 1);
                }
                return wrap(valueResult);
            }
        }

        return wrap(null);
    }

    private ContextWrapper toStringValue(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                         Object evaluationContext) {
        ContextWrapper valueObjectC = operands.next();
        argumentList.pop();

        Object valueObject = valueObjectC.get();

        if (null == valueObject)
            return wrap(valueObject);

        if (operands.hasNext()) {
            ContextWrapper next = operands.next();
            Object formatO = next.get();
            String format = (String) formatO;
            argumentList.pop();

            if (!StringUtils.isEmpty(format)) {
                format = format.replace("#", "");
            }
            Instant instant = DateTimeUtils.resolve(valueObject);
            if (null != instant) {
                String formattedString = DateTimeFormatter.ofPattern(format).format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
                return wrap(formattedString);
            }
        }

        return wrap(valueObject.toString());
    }

    private ContextWrapper toLong(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                  Object evaluationContext) {
        ContextWrapper valueObjectC = operands.next();
        argumentList.pop();

        Object valueObject = valueObjectC.get();

        if (null == valueObject)
            return wrap(valueObject);

        Number number = resolveNumber(valueObject);
        if (null == number) {
            return wrap(number);
        }
        return wrap(number.longValue()); // resolveNumber(valueObject).longValue();
    }

    private ContextWrapper toDouble(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                    Object evaluationContext) {
        ContextWrapper valueObjectC = operands.next();
        argumentList.pop();

        Object valueObject = valueObjectC.get();

        if (null == valueObject)
            return wrap(valueObject);

        Number number = resolveNumber(valueObject);
        if (null == number) {
            return wrap(number);
        }
        Double d = new Double(number.toString()); // resolveNumber(valueObject).doubleValue();
        return wrap(d);
    }

    private ContextWrapper toInteger(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                     Object evaluationContext) {

        ContextWrapper valueObjectC = operands.next();
        argumentList.pop();

        Object valueObject = valueObjectC.get();

        if (null == valueObject)
            return wrap(valueObject);

        Number number = resolveNumber(valueObject);
        if (null == number) {
            return wrap(number);
        }
        // return resolveNumber(valueObject).intValue();
        return wrap(number.intValue());
    }

    private Number resolveNumber(Object valueObject) {
        Number num = null;

        if (valueObject instanceof Number) {
            num = (Number) valueObject;
        } else if (valueObject instanceof String) {
            String valueObjectStr = (String) valueObject;
            if (valueObjectStr.contains(",")) {
                NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMAN);
                try {
                    num = numberFormat.parse(valueObjectStr);
                } catch (ParseException e) {
                    num = NumberUtils.createNumber(valueObjectStr);
                }
            } else {
                num = NumberUtils.createNumber(valueObjectStr);
            }
        } else if (valueObject instanceof Instant) {
            Instant obj = (Instant) valueObject;
            num = new Long(obj.toEpochMilli());
        }

        return num;
    }

    private ContextWrapper extract(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                   Object evaluationContext) {

        ContextWrapper fieldValueObjectC = operands.next();
        argumentList.pop();
        Object fieldValueObject = fieldValueObjectC.get();

        if (null == fieldValueObject)
            return wrap(null);

        String fieldValue = fieldValueObject.toString().toLowerCase();

        ContextWrapper next = operands.next();
        argumentList.pop();
        Object topicValuesO = next.get();

        ArrayList list = new ArrayList();
        if (null != topicValuesO) {
            String topicValues = TypeUtils.resolveString(topicValuesO);
            topicValues = topicValues.replaceAll("#", "");

            String[] searchedTopics = topicValues.toLowerCase().split(",");

            for (int i = 0; i < searchedTopics.length; i++) {
                String k = searchedTopics[i].trim().toLowerCase();
                boolean exists = false;
                if (k.contains(" ")) {
                    String[] sk = k.split(" ");
                    for (int j = 0; j < sk.length; j++) {
                        String sk1 = sk[j];
                        if (fieldValue.contains(sk1)) {
                            exists = true;
                            break;
                        }
                    }
                } else {
                    if (fieldValue.contains(k)) {
                        exists = true;
                    }
                }
                if (exists) {
                    list.add(k);
                }
            }
        }

        return wrap(list);
    }

    private ContextWrapper regexReplace(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, PathExtractor evaluationContext) {
        ContextWrapper op1C = operands.next();
        argumentList.pop();

        Object op1 = op1C.get();

        if (null == op1)
            return wrap(null);

        String regexFieldValue = op1.toString();

        ContextWrapper regexPatternC = operands.next();
        argumentList.pop();

        Object regexPatternO = regexPatternC.get();
        String regexPattern = regexPatternO.toString();

        ContextWrapper replaceWithC = operands.next();
        argumentList.pop();
        Object replaceWithO = replaceWithC.get();

        String replaceWith = replaceWithO.toString();


        regexPattern = regexPattern.replace("#", "");
        Pattern r = Pattern.compile(regexPattern, Pattern.DOTALL);
        String replacedStr = r.matcher(regexFieldValue).replaceAll(replaceWith);

        return wrap(replacedStr);
    }

    private ContextWrapper regexExtract(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                        Object evaluationContext) {
        ContextWrapper op1C = operands.next();
        Token token = argumentList.pop();

        Object op1 = op1C.get();

        if (null == op1)
            return wrap(null);

        String regexFieldValue = op1.toString();

        ContextWrapper regexPatternC = operands.next();
        Token token1 = argumentList.pop();
        Object regexPatternO = regexPatternC.get();

        String regexPattern = regexPatternO.toString();

        Object group = null;
        if (operands.hasNext()) {
            ContextWrapper groupC = operands.next();
            Token token2 = argumentList.pop();
            group = groupC.get();
        }

        regexPattern = regexPattern.replace("#", "");
        Pattern r = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = r.matcher(regexFieldValue);

        int pos;
        List<String> list = new ArrayList<String>();

        while (matcher.find()) {
            int groupCount = matcher.groupCount();
            for (int i = 0; i < groupCount + 1; i++) {
                String gr = matcher.group(i);
                list.add(gr);
            }
        }

        if (list.size() > 1) {
            if (null != group) {
                Integer groupInt = TypeUtils.resolveInteger(group);
                return wrap(list.get(groupInt));
            } else {
                return wrap(list);
            }
        }

        return wrap(list.size() != 0 ? list.get(0) : "");
    }

    private ContextWrapper regexMatch(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                      Object evaluationContext) {

        ContextWrapper op1C = operands.next();
        argumentList.pop();
        Object op1 = op1C.get();
        if (null == op1)
            return wrap(null);
        String regexFieldValue = op1.toString();

        ContextWrapper regexPatternC = operands.next();

        argumentList.pop();
        String regexPattern = regexPatternC.get().toString();

        regexPattern = regexPattern.replace("#", "");

        Pattern r = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = r.matcher(regexFieldValue);

        boolean matches = matcher.find();

        return wrap(matches);
    }

    private ContextWrapper toDate(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                  Object evaluationContext) {

        ContextWrapper valueObjectC = operands.next();
        Token valueObjectToken = argumentList.pop();
        Object valueObject = valueObjectC.get();

        String formatObject = null;

        if (operands.hasNext()) {
            ContextWrapper formatObjectC = operands.next();
            Token formatObjectToken = argumentList.pop();
            formatObject = (String) formatObjectC.get();
        }
        String format = null;
        if (!StringUtils.isEmpty(formatObject)) {
            format = formatObject.replace("#", "");
        }

        Object timeZoneObject = null;
        if (operands.hasNext()) {
            timeZoneObject = operands.next();
            argumentList.pop();
        }

        Instant leftDateTime = null;

        if (valueObject instanceof String) {
            String strDateTime = (String) valueObject;

            // first try to resolve as object
            Instant dt = DateTimeUtils.resolve(valueObject);
            if (null == dt) {
                dt = DateTimeUtils.resolve(strDateTime, format);
            }
            if (null != dt) {

                leftDateTime = DateTimeUtils.format(dt, format);

                /*
                 * DateTimeFormatter formatter =
                 * DateTimeFormat.forPattern(format).withLocale(Locale.ENGLISH);
                 * String s = dt.toString(format); leftDateTime =
                 * formatter.withZoneUTC().parseDateTime(s);
                 */
            }

        } else if (valueObject instanceof DateTime) {
            Instant dt = DateTimeUtils.format((DateTime) valueObject, format);
            leftDateTime = dt;
            // leftDateTime =
            // ((DateTime)valueObject).toDateTime(DateTimeZone.UTC);
        } else if (valueObject instanceof Date) {
            // leftDateTime = new DateTime(((Date)valueObject).getTime(),
            // DateTimeZone.UTC);
            Instant dt = DateTimeUtils.format((Date) valueObject, format);
            leftDateTime = dt;
        } else if (valueObject instanceof Number) {
            Number valueNumber = (Number) valueObject;
            Long value = valueNumber.longValue();
            Instant dt = DateTimeUtils.format(value, format);
            leftDateTime = dt;
            // leftDateTime = new DateTime(value, DateTimeZone.UTC);

        }

        if (null != leftDateTime) {
            /*
             * DateTimeFormatter formatter =
             * DateTimeFormat.forPattern(format).withLocale(Locale.ENGLISH);
             * String val = leftDateTime.toString(formatter); DateTime dateTime
             * = formatter.withZoneUTC().parseDateTime(val);
             */

            Instant i = DateTimeUtils.format(leftDateTime, format);

            if (null != timeZoneObject) {
                i = DateTimeUtils.resolve(i, timeZoneObject);
            }

            return wrap(i); // leftDateTime.atOffset(ZoneOffset.UTC);//dateTime;
        }

        return wrap(null);
    }

    private ContextWrapper strToDateTimeStamp(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                              Object evaluationContext) {

        ContextWrapper dataStringC = operands.next();
        argumentList.pop();

        String dateString = dataStringC.get().toString().replace("#", "");

        ContextWrapper stringFormatC = operands.next();
        Token token1 = argumentList.pop();

        String stringFormat = stringFormatC.get().toString().replace("#", "");


        if (stringFormat instanceof String && dateString instanceof String) {
            try {
                /*
                 * DateTimeFormatter df =
                 * DateTimeFormat.forPattern(stringFormat); DateTime dt =
                 * DateTime.parse(dateString, df);
                 */
                Instant dt = DateTimeUtils.resolve(dateString, stringFormat);
                return wrap(dt.toEpochMilli());
            } catch (Exception e) {
                System.out.println("Parse date error for date: " + dateString + " and format: " + stringFormat + " - "
                        + e.getMessage());
            }
        }

        return wrap(null);
    }

    protected ContextWrapper nextFunctionEvaluate(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList,
                                                  Object evaluationContext) {
        return super.evaluate(function, operands, argumentList, evaluationContext);
    }


    @Override
    protected ContextWrapper evaluate(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {

        if (operator == POW) {
            Tuple<Number, Number> numberTuple = getNumberTuple(operands);

            if (null != numberTuple.getKey() && null != numberTuple.getValue()) {
                return wrap(Math.pow(numberTuple.getKey().doubleValue(), numberTuple.getValue().doubleValue()));
            }
            throw new ExpressionValidationException("Values cannot be null type");

        } else if (operator == MODULO) {
            Tuple<Number, Number> numberTuple = getNumberTuple(operands);

            // TODO - write test ases for Modulo
            if (null != numberTuple.getKey() && null != numberTuple.getValue()) {
                return wrap(numberTuple.getKey().doubleValue() % numberTuple.getValue().doubleValue());
            }
            return wrap(0);

        } else if (operator == DIVIDE) {
            ContextWrapper leftC = operands.next();
            ContextWrapper rightC = operands.next();

            Object left = leftC.get();
            Object right = rightC.get();


            Number lN = null;
            Number rN = null;
            if (left instanceof Number) {
                lN = (Number) left;
            } else if (left instanceof String) {
                lN = resolveNumber(left);
            }

            if (right instanceof Number) {
                rN = (Number) right;
            } else if (right instanceof String) {
                rN = resolveNumber(right);
            }
            if (null != lN && null != rN) {
                return wrap(lN.doubleValue() / rN.doubleValue());
            }
            return wrap(0);
        } else if (operator == MULTIPLY) {

            ContextWrapper leftC = operands.next();
            ContextWrapper rightC = operands.next();

            Object left = leftC.get();
            Object right = rightC.get();


            Number lN = null;
            Number rN = null;
            if (left instanceof Number) {
                lN = (Number) left;
            } else if (left instanceof String) {
                lN = resolveNumber(left);
            }

            if (right instanceof Number) {
                rN = (Number) right;
            } else if (right instanceof String) {
                rN = resolveNumber(right);
            }
            if (null != lN && null != rN) {
                return wrap(lN.doubleValue() * rN.doubleValue());
            }
            return wrap(0);
        } else if (operator == NEGATE) {
            ContextWrapper nextC = operands.next();
            Object next = nextC.get();

            if (next == null) {
                return wrap(false);
            }
            Boolean nextB = TypeUtils.resolveBoolean(next);
            if (null != nextB) {
                return wrap(!nextB);
            } else {
                return wrap(!true);
            }
        } else if (operator == NOT_EQUAL) {
            Boolean booleanRes = getEqual(operator, operands, evaluationContext);
            return wrap(!booleanRes);
        } else if (operator == EQUAL) {
            Boolean booleanRes = getEqual(operator, operands, evaluationContext);
            return wrap(booleanRes);
        } else if (operator == LOWER_THEN_OR_EQUAL) {

            ContextWrapper leftC = operands.next();
            ContextWrapper rightC = operands.next();

            Object left = leftC.get();
            Object right = rightC.get();


            if (null == left || null == right) {
                return wrap(false);
            }

            Number l = (Number) left;
            Number r = (Number) right;
            // if (null!=l && null!=r)
            return wrap(l.doubleValue() <= r.doubleValue());

        } else if (operator == GREATER_THEN_OR_EQUAL) {

            ContextWrapper leftC = operands.next();
            ContextWrapper rightC = operands.next();

            Object left = leftC.get();
            Object right = rightC.get();


            if (null == left || null == right) {
                return wrap(false);
            }

            Number l = (Number) left;
            Number r = (Number) right;
            return wrap(l.doubleValue() >= r.doubleValue());
        } else if (operator == GREATER_THEN) {
            ContextWrapper leftC = operands.next();
            ContextWrapper rightC = operands.next();

            Object left = leftC.get();
            Object right = rightC.get();

            if (null == left || null == right) {
                return wrap(false);
            }

            // check this:
            // http://stackoverflow.com/questions/2683202/comparing-the-values-of-two-generic-numbers
            Number l = (Number) left;
            Number r = (Number) right;
            // if (null!=l && null!=r)
            return wrap(l.doubleValue() > r.doubleValue());
            // else return false;
        } else if (operator == LOWER_THEN) {
            ContextWrapper leftC = operands.next();
            ContextWrapper rightC = operands.next();

            Object left = leftC.get();
            Object right = rightC.get();


            if (null == left || null == right) {
                return wrap(false);
            }
            // check this:
            // http://stackoverflow.com/questions/2683202/comparing-the-values-of-two-generic-numbers
            Number l = (Number) left;
            Number r = (Number) right;
            // if (null!=l && null!=r)
            return wrap(l.doubleValue() < r.doubleValue());
            // else return false;
        } else if (operator == AND) {
            ContextWrapper leftC = operands.next();
            ContextWrapper rightC = operands.next();

            Object left = leftC.get();
            Object right = rightC.get();

            if (null == left || null == right) {
                return wrap(false);
            }

            boolean l = (Boolean) left;
            boolean r = (Boolean) right;
            return wrap(l && r); // Boolean.logicalAnd(l,r);
        } else if (operator == OR) {
            ContextWrapper leftC = operands.next();
            ContextWrapper rightC = operands.next();

            Object left = leftC.get();
            Object right = rightC.get();

            if (null == left || null == right) {
                return wrap(false);
            }

            boolean l = (Boolean) left;
            boolean r = (Boolean) right;
            return wrap(l || r); // Boolean.logicalOr(l,r);
        } else if (operator == PLUS) {

            ContextWrapper leftC = operands.next();
            ContextWrapper rightC = operands.next();

            Object left = leftC.get();
            Object right = rightC.get();


            if (left instanceof String || right instanceof String) {
                String l, r;
                if (left instanceof String) {
                    l = (String) left;
                } else {
                    l = (null == left ? "" : left.toString());
                }
                if (right instanceof String) {
                    r = (String) right;
                } else {
                    r = (null == right ? "" : right.toString());
                }
                return wrap(l + r);
            } else {
                Number l = (Number) left;
                if (null == l)
                    l = 0;
                Number r = (Number) right;
                if (null == r)
                    r = 0;
                return wrap(l.doubleValue() + r.doubleValue());
            }

        } else if (operator == MINUS) {
            ContextWrapper leftC = operands.next();
            Object left = leftC.get();
            if (null == left)
                return wrap(null);

            ContextWrapper rightC = operands.next();
            Object right = rightC.get();

            if (null == right)
                return wrap(null);
            Number l = (Number) left;
            Number r = (Number) right;

            if (null == l || null == r)
                return null;

            return wrap(l.doubleValue() - r.doubleValue());
        } else {
            return nextOperatorEvaluate(operator, operands, evaluationContext);
        }
        //return false;
    }

    private Boolean getEqual(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();

        Object left = leftC.get();
        Object right = rightC.get();


        if (null == left || right == null) {
            return false; // left.equals(right);
        }
        if (null == left && null == right) {
            return true;
        }
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() == ((Number) right).doubleValue();
        } else {
            if (left instanceof Boolean || right instanceof Boolean) {
                Boolean l = Boolean.parseBoolean(left.toString());
                Boolean r = Boolean.parseBoolean(right.toString());
                return l.equals(r);
            } else if (left instanceof String || right instanceof String) {
                return left.toString().equalsIgnoreCase(right.toString());
            } else {
                return left.equals(right);
            }
        }
    }

    private Tuple<Number, Number> getNumberTuple(Iterator<ContextWrapper> operands) {
        ContextWrapper leftC = operands.next();
        ContextWrapper rightC = operands.next();
        Object left = leftC.get();
        Object right = rightC.get();

        Number lN = null;
        Number rN = null;
        if (left instanceof Number) {
            lN = (Number) left;
        } else if (left instanceof String) {
            lN = resolveNumber(left);
        }

        if (right instanceof Number) {
            rN = (Number) right;
        } else if (right instanceof String) {
            rN = resolveNumber(right);
        }

        return new Tuple<>(lN, rN);
    }

    protected ContextWrapper nextOperatorEvaluate(Operator operator, Iterator<ContextWrapper> operands, Object evaluationContext) {
        return super.evaluate(operator, operands, evaluationContext);
    }

    /*
     * protected Object superOperatorEvaluate(Operator operator,
     * Iterator<Object> operands, Object evaluationContext) { return
     * super.evaluate(operator, operands, evaluationContext); }
     */

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
