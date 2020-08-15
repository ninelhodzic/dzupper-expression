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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by admin@datazup on 3/21/16.
 */
public class SelectMapperEvaluatorTest extends TestBase {

    private static AbstractResolverHelper mapListResolver = new SimpleResolverHelper();
    private static ExecutionContext executionContext = new ConcurrentExecutionContext();
    private static SelectMapperEvaluator evaluator = SelectMapperEvaluator.getInstance(executionContext,mapListResolver);

        /*
    @Test
    public void evaluateSimpleGroupByPropertyAsMapDefinedTest()throws EvaluatorException{
        String expression = "GROUP_BY($listOfMaps$, MAP(FIELD('property','type')))";
        // should be: {type1:[{}...], type2:[{}...]}
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof Map);
    }

    @Test
    public void evaluateSimpleGroupByPropertyAsMapDefinedChildrenKeyTest()throws EvaluatorException{
        String expression = "GROUP_BY($listOfMaps$, MAP(FIELD('property','type'),FIELD('childrenKey', 'children')))";
        // should be: [{type:'type1, children:[]}, {type:'type2, children:[]}]
        Object evaluaged = evaluate(expression);
        Assert.assertNotNull(evaluaged);
        Assert.assertTrue(evaluaged instanceof List);
    }*/

    @Test
    public void evaluateSimpleGroupByPropertyTest()throws EvaluatorException{
        String expression = "GROUP_BY($listOfMaps$, 'type')";
        // should be: {type1:[{}...], type2:[{}...]}
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        Object evaluated = evaluate(expression, pathExtractor);
        Assert.assertNotNull(evaluated);
        Assert.assertTrue(evaluated instanceof Map);
        Map result = (Map)evaluated;
        Assert.assertTrue(result.size()==2);

    }

    @Test
    public void evaluateSimpleGroupByPropertyAsMapDefinedChildrenKeyCustomPropertyNameTest()throws EvaluatorException{
        String expression = "GROUP_BY($listOfMaps$, \n" +
                "FIELD('properties',\n" +
                "   LIST(\n" +
                "       MAP(\n" +
                "           FIELD('property', 'type'),\n" +
                "              FIELD('propertyNewName', 'name')\n" +
                "          ),\n" +
                "       MAP(\n" +
                "           FIELD('property', 'sourceType'),\n" +
                "           FIELD('propertyNewName', 'category')\n" +
                "       )\n" +
                "      )\n" +
                "   )\n" +
                "   FIELD('childrenKey', 'children')\n" +
                ")";

        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        Object evaluated = evaluate(expression, pathExtractor);
        Assert.assertNotNull(evaluated);
        Assert.assertTrue(evaluated instanceof List);
    }


    @Test
    public void isStepRuns() throws EvaluatorException {
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        Object evaluated = evaluate("STEP($numbers$, '#ADD($_current$, 3)#', '#ADD($_current$, 3)#')", pathExtractor);
        Assert.assertNotNull(evaluated);
        Assert.assertTrue(evaluated instanceof List);
        List m = (List) evaluated;
        Assert.assertTrue(m.size()==5);
    }

    @Test
    public void isStepSubStepRuns() throws EvaluatorException {
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        Object evaluated = evaluate("STEP($numbers$, '#STEP($_current$, '#ADD($_current$, 3)#','#ADD($_current$, 3)#' )#')", pathExtractor);
        Assert.assertNotNull(evaluated);
        Assert.assertTrue(evaluated instanceof List);
        List m = (List) evaluated;
        Assert.assertTrue(m.size()==5);
    }

    @Test
    public void isSumRuns() throws EvaluatorException {
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        Object evaluated = evaluate("SUM($numbers$)", pathExtractor);
        Assert.assertNotNull(evaluated);
        Assert.assertTrue(evaluated instanceof Number);
        Number m = (Number) evaluated;
        Assert.assertTrue(m.doubleValue()== 11d);
    }

    @Test
    public void isFilterFieldsRuns() throws EvaluatorException {

        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        Object evaluated = evaluate("EXCLUDE_FIELDS($child$, $text$)", pathExtractor);
        Assert.assertNotNull(evaluated);
        Assert.assertTrue(evaluated instanceof Map);
        Map m = (Map) evaluated;
        Assert.assertTrue(!m.containsKey("child"));
        Assert.assertTrue(!m.containsKey("text"));
    }

    @Test
    public void isRemapperRightComplexRuns() throws EvaluatorException {
        String expression = "REMAP(THIS(), '#{ \"$child.value$\": \"noviValue\"}#')";
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        Object evaluated = evaluate(expression, pathExtractor);
        Assert.assertNotNull(evaluated);
        Assert.assertTrue(evaluated instanceof Map);
        Map m = (Map) evaluated;
        Assert.assertFalse(m.containsKey("tz"));
        Assert.assertTrue(m.containsKey("noviValue"));
        Assert.assertTrue(m.get("noviValue").equals(1));
        Assert.assertTrue(m.keySet().size()==1);
    }

    @Test
    public void isForEachRuns() throws EvaluatorException {
        String expression = "FOREACH($child.list$, '#IF(IS_OF_TYPE($_current$, 'HashMap'), $_current$, $_current$+\' \'+ $_index$)#')";
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        Object evaluated = evaluate(expression, pathExtractor);
        Assert.assertNotNull(evaluated);
        Assert.assertTrue(evaluated instanceof List);
        List list = (List) evaluated;
        Assert.assertTrue(list.size()==4);
        Assert.assertTrue(list.get(0).equals("Hello 0"));
        Assert.assertTrue(list.get(1).equals("Hi 1"));

    }

    @Test
    public void isThisExpressionRuns() throws EvaluatorException{
        String expression = "THIS()";
        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put("dateString", "nesto nebitno");

        PathExtractor pathExtractor = new PathExtractor(objectMap, new SimpleResolverHelper());

        Object evaluated = evaluate(expression, pathExtractor);
        Assert.assertNotNull(evaluated);
        Assert.assertTrue(evaluated instanceof Map);
        Map m = (Map) evaluated;
        Assert.assertTrue(m.get("dateString").equals("nesto nebitno"));
    }

    @Test
    public void isPatternMatch() {
        String expression = "SELECT";
        boolean match = false;
        if (Pattern.matches("[a-zA-Z0-9]+", expression)) {
            match = true;
        }
        Assert.assertTrue(match);

        match = false;
        expression = "SELECT(";
        if (Pattern.matches("[a-zA-Z0-9]+", expression)) {
            match = true;
        }
        Assert.assertTrue(!match);

    }

    @Test
    public void isEvaluatingComplexStringAsString() throws EvaluatorException{

        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put("dateString", "nesto nebitno");

        //SelectMapperEvaluator evaluator = new SelectMapperEvaluator();
        PathExtractor pathExtractor = new PathExtractor(objectMap, mapListResolver);

        Object stringValue = evaluate("'SELECT VALUES(jpa,dsd)'", pathExtractor);
        Assert.assertNotNull(stringValue);

        Assert.assertTrue(stringValue instanceof String);
    }

    @Test
    public void isParsingDateFormatFunction() throws EvaluatorException{
        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put("dateString", "Wed May 21 00:00:00 EDT 2008");

        // SelectMapperEvaluator evaluator = new SelectMapperEvaluator();
        PathExtractor pathExtractor = new PathExtractor(objectMap, mapListResolver);

        // NOTE: this is working as there is no - (minus) in these strings
        Object datetimeRes = evaluate("STR_TO_DATE_TIMESTAMP($dateString$, 'EEE MMM d H:m:s z Y')", pathExtractor);
        Assert.assertNotNull(datetimeRes);
        Assert.assertTrue(datetimeRes instanceof Long);

    }

    @Test
    public void isParsingNowDateTimestampFunction() throws EvaluatorException{
        Map<String, Object> objectMap = new HashMap<String, Object>();


        //  SelectMapperEvaluator evaluator = new SelectMapperEvaluator();
        PathExtractor pathExtractor = new PathExtractor(objectMap, mapListResolver);

        // NOTE: this is working as there is no - (minus) in these strings
        Object datetimeRes = evaluate("NOW()", pathExtractor);
        Assert.assertNotNull(datetimeRes);
        Assert.assertTrue(datetimeRes instanceof Long);

    }

    @Test
    public void isFieldValueSumarized() throws EvaluatorException{
        Map<String, Object> objectMap = new HashMap<String, Object>();
        PathExtractor pathExtractor = new PathExtractor(objectMap, mapListResolver);

        // NOTE: this is working as there is no - (minus) in these strings
        Object datetimeRes = evaluate("FIELD('key', 2+3)", pathExtractor);
        Assert.assertNotNull(datetimeRes);

    }

    @Test
    public void isMovingAndRemovingAndPuttingTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        // SelectMapperEvaluator evaluator = new SelectMapperEvaluator();

        Object o = evaluate("SIZE_OF(child.list)", pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Integer);
        Assert.assertTrue(((Integer) o) == 4);

        o = evaluate("REMOVE(child.list[last])", pathExtractor);
        Assert.assertNotNull(o);

        o = evaluate("SIZE_OF(child.list)==3", pathExtractor);
        Assert.assertTrue(o instanceof Boolean);

        Boolean b = (Boolean) o;
        Assert.assertTrue(b);
    }

    private Object evaluate(String expression, PathExtractor pathExtractor) throws EvaluatorException {
        ContextWrapper ev = evaluator.evaluate(expression, pathExtractor);
        return ev.get();
    }

    @Test
    public void isTypeOfCorrectTest() throws EvaluatorException {
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        // SelectMapperEvaluator evaluator = new SelectMapperEvaluator();

        Object o = evaluate("TYPE_OF($list$)", pathExtractor);
        Assert.assertTrue(o instanceof String);
        Assert.assertTrue(o.equals("List"));

        o = evaluate("TYPE_OF($text$)", pathExtractor);
        Assert.assertTrue(o instanceof String);
        Assert.assertTrue(o.equals("String"));

        o = evaluate("TYPE_OF($child$)", pathExtractor);
        Assert.assertTrue(o instanceof String);
        Assert.assertTrue(o.equals("Map"));

        o = evaluate("TYPE_OF($tz$)", pathExtractor);
        Assert.assertTrue(o instanceof String);
        Assert.assertTrue(o.equals("Integer"));
    }

    @Test
    public void isOfTypeCorrectTest() throws EvaluatorException {
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        // SelectMapperEvaluator evaluator = new SelectMapperEvaluator();

        Object o = evaluate("IS_OF_TYPE($list$, 'List')", pathExtractor);
        Assert.assertTrue(o instanceof Boolean);
        Assert.assertTrue((Boolean)o);

        o = evaluate("IS_OF_TYPE($text$, 'String')", pathExtractor);
        Assert.assertTrue(o instanceof Boolean);
        Assert.assertTrue((Boolean)o);

        o = evaluate("IS_OF_TYPE($child$, 'Map')", pathExtractor);
        Assert.assertTrue(o instanceof Boolean);
        Assert.assertTrue((Boolean)o);

        o = evaluate("IS_OF_TYPE($child$, 'HashMap')", pathExtractor);
        Assert.assertTrue(o instanceof Boolean);
        Assert.assertTrue((Boolean)o);

        o = evaluate("IS_OF_TYPE($tz$,'Integer')", pathExtractor);
        Assert.assertTrue(o instanceof Boolean);
        Assert.assertTrue((Boolean)o);

        o = evaluate("IS_OF_TYPE($tz$,'Number')", pathExtractor);
        Assert.assertTrue(o instanceof Boolean);
        Assert.assertTrue((Boolean)o);

    }

    @Test
    public void isSelectingSimpleItemsTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        // SelectMapperEvaluator evaluator = new SelectMapperEvaluator();

        Object o = evaluate("SELECT(1,2,3)", pathExtractor);
        Assert.assertNotNull(o);
    }


    @Test
    public void isSelectingItemsFromMapTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        //  SelectMapperEvaluator evaluator = new SelectMapperEvaluator();

        Object o = evaluate("SELECT($child.list$, $list$)", pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof Map);

        Map<String, Object> objectMap = (Map) o;
        Assert.assertNotNull(objectMap.get("childList"));
        Assert.assertNotNull(objectMap.get("list"));
    }

    @Test
    public void isSelectingItemsFromMapWhereListLastTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        // SelectMapperEvaluator evaluator = new SelectMapperEvaluator();

        Object o = evaluate("SELECT($child.list[last]$, $list$)", pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof Map);

        Map<String, Object> objectMap = (Map) o;
        Assert.assertNotNull(objectMap.get("childList[last]"));
        Assert.assertNotNull(objectMap.get("list"));
    }

    @Test
    public void isSelectingItemsFromMapWhereListIndex0Test() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        // SelectMapperEvaluator evaluator = new SelectMapperEvaluator();

        Object o = evaluate("SELECT($child.list[0]$, $list$)", pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof Map);

        Map<String, Object> objectMap = (Map) o;
        Assert.assertNotNull(objectMap.get("childList[0]"));
        Assert.assertNotNull(objectMap.get("list"));
    }

    @Test
    public void isSelectingItemsFromMapWhereListIndex0TestBenchmark() throws EvaluatorException{
        Map<String, Object> data = getData();

        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        // SelectMapperEvaluator evaluator = SelectMapperEvaluator.getInstance();

        long start = System.currentTimeMillis();
        int num = 1000;
        for (int i = 0; i < num; i++) {
            evaluate("SELECT($child.list[0]$, $list$)", pathExtractor);
            //   Assert.assertNotNull(compiled);
        }
        long end = System.currentTimeMillis();
        System.out.println("Num: " + num + " executed in: " + (end - start) + " ms, average: " + ((end - start) / num) + " ms");

    }


    @Test
    public void isUnionListItemsFromMapTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        //  SelectMapperEvaluator evaluator = new SelectMapperEvaluator();

        Object o = evaluate("UNION($child.list$, $list$)", pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof List);

        List l = (List) o;
        Assert.assertTrue(l.size() == 5);
    }

    @Test
    public void isUnioMapItemsFromMapTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        //   SelectMapperEvaluator evaluator = new SelectMapperEvaluator();

        Object o = evaluate("UNION($child$, $list$)", pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof List);

        List l = (List) o;
        Assert.assertTrue(l.size() == 2);
    }

    @Test
    public void isKeysItemsFromMapTest()throws EvaluatorException {
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        //   SelectMapperEvaluator evaluator = SelectMapperEvaluator.getInstance();

        Object o = evaluate("KEYS($child$)", pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof List);

        List l = (List) o;
        Assert.assertTrue(l.size() == 8);

    }

    @Test
    public void isValuesItemsFromMapTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        //   SelectMapperEvaluator evaluator = SelectMapperEvaluator.getInstance();

        Object o = evaluate("VALUES($child$)", pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof List);

        List l = (List) o;
        Assert.assertTrue(l.size() == 8);

    }

    @Test
    public void isUnionKeyValuesItemsFromMapTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        //  SelectMapperEvaluator evaluator = SelectMapperEvaluator.getInstance();

        Object o = evaluate("UNION(KEYS($child$), VALUES($child$))", pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof List);

        List l = (List) o;
        Assert.assertTrue(l.size() == 16);

    }

    @Test
    public void isToMapTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        //  SelectMapperEvaluator evaluator = SelectMapperEvaluator.getInstance();

        Object o = evaluate("MAP(FIELD('firstChildListItem', $child.list[0]$), FIELD('list', $list$))", pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof Map);

        Map<String, Object> objectMap = (Map) o;
        Assert.assertNotNull(objectMap.get("firstChildListItem"));
        Assert.assertNotNull(objectMap.get("list"));
    }

    @Test
    public void isToMapTestNegative() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        //  SelectMapperEvaluator evaluator = SelectMapperEvaluator.getInstance();

        String expression = "MAP(FIELD('upsertNumber', 0), FIELD('uniqueKey', 'rss_news_Es_Index_UpsertNumber_new'), FIELD('upsertValue', TO_INT('-500')))";
        Object o = evaluate(expression, pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof Map);

        Map<String, Object> objectMap = (Map) o;
        Assert.assertNotNull(objectMap.get("upsertValue"));
    }

    @Test
    public void simpleListTest()throws EvaluatorException {
        String expression = "LIST('item1', 2, 'item2,', 23.43)";
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof List);

        List<Object> objectList = (List) o;
        Assert.assertTrue(objectList.size() == 4);
    }

    @Test
    public void complexListTest() throws EvaluatorException{
        String expression = "MAP(FIELD('locations',LIST('Seoul','Busan','Daegu','Incheon','Gwangju','Daejeon','Ulsan','Gyeonggido','Gangwondo','N.Chungcheongdo','S.Chungcheongdo','N.Jeonrado','S.Jeonrado','N.Gyeongsangdo','S.Gyeongsangdo','Jejudo')))";

        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Map);
        Map map = (Map) o;

        List l = (List) map.get("locations");
        Assert.assertNotNull(l);
        Assert.assertTrue(l.get(0).equals("Seoul"));
    }

    @Test
    public void isToListTest()throws EvaluatorException {
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        //  SelectMapperEvaluator evaluator = SelectMapperEvaluator.getInstance();

        Object o = evaluate("LIST(FIELD('firstChildListItem', $child.list[0]$), FIELD('list', $list$))", pathExtractor);
        Assert.assertNotNull(o);

        Assert.assertTrue(o instanceof List);

        List<Object> objectList = (List) o;

        Assert.assertTrue(objectList.size() == 2);
        Assert.assertNotNull(objectList.get(0));
        Assert.assertNotNull(objectList.get(1));

        Object firstObject = objectList.get(0);
        Assert.assertTrue(firstObject instanceof Map);

        Object secondObject = objectList.get(1);
        Assert.assertTrue(secondObject instanceof Map);

        Assert.assertNotNull(((Map) firstObject).get("firstChildListItem"));
        Assert.assertNotNull(((Map) secondObject).get("list"));
    }

    @Test
    public void isSetSimpleStringTest() throws EvaluatorException{
        String expression = "SORTED_SET('FIELD_STRING','Seoul','Busan','Daegu','Incheon','Gwangju','Daejeon','Ulsan','Gyeonggido','Gangwondo','N.Chungcheongdo','S.Chungcheongdo','N.Jeonrado','S.Jeonrado','N.Gyeongsangdo','S.Gyeongsangdo','Jejudo')";

        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof List);
        List l = (List) o;

        Assert.assertTrue(l.get(0).equals("Busan"));
    }

    @Test
    public void isSetSimpleIntegerTest() throws EvaluatorException{
        String expression = "SORTED_SET('FIELD_NUMBER',100, 23, 4324, 5)";

        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof List);
        List l = (List) o;

        Assert.assertTrue(l.get(0).equals(5d)); // output with Numbers will be in Double format
    }

    @Test
    public void isSetSimpleTest() throws EvaluatorException{
        String expression = "SORTED_SET('Seoul','Seoul','Seoul','Busan','Daegu','Incheon','Gwangju')";

        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof List);
        List l = (List) o;

        Assert.assertTrue(l.size()==5);
    }

    @Test
    public void isListParitionedTest() throws EvaluatorException{
        String expression = "LIST_PARTITION(LIST(TO_INT(1), TO_INT(23), 5, 100), 2)";

        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof List);
        List l = (List) o;
        Assert.assertTrue(l.size()==2); // output with Numbers will be in Double format
        Assert.assertTrue(l.get(0) instanceof List);
        Assert.assertTrue(l.get(1) instanceof List);

        List l1 = (List)l.get(0);
        Assert.assertTrue(l1.get(1) instanceof Number);
        Assert.assertTrue(l1.get(1).equals(23));

    }

    @Test
    public void doesFieldIncludeNull()throws EvaluatorException {
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        String expression = "FIELD('newFieldName', $textUnknown$, 'false')";

        Object o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Map);
        Map r = (Map) o;
        Assert.assertTrue(r.size() == 0);
    }

    @Test
    public void doesResolveGetByKeyDynamically() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        String expression = "FIELD('newFieldName', 'name')";

        Object o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Map);
        Map r = (Map) o;
        Assert.assertTrue(r.size() == 1);

        data.putAll(r);
        pathExtractor = new PathExtractor(data, mapListResolver);

        expression = "GET($newFieldName$, $child$)";

        o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof String);
        Assert.assertTrue("child".equals(o));
    }

    @Test
    public void hierarchicalExtractionTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        //    SelectMapperEvaluator evaluator = new SelectMapperEvaluator();

        //String expression = "KEYS(MAP(FIELD('this',EXTRACT($text$, '#this#'), 'false'), FIELD('longer',EXTRACT($text$, '#longer, test#'), 'false'), FIELD('has', EXTRACT($text$, '#longer,  test, has purposes#'),'false')))";
        String expression = "KEYS(MAP(FIELD('this',EXTRACT($text$, '#this#'), 'false'), FIELD('longer',EXTRACT($text$, '#longer, test#'), 'false'), FIELD('has', EXTRACT($text$, '#longer,  test, has purposes#'),'false')))";

        Object o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof List);

        List l = (List) o;
        Assert.assertTrue(l.size() == 3);
        Assert.assertTrue(l.get(0).equals("this"));
        Assert.assertTrue(l.get(1).equals("longer"));
        Assert.assertTrue(l.get(2).equals("has"));

        System.out.println(JsonUtils.getJsonFromObject(l));
    }

    @Test
    public void templateSimpleTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        String strToCompile = "T('ovo je moj text {{child.name}}')";
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(strToCompile, pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(o.equals("ovo je moj text child"));
    }

    @Test
    public void templateSimpleTestNumberModulo() throws EvaluatorException{
        Map<String, Object> data = getData();
        String strToCompile = "T('#10#') % T('#3#')";
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(strToCompile, pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(o.equals(1d));
    }

    @Test //- MULTILINE TEST NOT ALLOWED
    public void templateHtmlMultiLineTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        String strToCompile = "T('#<html>This is Twitter results </br> \n" +
                "    <pre> {{json child}}</pre>\n" +
                "</html>#')";
        //String strToCompile = "T('#<html>ovo je moj text {{child.name}}</html>#')";
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(strToCompile, pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(((String) o).startsWith("<html>"));
        Assert.assertTrue(((String) o).endsWith("</html>"));
    }

    @Test
    public void templateHtmlTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        String strToCompile = "T('#<html>ovo je moj </br> text <pre> {{child.name}} </pre> </html>#')";
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(strToCompile, pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(((String) o).startsWith("<html>"));
        Assert.assertTrue(((String) o).endsWith("</html>"));
    }

    @Test
    public void testCleanSentence()throws EvaluatorException{
        Map<String, Object> data = getData();
        String expression = "This is regular string";
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(expression, pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof String);

        Assert.assertTrue(o.equals("This is regular string"));
    }

    @Test
    public void testCleanSentenceWithSingleQuotes() throws EvaluatorException {
        Map<String, Object> data = getData();
        String expression = "'This is regular string'";
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(expression, pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof String);

        Assert.assertTrue(o.equals("This is regular string"));
    }

    @Test
    public void testJsonStringParse() throws EvaluatorException{
        Map<String, Object> data = getData();
        String expression = "'{\"prop1\":\"val1\", \"prop2\":23}'";
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);
        Object o = evaluate(expression, pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof String);
        Map processed = JsonUtils.getMapFromJson((String) o);
        Assert.assertNotNull(processed);
        Assert.assertTrue(processed.get("prop2").equals(23));
    }
   /* @Test - TODO: This should work as well - how to prarse JSON as "," is splitter and tokenizer will split
    public void testJsonStringMap(){
        Map<String,Object> data =  getData();
        String expression = "MAP('{\"prop1\":\"val1\", \"prop2\":23}')";
        PathExtractor pathExtractor = new PathExtractor(data);
        Object o =  evaluator.evaluate(expression, pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Map);
        Assert.assertTrue(((Map)o).get("prop2").equals(23));
    }*/

    @Test
    public void multiStepJsonTemplateMapTest() throws EvaluatorException{
        Map<String, Object> data = getData();
        PathExtractor pathExtractor = new PathExtractor(data, mapListResolver);

        String expression = "'{\"prop1\":\"val1\", \"prop2\":23}, \"dt\":\"{{dateTime}}\"}'";
        Object o = evaluate(expression, pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof String);

        data.put("toTemplate", expression);
        pathExtractor = new PathExtractor(data, mapListResolver);

        expression = "T($toTemplate$)";
        o = evaluate(expression, pathExtractor);

        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof String);
        data.put("toMap", o);
        pathExtractor = new PathExtractor(data, mapListResolver);

        expression = "MAP($toMap$)";
        o = evaluate(expression, pathExtractor);
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Map);
        Assert.assertTrue(((Map) o).get("prop2").equals(23));

    }
}
