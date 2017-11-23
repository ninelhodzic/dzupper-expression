package mapperevaluator;

import base.TestBase;
import org.datazup.expression.SelectMapperEvaluator;
import org.datazup.pathextractor.PathExtractor;
import org.datazup.pathextractor.SimpleResolverHelper;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by ninel on 11/13/16.
 */
public class BasicTest  extends TestBase {

    private static SelectMapperEvaluator evaluator;
    private static SimpleResolverHelper mapListResolver = new SimpleResolverHelper();

    @BeforeClass
    public static void init(){
        evaluator = SelectMapperEvaluator.getInstance(mapListResolver);
    }

    private Object evaluateOnMap(String expression){
        return evaluator.evaluate(expression, new PathExtractor(getData(), mapListResolver));
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
