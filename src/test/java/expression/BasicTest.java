package expression;

import base.TestBase;
import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.SimpleObjectEvaluator;
import org.datazup.pathextractor.PathExtractor;
import org.datazup.pathextractor.SimpleResolverHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;


/**
 * Created by admin@datazup on 11/13/16.
 */
public class BasicTest extends TestBase {

    private static SimpleObjectEvaluator evaluator;
    private static SimpleResolverHelper mapListResolver =new SimpleResolverHelper();

    @BeforeClass
    public static void init(){
        evaluator = SimpleObjectEvaluator.getInstance(mapListResolver);
    }

    private Object evaluateOnMap(String expression){
        try {
            return evaluator.evaluate(expression,new PathExtractor(getData(), mapListResolver));
        } catch (EvaluatorException e) {
            throw new RuntimeException(e);

        }
    }

    private Object evaluateOnListOfMaps(String expression){
        try {
            return evaluator.evaluate(expression, getListOfMaps());
        } catch (EvaluatorException e) {
            throw new RuntimeException(e);

        }
    }

    private Object evaluateOnNestedListOfMaps(String expression){
        try {
            return evaluator.evaluate(expression, getNestedListOfMaps());
        } catch (EvaluatorException e) {
            throw new RuntimeException(e);

        }
    }

    @Test
    public void testPlus(){
        String expression = "2+2";

        Object res = evaluateOnMap(expression);
        Assert.assertTrue (res instanceof Number);
        Number bres = (Number)res;
        Assert.assertTrue(bres.intValue()==4);
    }

    @Test
    public void testAnd(){
        String expression = "2==2 && 1==1";
        Object res = evaluateOnMap(expression);
        Assert.assertTrue (res instanceof Boolean);
        Boolean bres = (Boolean)res;
        Assert.assertTrue(bres);
    }

    @Test
    public void testAndFalse(){
        String expression = "1==1 && 23!=23";
        Object res = evaluateOnMap(expression);
        Assert.assertTrue (res instanceof Boolean);
        Boolean bres = (Boolean)res;
        Assert.assertTrue(!bres);
    }

    @Test
    public void testLowerOrEqualTRUE(){
        String expression = "1<=1";
        Object res = evaluateOnMap(expression);
        Assert.assertTrue (res instanceof Boolean);
        Boolean bres = (Boolean)res;
        Assert.assertTrue(bres);
    }

    @Test
    public void testGreaterOrEqualTRUE(){
        String expression = "1>=1";
        Object res = evaluateOnMap(expression);
        Assert.assertTrue (res instanceof Boolean);
        Boolean bres = (Boolean)res;
        Assert.assertTrue(bres);
    }

    @Test
    public void testComplexBooleanTrue(){
        String expression = "1==1 && (10>(5+2) || (23+42)<21)";
        Object res = evaluateOnMap(expression);
        Assert.assertTrue (res instanceof Boolean);
        Boolean bres = (Boolean)res;
        Assert.assertTrue(bres);
    }

    @Test
    public void testComplexBooleanFalse(){
        String expression = "1==1 && IS_NULL($name$) || !SIZE_OF($child$)<1";
        Object res = evaluateOnMap(expression);
        Assert.assertTrue (res instanceof Boolean);
        Boolean bres = (Boolean)res;
        Assert.assertTrue(bres);
    }

    @Test
    public void testSumWithPlusStrings(){
        String expression = "'Hello'+'World'";
        Object r = evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof String);
        Assert.assertTrue(r.toString().equals("HelloWorld"));
    }

    @Test
    public void testSumWithPlusObjectStrings(){
        String expression = "$child.value$ + '-World'";
        Object r = evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof String);
        Assert.assertTrue(r.toString().equals("1-World"));
    }

    @Test
    public void testSum(){
        String expression = "5+10";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==15d);
    }

    @Test
    public void testSubtraction(){
        String expression = "5-10";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==-5d);
    }
    @Test
    public void testSubtractionObjects(){
        String expression = "$child.value$ + $child.value3$";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==-4d);
    }

    @Test
    public void testMultiply(){
        String expression = "5 * 10";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==50d);
    }

    @Test
    public void testModulo(){
        String expression = "10 % 3";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==1d);
    }

    @Test
    public void testMultiplyObjectsModulo(){
        String expression = "($child.value$ * 10) % 4";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==2d);
    }

    @Test
    public void testDivideNumberStringModulo(){
        String expression = "('10' / '5') % '2'";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==0d);
    }

    @Test
    public void testMultiplyObjects(){
        String expression = "$child.value$ * 10";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==10d);
    }

    @Test
    public void testDivide(){
        String expression = "10 / 5";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==2d);
    }
    @Test
    public void testDivideNumberString(){
        String expression = "'10' / '5'";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==2d);
    }

    @Test
    public void testDivideObject(){
        String expression = "10 / $child.value$";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==10d);
    }

    @Test
    public void testComplexEquation(){
        String expression = "10 / 2 + (3-1) * 5";
        Object r= evaluateOnMap(expression);
        Assert.assertNotNull(r);
        Assert.assertTrue(r instanceof Double);
        Assert.assertTrue(((Double)r)==15d);
    }

    @Test
    public void testDate(){
        String expression = "NOW() > STR_TO_DATE_TIMESTAMP('#2016-01-01#', '#yyyy-MM-dd#')";
        Object res = evaluateOnMap(expression);
        Assert.assertTrue(res instanceof Boolean);
        Boolean bres = (Boolean)res;
        Assert.assertTrue(bres);
    }

    @Test
    public void testDateOlx(){
        String expression = "TO_DATE(REPLACE_ALL('01.10.2017. u 00:40', ' u', ''), '#dd.MM.yyyy. HH:mm#')";
        Object res = evaluateOnMap(expression);
        Assert.assertNotNull(res);
        Assert.assertTrue(res instanceof Instant);

    }


    @Test
    public void testReplaceAllString(){
        String s = "01.10.2017. 00:40";
        String expression = "REPLACE_ALL('01.10.2017. u 00:40', 'u ', '')";
        Object res = evaluateOnMap(expression);
        Assert.assertTrue(res instanceof String);
        Assert.assertTrue(res.equals(s));

    }
}
