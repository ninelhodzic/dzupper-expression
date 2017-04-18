package expression;

import base.TestBase;
import org.datazup.expression.SimpleObjectEvaluator;
import org.datazup.pathextractor.PathExtractor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by ninel on 3/15/16.
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
        PathExtractor pathExtractor = new PathExtractor(getData());
        SimpleObjectEvaluator evaluator = new SimpleObjectEvaluator();
        return  evaluator.evaluate(expression,pathExtractor);
    }
    
    @Test
    public void evaluateRegexFunctionExpressionTest(){
        String expression = "REGEX_MATCH($child.name$, '\bchild\b')";
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
    }
    
    @Test
    public void evaluateRegexExtractByGroupFunctionExpressionTest(){
        String expression = "REGEX_EXTRACT($child.name$, '#(?<=c)(.*)(?=d)#',1)";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof String);
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

    private void assertDayDates(DateTime dt){

        Assert.assertTrue(dt.getHourOfDay()==0);
        Assert.assertTrue(dt.getMinuteOfDay()==0);
        Assert.assertTrue(dt.getSecondOfDay()==0);
        assertDateZeroMinutes(dt);
    }

    private void assertDateZeroMinutes(DateTime dt){

        Assert.assertTrue(dt.getMinuteOfHour()==0);
        Assert.assertTrue(dt.getSecondOfMinute()==0);
    }

    @Test
    public void evaluateDateTimeToDateFormatExpressionTest(){
        String expression = "TO_DATE($dateTime$, '#YYYY-MM-dd#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof DateTime);

        DateTime dt = (DateTime)evaluaged;
        assertDayDates(dt);
    }

    @Test
    public void evaluateDateToDateFormatExpressionTest(){
        String expression = "TO_DATE($date$, '#YYYY-MM-dd#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof DateTime);

        DateTime dt = (DateTime)evaluaged;
        assertDayDates(dt);
    }

    @Test
    public void evaluateDateTimeStringToDateFormatExpressionTest(){
        String expression = "TO_DATE($dateTimeString$, '#YYYY-MM-dd#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof DateTime);

        DateTime dt = (DateTime)evaluaged;
        assertDayDates(dt);
    }

    @Test
    public void evaluateDateTimeToDateHourFormatExpressionTest(){
        String expression = "TO_DATE($dateTime$, '#YYYY-MM-dd hh#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof DateTime);

        DateTime dt = (DateTime)evaluaged;
        assertDateZeroMinutes(dt);
    }


    @Test
    public void evaluateDateTimeToDateYearFormatExpressionTest(){
        String expression = "TO_DATE($dateTime$, '#YYYY#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof DateTime);

        DateTime dt = (DateTime)evaluaged;
        assertDayDates(dt);
        Assert.assertTrue(dt.getMonthOfYear()==1);
        Assert.assertTrue(dt.getDayOfYear()==1);
    }

    @Test
    public void evaluateDateTimeStringToDateMonthFormatExpressionTest(){
        String expression = "TO_DATE($dateTimeString$, '#YYYY-MM#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof DateTime);

        DateTime dt = (DateTime)evaluaged;
        assertDayDates(dt);
        Assert.assertTrue(dt.getDayOfMonth()==1);
    }

    @Test
    public void evaluateTweetStringToDateMonthFormatExpressionTest(){
        String expression = "TO_DATE($tweetCreatedAt$, '#YYYY-MM#')";
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof DateTime);

        DateTime dt = (DateTime)evaluaged;
        assertDayDates(dt);
        Assert.assertTrue(dt.getDayOfMonth()==1);
    }

}
