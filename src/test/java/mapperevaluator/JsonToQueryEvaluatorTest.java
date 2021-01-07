package mapperevaluator;

import base.TestBase;
import org.datazup.exceptions.EvaluatorException;
import org.datazup.expression.SelectMapperEvaluator;
import org.datazup.expression.context.ConcurrentExecutionContext;
import org.datazup.expression.context.ContextWrapper;
import org.datazup.expression.context.ExecutionContext;
import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.pathextractor.PathExtractor;
import org.datazup.pathextractor.SimpleResolverHelper;
import org.datazup.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class JsonToQueryEvaluatorTest extends TestBase {

    private static AbstractResolverHelper mapListResolver = new SimpleResolverHelper();
    private static ExecutionContext executionContext = new ConcurrentExecutionContext();
    private static SelectMapperEvaluator evaluator = SelectMapperEvaluator.getInstance(executionContext,mapListResolver);

    @Test
    public void jsonGroupByTest() throws EvaluatorException {
        String json = "{\n" +
                "        \"fields\": [\n" +
                "            {\"name\":\"*\",\"func\":\"count\"}\n" +
                "        ],\n" +
                "        \"groupBy\": [\n" +
                "            {\"name\":\"createdAt\",\"func\":\"year\"}\n" +
                "        ],\n" +
                "        \"where\": {\n" +
                "            \"AND\": [\n" +
                "                {\"name\":\"createdAt\",\"func\":\"GTE\",\"value\":\"2020-12-27\"},\n" +
                "                {\"name\":\"enabled\",\"func\":\"=\",\"value\":true},\n" +
                "                {\"OR\": [{\"name\":\"tenantName\", \"func\":\"=\",\"value\":\"dataZup\"}]},\n" +
                "                {\"OR\": [{\"name\":\"applicationTenantName\",\"func\":\"=\",\"value\":\"ahsalini\"}]}\n" +
                "            ]\n" +
                "        },\n" +
                "        \"having\":{\n" +
                "            \"AND\": [\n" +
                "                {\"name\":\"createdAt\",\"func\":\">\",\"value\":\"2020-12-27\"}\n" +
                "            ]\n" +
                "        },\n" +
                "        \"orderBy\": [ {\"name\":\"createdAt\", \"value\":\"DESC\"}] "+
                "    }";
        Map<String, Object> data = getData();
        data.put("jsonString", json);
        String expression = "TO_DB_QUERY('MONGO_DB', MAP($jsonString$))";
        evaluate(expression, data);
    }

    @Test
    public void simpleFindTest() throws EvaluatorException {
        String json = "\n" +
                "{\n" +
                "        \"fields\": [\n" +
                "            {\"name\":\"tenantName\",\"func\":\"count\"}\n" +
                "        ],\n" +
                "    \"where\":{\n" +
                "        \"AND\":[{\"name\":\"tenantName\",\"func\":\"=\",\"value\":\"dataZup\"}]\n" +
                "    }\n" +
                "}\n";
        String expression = "TO_DB_QUERY(MAP($jsonString$))";
        Map<String, Object> data = getData();
        data.put("jsonString", json);
        evaluate(expression, data);
    }

    @Test
    public void complexFindTest() throws EvaluatorException {
        String json = "\n" +
                "{\n" +
                "    \"fields\":\"*\",\n" +
                "        \"where\": {\n" +
                "            \"AND\": [\n" +
                "                {\"name\":\"createdAt\",\"func\":\"GTE\",\"value\":\"2020-12-27\"},\n" +
                "                {\"OR\": [{\"name\":\"tenantName\", \"func\":\"=\",\"value\":\"dataZup\"}]},\n" +
                "                {\"OR\": [{\"name\":\"applicationTenantName\",\"func\":\"=\",\"value\":\"ahsalini\"}]}\n" +
                "            ]\n" +
                "        }\n" +
                "}\n";
        String expression = "TO_DB_QUERY(MAP($jsonString$))";
        Map<String, Object> data = getData();
        data.put("jsonString", json);
        evaluate(expression, data);
    }

    @Test
    public void     simpleOrderByTest() throws EvaluatorException {
        String json = "\n" +
                "{\n" +
                "    \"fields\":\"*\",\n" +
                "\"orderBy\":[\n" +
                "            {\"name\":\"createdAt\", \"value\":-1},\n" +
                "            {\"name\":\"updatedAt\", \"value\":1, \"func\":\"hour\"}\n" +
                "        ]" +
                "}\n";
        String expression = "TO_DB_QUERY(MAP($jsonString$))";
        Map<String, Object> data = getData();
        data.put("jsonString", json);
        evaluate(expression, data);
    }

    @Test
    public void simpleGroupByTest() throws EvaluatorException {
        String json = "{ \"fields\": [\n" +
                "            {\"name\":\"*\",\"func\":\"count\"}\n" +
                "        ],\n" +
                "        \"groupBy\": [\n" +
                "            {\"name\":\"createdAt\",\"func\":\"year\"}\n" +
                "        ]}";
        String expression = "TO_DB_QUERY(MAP($jsonString$))";
        Map<String, Object> data = getData();
        data.put("jsonString", json);
        evaluate(expression, data);
    }

    private void evaluate(String expression, Map data) throws EvaluatorException {
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        Object evaluaged = evaluate(expression,  pathExtractor);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Map);
        Map<String,Object> res = (Map)evaluaged;
        Assert.assertTrue(!res.isEmpty());
        System.out.println(JsonUtils.getJsonFromObject(res));
    }

    private Object evaluate(String expression, PathExtractor pathExtractor) throws EvaluatorException {
        ContextWrapper ev = evaluator.evaluate(expression, pathExtractor);
        return ev.get();
    }
}
