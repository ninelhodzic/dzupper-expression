package mapperevaluator;

import base.TestBase;
import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.SelectMapperEvaluator;
import org.datazup.expression.context.ConcurrentExecutionContext;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.pathextractor.PathExtractor;
import org.datazup.pathextractor.SimpleResolverHelper;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by admin@datazup on 11/13/16.
 */
public class BasicTest  extends TestBase {

    private static SelectMapperEvaluator evaluator;
    private static final SimpleResolverHelper mapListResolver = new SimpleResolverHelper();
    private static final ExecutionContext executionContext = new ConcurrentExecutionContext();

    @BeforeClass
    public static void init(){
        evaluator = SelectMapperEvaluator.getInstance(executionContext, mapListResolver);
    }

    private Object evaluateOnMap(String expression){
        try {
            return evaluator.evaluate(expression, new PathExtractor(getData(), mapListResolver));
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
    public void testSelect(){
        String expression = "SELECT($name$, $child$)";
        Object res = evaluateOnMap(expression);
    }
}
