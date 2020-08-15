package expression;

import base.TestBase;
import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.SimpleObjectEvaluator;
import org.datazup.expression.context.ConcurrentExecutionContext;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.pathextractor.PathExtractor;
import org.datazup.pathextractor.SimpleResolverHelper;
import org.datazup.utils.DateTimeUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by admin@datazup on 3/15/16.
 */

public class SimpleObjectEvaluatorTest extends TestBase {

    private static ExecutionContext executionContext = new ConcurrentExecutionContext();

    /*@Test
    public void isEmptyExpressionEvaluatedAsTrueTest() throws EvaluatorException{
        String expression = "";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean)evaluaged;
        Assert.assertTrue(b);
    }*/

    @Test
    public void evaluateSizeOfListExpressionTest() throws EvaluatorException {
        String expression = "SIZE_OF($list$)==1";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean) evaluaged;
        Assert.assertTrue(b);
    }

    @Test
    public void evaluateNullFunctionMissingExpressionTest() throws EvaluatorException {
        String expression = "IS_NULL(child.name.nemaovo)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean) evaluaged;
        Assert.assertTrue(b);
    }

    @Test
    public void evaluateNullFunctionExpressionTest() throws EvaluatorException {
        String expression = "IS_NULL(child.name1)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean) evaluaged;
        Assert.assertTrue(b);
    }

    @Test
    public void evaluateNotNullFunctionExpressionTest() throws EvaluatorException {
        String expression = "!IS_NULL(child.name)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean) evaluaged;
        Assert.assertTrue(b);
    }

    public void resolveComplexExpressionTest() throws EvaluatorException {
        String expression = "($child.name$ == 'child' && $number$==5.30 && 6.30-1 == $number$) && (NOW() < NOW() || NOW()==NOW())";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
    }

    public Object evaluate(String expression) throws EvaluatorException {
        SimpleResolverHelper mapListResolver = new SimpleResolverHelper();
        PathExtractor pathExtractor = new PathExtractor(getData(), mapListResolver);
        SimpleObjectEvaluator evaluator = SimpleObjectEvaluator.getInstance(executionContext, mapListResolver); //new SimpleObjectEvaluator();
        ContextWrapper wrapper = evaluator.evaluate(expression, pathExtractor);
        return wrapper.get();
    }

    @Test
    public void evaluateRegexFunctionExpressionTest() throws EvaluatorException {
        String expression = "REGEX_MATCH($child.name$, '\\bchild\\b')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean) evaluaged;
        Assert.assertTrue(b);
    }

    @Test
    public void evaluateRegexFunctionExpressionStringTest() throws EvaluatorException {
        String expression = "REGEX_MATCH('this is [ERROR] of something', '#\\[ERROR\\]#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean) evaluaged;
        Assert.assertTrue(b);
    }

    @Test
    public void evaluateRegexExtractFunctionExpressionTest() throws EvaluatorException {
        String expression = "REGEX_EXTRACT($child.name$, '#(?<=c)(.*)(?=d)#',0)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue(evaluaged.equals("hil"));

    }


    @Test
    public void evaluateRegexExtractMulitFunctionExpressionTest() throws EvaluatorException {
        String expression = "REGEX_EXTRACT($log$, '#(?<class>[^\\s]+)+\\s(?<thread>[^\\s]+)+\\s(?<level>[^\\s]+)+\\s(?<message>.*)#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof List);
        Assert.assertTrue(((List) evaluaged).size() == 5);
    }

    @Test
    public void evaluateRegexExtractHtmlTest() throws EvaluatorException {

        // #<a[^>]*?>(.*?)<\/a>#
        // #<a.*>(.*?)<#
        String expression = "REGEX_EXTRACT($child.html$, '#<a[^>]*?>(.*?)</a>#', 1)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue(evaluaged.equals("Twitter for iPhone"));
    }

    @Test
    public void evaluateRegexMatchHtmlTest() throws EvaluatorException {

        // #<a[^>]*?>(.*?)<\/a>#
        // #<a.*>(.*?)<#
        String expression = "REGEX_MATCH($child.html$, '#<a.*#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue(evaluaged.equals(Boolean.TRUE));
    }

    @Test
    public void evaluateRegexExtractByGroupFunctionExpressionTest() throws EvaluatorException {
        String expression = "REGEX_EXTRACT($child.name$, '#(?<=c)(.*)(?=d)#',1)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
    }

    @Test
    public void evaluateRegexExtractNumber() throws EvaluatorException {
        String expression = "TO_INT(REGEX_EXTRACT($fieldPrice$, '#\\d+#',1))";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Number);
        Assert.assertTrue(evaluaged.equals(20));
    }

    @Test
    public void evaluateRegexExtractNumberWithSeparator() throws EvaluatorException {
        String expression = "TO_DOUBLE(REGEX_EXTRACT($fieldPrice1$, '#\\d+,.\\d+#'))";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Number);
        Assert.assertTrue(evaluaged.equals(20.15));
    }

    @Test
    public void evaluateRegexExtractNumberWithSeparator2() throws EvaluatorException {
        String expression = "TO_DOUBLE(REGEX_EXTRACT($fieldPrice2$, '#\\d+(,|.)\\d+#',0))";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Number);
        Assert.assertTrue(evaluaged.equals(20.15d));
    }

    /*
    @Test
    public void evaluateRegexExtractByKeywordsTest(){
        String expression = "REGEX_EXTRACT($text$, '#(\\bthis\\b|\\blonger\\b|\\btest.*[])(?!.*\\1)#',0)";

        //Note: this regex will match only full words so any prefix/suffix will not be matched
        // such  as: test will not match "testing"... etc

        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof List);
        List l = (List)evaluaged;
        Assert.assertTrue(l.size()==2);
        Assert.assertTrue("this".equals(l.get(0)));
        Assert.assertTrue("longer".equals(l.get(1)));
      //  Assert.assertTrue("test".equals(l.get(2)));
    }
    */

    @Test
    public void evaluateExtractorFnTest() throws EvaluatorException {
        String expression = "EXTRACT($text$, '#this,longer,   test, has purposes#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof List);
        List l = (List) evaluaged;
        Assert.assertTrue(l.size() == 4);
        Assert.assertTrue("this".equals(l.get(0)));
        Assert.assertTrue("longer".equals(l.get(1)));
        Assert.assertTrue("test".equals(l.get(2)));
        Assert.assertTrue("has purposes".equals(l.get(3)));
    }

    @Test
    public void evaluateContainsStringSimpleTest() throws EvaluatorException {

        String expression = "CONTAINS($text$, 'longer text')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean) evaluaged);
    }

    @Test
    public void evaluateContainsStringSimpleCaseInsensitiveTest() throws EvaluatorException {

        String expression = "CONTAINS($text$,'ALL_INSENSITIVE', 'longer TEXT')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean) evaluaged);
    }

    @Test
    public void evaluateContainsStringSimpleCaseInsensitiveMoreTest() throws EvaluatorException {

        String expression = "CONTAINS($text$,'ALL_INSENSITIVE', 'longer TEXT', 'Test', 'tesTing purposes')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean) evaluaged);
    }

    @Test
    public void evaluateContainsStringSimpleCaseInsensitiveFalseTest() throws EvaluatorException {

        String expression = "CONTAINS($text$,'ALL', 'longer TEXT')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertFalse((Boolean) evaluaged);
    }

    @Test
    public void evaluateContainsStringSimpleNotMatchTest() throws EvaluatorException {

        String expression = "CONTAINS($text$, 'longer text 1')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertFalse((Boolean) evaluaged);
    }

    @Test
    public void evaluateContainsListSimpleTest() throws EvaluatorException {

        String expression = "CONTAINS($child.list$, 'Hello')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean) evaluaged);
    }

    @Test
    public void evaluateContainsListSimpleFalseTest() throws EvaluatorException {

        String expression = "CONTAINS($child.list$, 'Hello 1')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertFalse((Boolean) evaluaged);
    }

    @Test
    public void evaluateContainsListAnyTest() throws EvaluatorException {

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "CONTAINS($child.list$,'ANY', 'Hello', 'ahoj')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean) evaluaged);
    }

    @Test
    public void evaluateContainMapKeyAllTest() throws EvaluatorException {

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "CONTAINS($child$,'ALL', 'list', 'html')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean) evaluaged);
    }

    @Test
    public void evaluateRandomTest() throws EvaluatorException {

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "RANDOM_NUM()";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Integer);
        System.out.println(evaluaged);
    }

    @Test
    public void evaluateRandomMaxTest() throws EvaluatorException {

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "RANDOM_NUM(50)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Integer);
        System.out.println(evaluaged);
    }

    @Test
    public void evaluateRandomMinMaxTest() throws EvaluatorException {

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "RANDOM_NUM(1, 10)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Number);
        System.out.println(evaluaged);
    }

    @Test
    public void evaluateRandomChartTest() throws EvaluatorException {

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "RANDOM_CHAR(' ')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        System.out.println(evaluaged);
    }

    @Test
    public void evaluateRandomWordTest() throws EvaluatorException {

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "RANDOM_WORD('This is the word')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        System.out.println(evaluaged);
    }

    @Test
    public void evaluateRandomSentenceTest() throws EvaluatorException {

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "RANDOM_SENTENCE('This is the word. Another Sentence. Third to check... more to solve.. this is interesting')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        System.out.println(evaluaged);
    }

    @Test
    public void evaluateSplitterDocTest() throws EvaluatorException {

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "SPLITTER('This is the word. Another Sentence. Third to check... more to solve.. this is interesting','\\.', 'en', 'true')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof List);
        System.out.println(evaluaged);
    }

    @Test
    public void testAbsValue() throws EvaluatorException {
        String expression = "ABS($child.value3$)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Integer);

        Integer val = (Integer) evaluaged;
        Assert.assertTrue(val == 5);

    }

    @Test
    public void testAbsStringValue() throws EvaluatorException {
        String expression = "ABS('-10')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Number);

        Number val = (Number) evaluaged;
        Assert.assertTrue(val.doubleValue() == 10d);
    }

    @Test
    public void testRoundDoubleValue() throws EvaluatorException {
        String expression = "ROUND(0-10.23123523, 2)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Number);

        Number val = (Number) evaluaged;
        Assert.assertTrue(val.floatValue() == -10.24f);
    }

    @Test
    public void testCeilDoubleValue() throws EvaluatorException {
        String expression = "CEIL(0-10.23123523, 2)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Number);

        Number val = (Number) evaluaged;
        Assert.assertTrue(val.floatValue() == -10.23f);
    }

    @Test
    public void evaluateSubstringTest1() throws EvaluatorException {
        String expression = "SUBSTRING('Hello world!', 2, 5)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue(evaluaged.equals("llo"));
        System.out.println(evaluaged);
    }

    @Test
    public void evaluateSubstringTest2() throws EvaluatorException {
        String expression = "SUBSTRING('Hello world!', 2)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue(evaluaged.equals("llo world!"));
        System.out.println(evaluaged);
    }


    @Test
    public void evaluateSimpleDateConversionBasedOnFormatTest() {
        DateTime dt = DateTime.now(DateTimeZone.UTC);
        String format = "YYYY";

        Integer year = dt.getYear();

        DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
        String str = dt.toString(formatter);

        Assert.assertTrue(str.equals(year.toString()));

        DateTime dtNew = formatter.withZoneUTC().parseDateTime(str);
        String str1 = dtNew.toString(formatter);
        Assert.assertTrue(str1.startsWith(year.toString()));

    }

    private void assertDayDates(Instant dt) {
        Assert.assertTrue(DateTimeUtils.getHour(dt) == 0);//dt.get(ChronoField.HOUR_OF_DAY)==0);
        Assert.assertTrue(DateTimeUtils.getMinute(dt) == 0);//dt.get(ChronoField.MINUTE_OF_DAY)==0);//dt.getMinuteOfDay()==0);
        Assert.assertTrue(DateTimeUtils.getSecond(dt) == 0);//dt.get(ChronoField.SECOND_OF_DAY)==0);//dt.getSecondOfDay()==0);
        assertDateZeroMinutes(dt);
    }

    private void assertDateZeroMinutes(Instant dt) {

        Assert.assertTrue(DateTimeUtils.getMinute(dt) == 0);//dt.get(ChronoField.MINUTE_OF_HOUR)==0);//dt.getMinuteOfHour()==0);
        Assert.assertTrue(DateTimeUtils.getSecond(dt) == 0);//dt.get(ChronoField.SECOND_OF_MINUTE)==0);//dt.getSecondOfMinute()==0);
    }

    @Test
    public void evaluateDateTimeToDateFormatExpressionTest() throws EvaluatorException {
        String expression = "TO_DATE($dateTime$, '#YYYY-MM-dd#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;
        assertDayDates(dt);
    }

    @Test
    public void evaluateAdditionToDate() throws EvaluatorException {
        String expression = "DATE_DIFF(DATE_MINUS(NOW(), 10, 'DAYS'), DATE_PLUS(NOW(), 10, 'DAYS'), 'DAYS')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertNotNull(evaluaged instanceof Number);
        Number number = (Number)evaluaged;
        Assert.assertTrue(number.intValue()==20);
    }

    @Test
    public void evaluateDateTimeFromLongAndTimeZone() throws EvaluatorException {
        String expression = "TO_DATE($ts$,  '', $tz$)";

        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;
        System.out.println(dt.toEpochMilli() + " " + dt.toString() + ", orig: " + 1464811823300L + " (06/01/2016 @ 8:10pm (UTC)) - offset: " + 240);
    }

    @Test
    public void evaluateDateTimeWeekOfMonthTest() throws EvaluatorException {
        String expression = "WEEK(TO_DATE('20171219121750.783Z'))";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Integer);
        Assert.assertTrue(evaluaged.equals(4));
    }

    @Test
    public void evaluateDateTimeWeekOfYEarTest() throws EvaluatorException {
        String expression = "WEEK_OF_YEAR(TO_DATE('20171219121750.783Z'))";
        Object evaluated = evaluate(expression);
        Assert.assertNotNull(evaluated);
        Assert.assertTrue(evaluated instanceof Integer);
        Assert.assertTrue(evaluated.equals(51));
        System.out.println("Week of the year: " + evaluated + " for date: 20171219121750.783Z");
    }

    @Test
    public void evaluateDateTimeToDateFormatExpressionLdapFormatTest() throws EvaluatorException {
        String expression = "TO_DATE('20171219121750.783Z')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;

        Assert.assertTrue(DateTimeUtils.getYear(dt) == 2017);
        Assert.assertTrue(DateTimeUtils.getMonth(dt) == 12);
        Assert.assertTrue(DateTimeUtils.getDayOfMonth(dt) == 19);
        Assert.assertTrue(DateTimeUtils.getHour(dt) == 12);
        Assert.assertTrue(DateTimeUtils.getMinute(dt) == 17);
        Assert.assertTrue(DateTimeUtils.getSecond(dt) == 50);

    }

    @Test
    public void evaluateToDate() throws EvaluatorException {
        String expression = "TO_DATE('2018-02-26 14:42:04')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;

        Assert.assertTrue(DateTimeUtils.getYear(dt) == 2018);
        Assert.assertTrue(DateTimeUtils.getMonth(dt) == 02);
        Assert.assertTrue(DateTimeUtils.getDayOfMonth(dt) == 26);
        Assert.assertTrue(DateTimeUtils.getHour(dt) == 14);
        Assert.assertTrue(DateTimeUtils.getMinute(dt) == 42);
        Assert.assertTrue(DateTimeUtils.getSecond(dt) == 04);

    }

    @Test
    public void testDiffSeconds() throws EvaluatorException {
        String expression = "DATE_DIFF(TO_DATE('2018-02-26 14:42:04'), TO_DATE('2018-02-26 14:42:14'), 'SECONDS')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Long);

        Long val = (Long) evaluaged;
        Assert.assertTrue(val == 10);

    }

    @Test
    public void evaluateToDateFormated() throws EvaluatorException {
        String expression = "TO_DATE('2018-02-26 14:42:04', '#YYYY-MM-dd hh a#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;

        Assert.assertTrue(DateTimeUtils.getYear(dt) == 2018);
        Assert.assertTrue(DateTimeUtils.getMonth(dt) == 02);
        Assert.assertTrue(DateTimeUtils.getDayOfMonth(dt) == 26);
        Assert.assertTrue(DateTimeUtils.getHour(dt) == 14);

        Assert.assertTrue(DateTimeUtils.getMinute(dt) == 0);
        Assert.assertTrue(DateTimeUtils.getSecond(dt) == 0);
    }

    //

    @Test
    public void evaluateDateTimeToHourExpressionTest() throws EvaluatorException {
        String expression = "HOUR($dateTime$)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Integer);

        System.out.println(evaluaged);

        expression = "HOUR($dateTime1$)";
        evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Integer);

        System.out.println(evaluaged);
    }

    @Test
    public void evaluateDateTimeToDateHourAMPMFormatExpressionTest() throws EvaluatorException {
        String expression = "TO_DATE($dateTime$, '#YYYY-MM-dd hh ZZZ a#')";
        Object evaluated1 = evaluate(expression);

        Assert.assertTrue(evaluated1 instanceof Instant);
        Instant instant = (Instant) evaluated1;

        LocalDateTime l = LocalDateTime.ofInstant(instant, ZoneId.of("UTC")); //new LocalDateTime(instant);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh a").withZone(ZoneId.of("UTC"));

        DateFormat df = new SimpleDateFormat("hh a");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        java.sql.Date date = new java.sql.Date(instant.toEpochMilli());


        System.out.println("Instant: " + evaluated1);
        System.out.println("Formatted Instant: " + formatter.format(instant));
        System.out.println("Formatted sql.Date: " + df.format(date));


        expression = "HOUR($dateTime$)";
        evaluated1 = evaluate(expression);
        System.out.println("Hour: " + evaluated1);


        expression = "TO_DATE($dateTime1$, '#YYYY-MM-dd hh a#')";
        evaluated1 = evaluate(expression);

        Assert.assertTrue(evaluated1 instanceof Instant);
        instant = (Instant) evaluated1;

        date = new java.sql.Date(instant.toEpochMilli());

        System.out.println(evaluated1);
        System.out.println(formatter.format(instant));
        System.out.println(df.format(date));


        expression = "HOUR($dateTime1$)";
        evaluated1 = evaluate(expression);
        System.out.println("Hour1: " + evaluated1);

        /*Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant)evaluaged;
        assertDayDates(dt);*/
    }

    @Test
    public void evaluateDateToDateFormatExpressionTest() throws EvaluatorException {
        String expression = "TO_DATE($date$, '#YYYY-MM-dd#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;
        assertDayDates(dt);
    }

    @Test
    public void evaluateDateTimeStringToDateFormatExpressionTest() throws EvaluatorException {
        String expression = "TO_DATE($dateTimeString$, '#YYYY-MM-dd#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;
        assertDayDates(dt);
    }

    @Test
    public void evaluateDateTimeToDateHourFormatExpressionTest() throws EvaluatorException {
        String expression = "TO_DATE($dateTime$, '#YYYY-MM-dd hh#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;
        assertDateZeroMinutes(dt);
    }


    @Test
    public void evaluateDateTimeToDateYearFormatExpressionTest() throws EvaluatorException {
        String expression = "TO_DATE($dateTime$, '#YYYY#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;
        assertDayDates(dt);
        Assert.assertTrue(DateTimeUtils.getMonth(dt) == 1);//dt.get(ChronoField.MONTH_OF_YEAR)==1);//dt.getMonthOfYear()==1);
        Assert.assertTrue(DateTimeUtils.getDayOfMonth(dt) == 1);//dt.get(ChronoField.DAY_OF_YEAR)==1);//dt.getDayOfYear()==1);
    }

    @Test
    public void evaluateDateTimeStringToDateMonthFormatExpressionTest() throws EvaluatorException {
        String expression = "TO_DATE($dateTimeString$, '#YYYY-MM#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;
        assertDayDates(dt);
        Assert.assertTrue(DateTimeUtils.getDayOfMonth(dt) == 1);//dt.get(ChronoField.DAY_OF_MONTH)==1);//dt.getDayOfMonth()==1);
    }

    @Test
    public void evaluateTweetStringToDateMonthFormatExpressionTest() throws EvaluatorException {
        String expression = "TO_DATE($tweetCreatedAt$, '#YYYY-MM#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant) evaluaged;
        assertDayDates(dt);
        Assert.assertTrue(DateTimeUtils.getDayOfMonth(dt) == 1);//dt.get(ChronoField.DAY_OF_MONTH)==1);//dt.getDayOfMonth()==1);
    }

    @Test
    public void evaluateIfTrueFalsValueTest() throws EvaluatorException {
        String expression = "IF(!IS_NULL($child.name$), TO_INT('10'), '20')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Integer);
        Assert.assertTrue(evaluaged.equals(10));
    }

    @Test
    public void evaluateIfTrueFalsValueAsIntegerTest() throws EvaluatorException {
        String expression = "IF(TO_INT('0')-TO_INT('1'), TO_INT('10'), '20')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue(evaluaged.equals("20"));
    }

    @Test
    public void evaluateBooleanSimple() throws EvaluatorException {
        String expression = "'true'=='true'";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean) evaluaged);
    }

    @Test
    public void evaluateBooleanObjectSimple() throws EvaluatorException {
        String expression = "$child.valueTrue$=='true'";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean) evaluaged);
    }

    @Test
    public void evaluateBooleanObjectComplex() throws EvaluatorException {
        String expression = "!IS_NULL($child.valueTrue$) && $child.valueTrue$=='true'";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean) evaluaged);
    }

    @Test
    public void evaluateSimplePathExtractor() throws EvaluatorException {
        String expression = "$child.name$";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
    }

    @Test
    public void evaluateSimplePathExtractorList() throws EvaluatorException {
        String expression = "$child.list$";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof List);
    }

    @Test
    public void evaluateSimplePathExtractorLisParams() throws EvaluatorException {
        String expression = "$child.list[0]$";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue("Hello".equals(evaluaged));
    }

    @Test
    public void evaluateSimplePathExtractorLisParamsLast() throws EvaluatorException {
        String expression = "$child.list[last]$";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Map);
        //Assert.assertTrue("Hello".equals(evaluaged));
    }

    @Test
    public void evaluateSimplePathExtractorLisParamsLastChil() throws EvaluatorException {
        String expression = "$child.list[last].third.thirdlist[0]$";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue("thirdhaha".equals(evaluaged));
    }
}
