package SelectMapperEvaluator;

import base.TestBase;
import org.datazup.expression.SelectMapperEvaluator;
import org.datazup.pathextractor.PathExtractor;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by ninel on 11/13/16.
 */
public class BasicTest  extends TestBase {

    private static SelectMapperEvaluator evaluator;

    @BeforeClass
    public static void init(){
        evaluator = new SelectMapperEvaluator();
    }

    private Object evaluateOnMap(String expression){
        return evaluator.evaluate(expression, new PathExtractor(getData()));
    }

    private Object evaluateOnListOfMaps(String expression){
        return evaluator.evaluate(expression, getListOfMaps());
    }

    private Object evaluateOnNestedListOfMaps(String expression){
        return evaluator.evaluate(expression, getNestedListOfMaps());
    }


    @Test
    public void testSelect(){
        String expression = "SELECT($name$, $child$)";
        Object res = evaluateOnMap(expression);
    }
}
