# dzupper

Simple Expression parser and extractor for Java depending on path-extractor: https://github.com/datazup/path-extractor

Sample

```
Map<String,Object> map = new HashMap<>();
map.put("name", "value");

Map<String,Object> mapChild = new HashMap<>();
mapChild.put("value", 10);

map.put("child", mapChild);


SimpleObjectEvaluator evaluator = new SimpleObjectEvaluator();
PathExtractor pathExtractor = new PathExtractor(map);

boolean isTrue = evaluator.evaluate("$name.child.value$==10", pathExtractor);

isTrue = evaluator.evaluate("YEAR(NOW())==2016", pathExtractor);

```

Or using SelectMapperEvaluator to extract and manipulate data

```
SelectMapperEvaluator evaluator = new SelectMapperEvaluator();
PathExtractor pathExtractor = new PathExtractor(map);

Map<String,Object> newMap = evaluator.evaluate("FIELD('newFieldName', $name.child.value$)", pathExtractor);
// return map: { "newFieldName": 10 }

// Supports Handlebars template
String renderedString = evaluator.evaluate("This is simple string with value {{ $name.child.value$ }}", pathExtractor);

```