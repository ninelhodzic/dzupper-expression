package expression;

import base.TestBase;
import org.datazup.expression.SimpleObjectEvaluator;
import org.datazup.pathextractor.PathExtractor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Created by ninel on 11/13/16.
 */
public class BasicTest extends TestBase {

    private static SimpleObjectEvaluator evaluator;

    @BeforeClass
    public static void init(){
        evaluator = SimpleObjectEvaluator.getInstance();
    }

    private Object evaluateOnMap(String expression){
        return evaluator.evaluate(expression,new PathExtractor(getData()));
    }

    private Object evaluateOnListOfMaps(String expression){
        return evaluator.evaluate(expression, getListOfMaps());
    }

    private Object evaluateOnNestedListOfMaps(String expression){
        return evaluator.evaluate(expression, getNestedListOfMaps());
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


}
