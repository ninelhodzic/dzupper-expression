package expression;

import base.TestBase;
import org.datazup.expression.SimpleObjectEvaluator;
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

    @Test
    public void isEmptyExpressionEvaluatedAsTrueTest(){
        String expression = "";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean)evaluaged;
        Assert.assertTrue(b);
    }

    @Test
    public void evaluateSizeOfListExpressionTest(){
        String expression = "SIZE_OF($list$)==1";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean)evaluaged;
        Assert.assertTrue(b);
    }

    @Test
    public void evaluateNullFunctionMissingExpressionTest(){
        String expression = "IS_NULL(child.name.nemaovo)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean)evaluaged;
        Assert.assertTrue(b);
    }

    @Test
    public void evaluateNullFunctionExpressionTest(){
        String expression = "IS_NULL(child.name1)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean)evaluaged;
        Assert.assertTrue(b);
    }

    @Test
    public void evaluateNotNullFunctionExpressionTest(){
        String expression = "!IS_NULL(child.name)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean)evaluaged;
        Assert.assertTrue(b);
    }

    public void resolveComplexExpressionTest(){
        String expression = "($child.name$ == 'child' && $number$==5.30 && 6.30-1 == $number$) && (NOW() < NOW() || NOW()==NOW())";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
    }

    public Object evaluate(String expression){
        SimpleResolverHelper mapListResolver = new SimpleResolverHelper();
        PathExtractor pathExtractor = new PathExtractor(getData(),mapListResolver);
        SimpleObjectEvaluator evaluator = SimpleObjectEvaluator.getInstance(mapListResolver); //new SimpleObjectEvaluator();
        return  evaluator.evaluate(expression,pathExtractor);
    }
    
    @Test
    public void evaluateRegexFunctionExpressionTest(){
        String expression = "REGEX_MATCH($child.name$, '\\bchild\\b')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean)evaluaged;
        Assert.assertTrue(b);
    }

    @Test
    public void evaluateRegexFunctionExpressionStringTest(){
        String expression = "REGEX_MATCH('this is [ERROR] of something', '#\\[ERROR\\]#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Boolean b = (Boolean)evaluaged;
        Assert.assertTrue(b);
    }
    
    @Test
    public void evaluateRegexExtractFunctionExpressionTest(){
        String expression = "REGEX_EXTRACT($child.name$, '#(?<=c)(.*)(?=d)#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue(evaluaged.equals("hil"));
    }

    @Test
    public void evaluateRegexExtractHtmlTest(){

         // #<a[^>]*?>(.*?)<\/a>#
        // #<a.*>(.*?)<#
        String expression = "REGEX_EXTRACT($child.html$, '#<a[^>]*?>(.*?)</a>#', 1)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue(evaluaged.equals("Twitter for iPhone"));
    }

    @Test
    public void evaluateRegexMatchHtmlTest(){

        // #<a[^>]*?>(.*?)<\/a>#
        // #<a.*>(.*?)<#
        String expression = "REGEX_MATCH($child.html$, '#<a.*#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue(evaluaged.equals(Boolean.TRUE));
    }
    
    @Test
    public void evaluateRegexExtractByGroupFunctionExpressionTest(){
        String expression = "REGEX_EXTRACT($child.name$, '#(?<=c)(.*)(?=d)#',1)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
    }

    @Test
    public void evaluateRegexExtractNumber(){
        String expression = "TO_INT(REGEX_EXTRACT($fieldPrice$, '#\\d+#',1))";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Number);
        Assert.assertTrue(evaluaged.equals(20));
    }

    @Test
    public void evaluateRegexExtractNumberWithSeparator(){
        String expression = "TO_DOUBLE(REGEX_EXTRACT($fieldPrice1$, '#\\d+,.\\d+#'))";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Number);
        Assert.assertTrue(evaluaged.equals(20.15));
    }

    @Test
    public void evaluateRegexExtractNumberWithSeparator2(){
        String expression = "TO_DOUBLE(REGEX_EXTRACT($fieldPrice2$, '#\\d+(,|.)\\d+#'))";
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
    public void evaluateExtractorFnTest(){
        String expression = "EXTRACT($text$, '#this,longer,   test, has purposes#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof List);
        List l = (List)evaluaged;
        Assert.assertTrue(l.size()==4);
        Assert.assertTrue("this".equals(l.get(0)));
        Assert.assertTrue("longer".equals(l.get(1)));
        Assert.assertTrue("test".equals(l.get(2)));
        Assert.assertTrue("has purposes".equals(l.get(3)));
    }

    @Test
    public void evaluateContainsStringSimpleTest(){

        String expression = "CONTAINS($text$, 'longer text')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean)evaluaged);
    }

    @Test
    public void evaluateContainsStringSimpleCaseInsensitiveTest(){

        String expression = "CONTAINS($text$,'ALL_INSENSITIVE', 'longer TEXT')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean)evaluaged);
    }

    @Test
    public void evaluateContainsStringSimpleCaseInsensitiveMoreTest(){

        String expression = "CONTAINS($text$,'ALL_INSENSITIVE', 'longer TEXT', 'Test', 'tesTing purposes')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean)evaluaged);
    }

    @Test
    public void evaluateContainsStringSimpleCaseInsensitiveFalseTest(){

        String expression = "CONTAINS($text$,'ALL', 'longer TEXT')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertFalse((Boolean)evaluaged);
    }

    @Test
    public void evaluateContainsStringSimpleNotMatchTest(){

        String expression = "CONTAINS($text$, 'longer text 1')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertFalse((Boolean)evaluaged);
    }

    @Test
    public void evaluateContainsListSimpleTest(){

        String expression = "CONTAINS($child.list$, 'Hello')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean)evaluaged);
    }

    @Test
    public void evaluateContainsListSimpleFalseTest(){

        String expression = "CONTAINS($child.list$, 'Hello 1')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertFalse((Boolean)evaluaged);
    }

    @Test
    public void evaluateContainsListAnyTest(){

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "CONTAINS($child.list$,'ANY', 'Hello', 'ahoj')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean)evaluaged);
    }

    @Test
    public void evaluateContainMapKeyAllTest(){

        // Note: Insensitive doesn't workf ro lists or maps
        String expression = "CONTAINS($child$,'ALL', 'list', 'html')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean)evaluaged);
    }


    @Test
    public void evaluateSimpleDateConversionBasedOnFormatTest(){
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

    private void assertDayDates(Instant dt){

        //LocalDateTime.ofInstant(dt, ZoneOffset.UTC).getHour();



        Assert.assertTrue(DateTimeUtils.getHour(dt)==0);//dt.get(ChronoField.HOUR_OF_DAY)==0);
        Assert.assertTrue(DateTimeUtils.getMinute(dt)==0);//dt.get(ChronoField.MINUTE_OF_DAY)==0);//dt.getMinuteOfDay()==0);
        Assert.assertTrue(DateTimeUtils.getSecond(dt)==0);//dt.get(ChronoField.SECOND_OF_DAY)==0);//dt.getSecondOfDay()==0);
        assertDateZeroMinutes(dt);
    }

    private void assertDateZeroMinutes(Instant dt){

        Assert.assertTrue(DateTimeUtils.getMinute(dt)==0);//dt.get(ChronoField.MINUTE_OF_HOUR)==0);//dt.getMinuteOfHour()==0);
        Assert.assertTrue(DateTimeUtils.getSecond(dt)==0);//dt.get(ChronoField.SECOND_OF_MINUTE)==0);//dt.getSecondOfMinute()==0);
    }

    @Test
    public void evaluateDateTimeToDateFormatExpressionTest(){
        String expression = "TO_DATE($dateTime$, '#YYYY-MM-dd#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant)evaluaged;
        assertDayDates(dt);
    }

    @Test
    public void evaluateDateTimeToHourExpressionTest(){
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
    public void evaluateDateTimeToDateHourAMPMFormatExpressionTest(){
        String expression = "TO_DATE($dateTime$, '#YYYY-MM-dd hh ZZZ a#')";
        Object evaluated1 = evaluate(expression);

        Assert.assertTrue(evaluated1 instanceof Instant);
        Instant instant = (Instant)evaluated1;

        LocalDateTime l = LocalDateTime.ofInstant(instant, ZoneId.of("UTC")); //new LocalDateTime(instant);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh a").withZone(ZoneId.of("UTC"));

        DateFormat df = new SimpleDateFormat("hh a");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        java.sql.Date date = new java.sql.Date(instant.toEpochMilli());


        System.out.println("Instant: "+evaluated1);
        System.out.println("Formatted Instant: "+formatter.format(instant));
        System.out.println("Formatted sql.Date: "+df.format(date));


        expression = "HOUR($dateTime$)";
        evaluated1 = evaluate(expression);
        System.out.println("Hour: "+evaluated1);


        expression = "TO_DATE($dateTime1$, '#YYYY-MM-dd hh a#')";
        evaluated1 = evaluate(expression);

        Assert.assertTrue(evaluated1 instanceof Instant);
        instant = (Instant)evaluated1;

        date = new java.sql.Date(instant.toEpochMilli());

        System.out.println(evaluated1);
        System.out.println(formatter.format(instant));
        System.out.println(df.format(date));


        expression = "HOUR($dateTime1$)";
        evaluated1 = evaluate(expression);
        System.out.println("Hour1: "+evaluated1);

        /*Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant)evaluaged;
        assertDayDates(dt);*/
    }

    @Test
    public void evaluateDateToDateFormatExpressionTest(){
        String expression = "TO_DATE($date$, '#YYYY-MM-dd#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant)evaluaged;
        assertDayDates(dt);
    }

    @Test
    public void evaluateDateTimeStringToDateFormatExpressionTest(){
        String expression = "TO_DATE($dateTimeString$, '#YYYY-MM-dd#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant)evaluaged;
        assertDayDates(dt);
    }

    @Test
    public void evaluateDateTimeToDateHourFormatExpressionTest(){
        String expression = "TO_DATE($dateTime$, '#YYYY-MM-dd hh#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant)evaluaged;
        assertDateZeroMinutes(dt);
    }


    @Test
    public void evaluateDateTimeToDateYearFormatExpressionTest(){
        String expression = "TO_DATE($dateTime$, '#YYYY#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant)evaluaged;
        assertDayDates(dt);
        Assert.assertTrue(DateTimeUtils.getMonth(dt)==1);//dt.get(ChronoField.MONTH_OF_YEAR)==1);//dt.getMonthOfYear()==1);
        Assert.assertTrue(DateTimeUtils.getDayOfMonth(dt)==1);//dt.get(ChronoField.DAY_OF_YEAR)==1);//dt.getDayOfYear()==1);
    }

    @Test
    public void evaluateDateTimeStringToDateMonthFormatExpressionTest(){
        String expression = "TO_DATE($dateTimeString$, '#YYYY-MM#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant)evaluaged;
        assertDayDates(dt);
        Assert.assertTrue(DateTimeUtils.getDayOfMonth(dt)==1);//dt.get(ChronoField.DAY_OF_MONTH)==1);//dt.getDayOfMonth()==1);
    }

    @Test
    public void evaluateTweetStringToDateMonthFormatExpressionTest(){
        String expression = "TO_DATE($tweetCreatedAt$, '#YYYY-MM#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Instant);

        Instant dt = (Instant)evaluaged;
        assertDayDates(dt);
        Assert.assertTrue(DateTimeUtils.getDayOfMonth(dt)==1);//dt.get(ChronoField.DAY_OF_MONTH)==1);//dt.getDayOfMonth()==1);
    }

    @Test
    public void evaluateIfTrueFalsValueTest(){
        String expression = "IF(!IS_NULL($child.name$), TO_INT('10'), '20')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Integer);
        Assert.assertTrue(evaluaged.equals(10));
    }

    @Test
    public void evaluateIfTrueFalsValueAsIntegerTest(){
        String expression = "IF(TO_INT('0')-TO_INT('1'), TO_INT('10'), '20')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue(evaluaged.equals("20"));
    }

    @Test
    public void evaluateBooleanSimple(){
        String expression = "'true'=='true'";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean)evaluaged);
    }
    @Test
    public void evaluateBooleanObjectSimple(){
        String expression = "$child.valueTrue$=='true'";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean)evaluaged);
    }

    @Test
    public void evaluateBooleanObjectComplex(){
        String expression = "!IS_NULL($child.valueTrue$) && $child.valueTrue$=='true'";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Boolean);
        Assert.assertTrue((Boolean)evaluaged);
    }

    @Test
    public void evaluateSimplePathExtractor(){
        String expression = "$child.name$";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
    }

    @Test
    public void evaluateSimplePathExtractorList(){
        String expression = "$child.list$";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof List);
    }
    @Test
    public void evaluateSimplePathExtractorLisParams(){
        String expression = "$child.list[0]$";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue("Hello".equals(evaluaged));
    }

    @Test
    public void evaluateSimplePathExtractorLisParamsLast(){
        String expression = "$child.list[last]$";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Map);
        //Assert.assertTrue("Hello".equals(evaluaged));
    }

    @Test
    public void evaluateSimplePathExtractorLisParamsLastChil(){
        String expression = "$child.list[last].third.thirdlist[0]$";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
        Assert.assertTrue("thirdhaha".equals(evaluaged));
    }
}
