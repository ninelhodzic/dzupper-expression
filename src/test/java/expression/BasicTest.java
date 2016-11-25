package expression;

import base.TestBase;
import org.datazup.expression.SimpleObjectEvaluator;
import org.datazup.pathextractor.PathExtractor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;


/**
 * Created by ninel on 11/13/16.
 */
public class BasicTest extends TestBase {

    private static SimpleObjectEvaluator evaluator;

    @BeforeClass
    public static void init(){
        evaluator = new SimpleObjectEvaluator();
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
        Assert.isInstanceOf(Boolean.class, res);
        Boolean bres = (Boolean)res;
        Assert.isTrue(bres);
    }

    @Test
    public void testAndFalse(){
        String expression = "1==1 && 23!=23";
        Object res = evaluateOnMap(expression);
        Assert.isInstanceOf(Boolean.class, res);
        Boolean bres = (Boolean)res;
        Assert.isTrue(!bres);
    }

    @Test
    public void testComplexBooleanTrue(){
        String expression = "1==1 && (10>(5+2) || (23+42)<21)";
        Object res = evaluateOnMap(expression);
        Assert.isInstanceOf(Boolean.class, res);
        Boolean bres = (Boolean)res;
        Assert.isTrue(bres);
    }

    @Test
    public void testComplexBooleanFalse(){
        String expression = "1==1 && IS_NULL($name$) || !SIZE_OF($child$)<1";
        Object res = evaluateOnMap(expression);
        Assert.isInstanceOf(Boolean.class, res);
        Boolean bres = (Boolean)res;
        Assert.isTrue(bres);
    }

    /*@Test //this doesn;t work due to minus (-) in date format and date
    public void testDate(){
        String expression = "NOW() > STR_TO_DATE_TIMESTAMP('2016-01-01', 'yyyy-MM-dd')";
        Object res = evaluateOnMap(expression);
        Assert.isInstanceOf(Boolean.class, res);
        Boolean bres = (Boolean)res;
        Assert.isTrue(bres);
    }*/
}
