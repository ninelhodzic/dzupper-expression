package expression;

import base.TestBase;
import org.datazup.expression.SimpleObjectEvaluator;
import org.datazup.pathextractor.PathExtractor;
import org.junit.Assert;
import org.junit.Test;

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
}
