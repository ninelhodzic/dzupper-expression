package org.datazup.expression.evaluators;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.datazup.expression.*;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.expression.exceptions.ExpressionValidationException;
import org.datazup.expression.exceptions.NotSupportedExpressionException;
import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.utils.DateTimeUtils;
import org.datazup.utils.TypeUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionEvaluateUtils extends EvaluatorBase {
    protected static final Logger LOG = LoggerFactory.getLogger(FunctionEvaluateUtils.class);

    public FunctionEvaluateUtils(AbstractResolverHelper mapListResolver, ExecutionContext executionContext) {
        super(mapListResolver, executionContext);
    }

    public ContextWrapper getIndexOf(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getRegexReplace(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getNow(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return executionContext.create(System.currentTimeMillis()); //Instant.now();
    }

    public ContextWrapper getDateNow(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return executionContext.create(Instant.now()); //Instant.now();
    }

    public ContextWrapper getIsNull(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper op1 = operands.next();
        Token token = argumentList.pop();
        Object resolved = op1.get();
        if (null == resolved || resolved instanceof NullObject || resolved.toString() == token.getContent().toString()) {
            return executionContext.create(true);
        } else if (resolved instanceof String) {
            return executionContext.create(((String) resolved).isEmpty());
        }
        return executionContext.create(resolved == null);
    }

    public ContextWrapper getSetNull(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        Object op1 = operands.next();
        argumentList.pop();
        return executionContext.create(null); // getNullObject();
    }

    public ContextWrapper getSizeOf(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    }

    public ContextWrapper getTypeOf(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    }

    public ContextWrapper getIsOfType(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return getIsOfTypeInternal(function, operands, argumentList, evaluationContext);
    }

    private ContextWrapper getIsOfTypeInternal(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getIf(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    protected ContextWrapper wrap(Object object) {
        return executionContext.create(object);
    }


    public ContextWrapper getStrToDateTimestamp(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {

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

    public ContextWrapper getMinute(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper op1 = operands.next();
        argumentList.pop();
        Instant dt = DateTimeUtils.resolve(op1.get());
        return executionContext.create(DateTimeUtils.getMinute(dt));
    }

    public ContextWrapper getHour(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper op1 = operands.next();
        argumentList.pop();
        Instant dt = DateTimeUtils.resolve(op1.get());
        return executionContext.create(DateTimeUtils.getHour(dt));
    }

    public ContextWrapper getDay(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper op1 = operands.next();
        argumentList.pop();
        Instant dt = DateTimeUtils.resolve(op1.get());
        return executionContext.create(DateTimeUtils.getDayOfMonth(dt));
    }

    public ContextWrapper getWeek(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper op1 = operands.next();
        argumentList.pop();
        Instant dt = DateTimeUtils.resolve(op1.get());
        LocalDateTime localDate = LocalDateTime.ofInstant(dt, ZoneId.systemDefault());
        Integer res = localDate.get(WeekFields.of(Locale.ENGLISH).weekOfMonth()); //DateTimeUtils.getDayOfMonth(dt) % 7;
        return executionContext.create(res);
    }

    public ContextWrapper getWeekOfYear(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper op1 = operands.next();
        argumentList.pop();
        Instant dt = DateTimeUtils.resolve(op1.get());
        LocalDateTime localDate = LocalDateTime.ofInstant(dt, ZoneId.systemDefault());
        Integer res = localDate.get(WeekFields.of(Locale.ENGLISH).weekOfYear()); //DateTimeUtils.getDayOfMonth(dt) % 7;

        return executionContext.create(res);
    }

    public ContextWrapper getMonth(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper op1 = operands.next();
        argumentList.pop();
        Instant dt = DateTimeUtils.resolve(op1.get());
        return executionContext.create(DateTimeUtils.getMonth(dt));
    }

    public ContextWrapper getYear(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper op1 = operands.next();
        argumentList.pop();
        Instant dt = DateTimeUtils.resolve(op1.get());
        return executionContext.create(DateTimeUtils.getYear(dt));
    }

    public ContextWrapper getDateDiff(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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
        if (null == firstDTObj || null == secondDTObj)
            return wrap(null);

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

    public ContextWrapper getDateMinus(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return manageDateAddSubtract("MINUS", operands, argumentList);
    }

    public ContextWrapper getDatePlus(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return manageDateAddSubtract("PLUS", operands, argumentList);
    }

    public ContextWrapper getDateStart(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {

        ContextWrapper firstDTObjContext = operands.next();
        argumentList.pop();

        Object dt1 = firstDTObjContext.get();
        if (null == dt1) {
            return wrap(null);
        }

        Instant date = DateTimeUtils.resolve(dt1);
        if (null == date) {
            return wrap(null);
        }
        LocalDate localDateTime = LocalDateTime.ofInstant(date, ZoneId.systemDefault()).toLocalDate();
        Instant res = LocalDateTime.of(localDateTime, LocalTime.MIN).toInstant(ZoneOffset.UTC);

        return wrap(res);
    }

    public ContextWrapper getDateEnd(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper firstDTObjContext = operands.next();
        argumentList.pop();

        Object dt1 = firstDTObjContext.get();
        if (null == dt1) {
            return wrap(null);
        }

        Instant date = DateTimeUtils.resolve(dt1);
        if (null == date) {
            return wrap(null);
        }
        LocalDate localDateTime = LocalDateTime.ofInstant(date, ZoneId.systemDefault()).toLocalDate();
        Instant res = LocalDateTime.of(localDateTime, LocalTime.MAX).toInstant(ZoneOffset.UTC);

        return wrap(res);

    }

    public ContextWrapper getToDate(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {

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
        } else if (valueObject instanceof Instant) {
            leftDateTime = (Instant) valueObject;
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

    public ContextWrapper getToInt(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {

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

    public ContextWrapper getToLong(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getToDouble(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getToString(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object Object) {

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

    public ContextWrapper getToBoolean(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper valueObjC = operands.next();
        argumentList.pop();
        Object valueObj = valueObjC.get();
        if (null == valueObj) {
            return executionContext.create(false);
        }
        return executionContext.create(TypeUtils.resolveBoolean(valueObj));

    }

    public ContextWrapper getTrue(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return executionContext.create(true);
    }

    public ContextWrapper getFalse(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return executionContext.create(false);
    }

    public ContextWrapper getToLowercase(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        ContextWrapper op1 = operands.next();
        argumentList.pop();
        String val = TypeUtils.resolveString(op1.get());
        if (StringUtils.isEmpty(val))
            return op1;
        else
            return executionContext.create(val.toLowerCase());
    }

    public ContextWrapper getToUpperCase(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {

        ContextWrapper op1 = operands.next();
        argumentList.pop();

        String val = TypeUtils.resolveString(op1);
        if (StringUtils.isEmpty(val))
            return op1;
        else
            return executionContext.create(val.toUpperCase());
    }

    public ContextWrapper getAbs(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getRound(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getCeil(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getStringFormat(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getReplaceAll(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {

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

    public ContextWrapper getSplitter(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getSubstring(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getRegexMatch(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getRegexExtract(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getExtract(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getUUIDString(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        /*String randomString = RandomStringUtils.randomAlphabetic(32);
        if (operands.hasNext()) {
            ContextWrapper containerOrStringC = operands.next();
            Token token1 = argumentList.pop();
            Object containerOrString = containerOrStringC.get();
            String tmp = TypeUtils.resolveString(containerOrString);
            if (null!=tmp && !tmp.isEmpty()){
                randomString = tmp;
            }
        }*/
        UUID uuid = UUID.randomUUID();//.fromString(randomString);
        return wrap(uuid.toString());
    }

    private Boolean inSensitiveResolve(String container, String matcher, String allOrAnyType){
        if (allOrAnyType.contains("INSENSITIVE")) {
            return (container).toLowerCase().contains(matcher.toLowerCase());
        } else {
            return (container).contains(matcher);
        }
    }

    public ContextWrapper getContains(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {

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
                Boolean res = inSensitiveResolve((String)containerOrString, value.toString(), allOrAnyType);
                return wrap(res);
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

    public ContextWrapper getRandomNum(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getRandomSentence(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getRandomWord(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getRandomChar(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getRandomAlphabetic(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return evaluateRandomFunction("randomAlphabetic", function, operands, argumentList, evaluationContext);
    }

    private ContextWrapper evaluateRandomFunction(String type, Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
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

    public ContextWrapper getRandomAlphanumeric(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return evaluateRandomFunction("randomAlphanumeric", function, operands, argumentList, evaluationContext);
    }

    public ContextWrapper getRandomNumeric(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return evaluateRandomFunction("randomNumeric", function, operands, argumentList, evaluationContext);
    }

    public ContextWrapper getRandomString(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return evaluateRandomFunction("random", function, operands, argumentList, evaluationContext);
    }

    public ContextWrapper getRandomAscii(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return evaluateRandomFunction("randomAscii", function, operands, argumentList, evaluationContext);
    }

    public ContextWrapper getRandomGraph(Function function, Iterator<ContextWrapper> operands, Deque<Token> argumentList, Object evaluationContext) {
        return evaluateRandomFunction("randomGraph", function, operands, argumentList, evaluationContext);
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
}
