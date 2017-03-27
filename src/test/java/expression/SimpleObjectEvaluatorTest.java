package expression;

import base.TestBase;
import org.datazup.expression.SimpleObjectEvaluator;
import org.datazup.pathextractor.PathExtractor;
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

}
