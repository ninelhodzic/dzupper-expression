package org.datazup.builders.mongo;

import org.apache.commons.lang3.ObjectUtils;
import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.utils.TypeUtils;

import java.util.*;

public class MongoJsonQueryBuilder {
    private static Set<String> whereKeywords = new HashSet<>(Arrays.asList("AND", "OR", "NOR"));
    private static Set<String> whereFundKeywords = new HashSet<>(Arrays.asList("and", "or", "in", "=", ">", ">=", "<=", "<", "!", "nor", "eq", "!="));
    private static Map<String, String> whereMapper = new HashMap<String, String>() {{
        put("=", "eq");
        put(">", "gt");
        put(">=", "gte");
        put("<", "lt");
        put("<=", "lte");
        put("!", "not");
        put("!=", "ne");
    }};
    private Map<String, Object> jsonObject;
    private AbstractResolverHelper mapListResolver;

    public MongoJsonQueryBuilder(Map<String, Object> jsonObject, AbstractResolverHelper mapListResolver) {
        this.jsonObject = jsonObject;
        this.mapListResolver = mapListResolver;
    }

    public Map<String, Object> execute() {
        Object groupByList = jsonObject.get("groupBy");

        Map<String, Object> query = null;
        if (null == groupByList) {
            query = buildFindQuery();
        } else {
            query = buildAggregationQuery();
        }
        return query;
    }

    private Map buildAggregationQuery() {
        Map<String, Object> query = new HashMap<>();
        List<Map<String, Object>> aggregations = new ArrayList<>();
        // Object fieldsObj = jsonObject.get("fields");
        List<Map<String, Object>> groupByList = mapListResolver.resolveToList(jsonObject.get("groupBy"));
        List<Map<String, Object>> orderByObj = mapListResolver.resolveToList(jsonObject.get("orderBy"));
        Map<String, Object> havingObj = mapListResolver.resolveToMap(jsonObject.get("having"));
        Map<String, Object> whereObj = mapListResolver.resolveToMap(jsonObject.get("where"));
        Integer limit = TypeUtils.resolveInteger(jsonObject.get("limit"));
        Integer skip = TypeUtils.resolveInteger(jsonObject.get("skip"));

        List<Map<String, Object>> fields = getFields();

        List<String> filterStageUnwind = new ArrayList<>();

        Map filterStage = getFilterStage(filterStageUnwind, whereObj);
        // Map sumStage = getSumStage(groupByList);
        List<Map> unwindStage = getUnwindStage(fields, groupByList);
        Map groupStage = getGroupStage(fields, groupByList);
        Map flattenStage = getFlattenStage(fields, groupByList);
        Map constraintsStage = getConstraintsStage(havingObj);

        Map sortingStage = getOrderByConstraint(orderByObj);


        if (null != filterStage) {
            if (null != filterStageUnwind && filterStageUnwind.size() > 0) {
                filterStageUnwind.forEach(toUnwind -> {
                    Map<String, Object> filterUnwind = new HashMap<>();
                    if (null != toUnwind && !toUnwind.isEmpty()) {
                        filterUnwind.put("$unwind", "$" + toUnwind);
                        aggregations.add(filterUnwind);
                    }
                });


            }
            aggregations.add(filterStage);
        }
        if (null != unwindStage) {
            unwindStage.forEach(map -> {
                aggregations.add(map);
            });
        }

        if (null != groupStage) {
            aggregations.add(groupStage);
        }
        if (null != flattenStage)
            aggregations.add(flattenStage);
        if (null != constraintsStage)
            aggregations.add(constraintsStage);

        if (null != sortingStage) {
            Map sort = new HashMap();
            sort.put("$sort", sortingStage);
            aggregations.add(sort);
        }

        if (null != limit) {
            Map l = new HashMap();
            l.put("$limit", limit);
            aggregations.add(l);
        }
        if (null != skip) {
            Map l = new HashMap();
            l.put("$skip", limit);
            aggregations.add(l);
        }

        query.put("AGGREGATIONS", aggregations);

        return query;
    }

    private List<String> unwindByString(String name) {
        if (null != name) {
            if (name.contains(".")) {
                String[] splited = name.split("\\.");
                if (null != splited && splited.length > 1) {
                    List<String> list = new ArrayList<>();

                    for (int i = 0; i < splited.length - 1; i++) {
                        String item = splited[i];
                        if (i > 0) {
                            item = list.get(i) + "." + item;
                        }
                        list.add(item);
                    }

                    return list;
                }
            }
        }
        return null;
    }

    private List<String> unwindSplitted(Map map) {
        String name = (String) map.get("name");

        return unwindByString(name);
    }

    private void addToUnwind(List<String> unwindList, List<String> tmpNew) {
        if (tmpNew != null && tmpNew.size() > 0) {
            tmpNew.forEach(r -> {
                if (!unwindList.contains(r)) {
                    unwindList.add(r);
                }
            });
        }
    }

    private List<Map> getUnwindStage(List<Map<String, Object>> fields, List<Map<String, Object>> groupByList) {


        /*

            {
                "items":[
                    {
                        "tags":[
                            '1','2','3'
                        ]
                    },
                    {
                        "tags":[
                            '1','2','3'
                        ]
                    }
                ]
            }

          // First Stage
              { $unwind: "$items" },
              // Second Stage
              { $unwind: "$items.tags" },

         */
        List<String> unwinds = new ArrayList<>();
        if (fields != null) {
            for (Map<String, Object> field : fields) {
                List<String> tmpF = unwindSplitted(field);
                addToUnwind(unwinds, tmpF);
            }
        }

        if (groupByList != null) {
            for (Map<String, Object> group : groupByList) {
                List<String> tmpG = unwindSplitted(group);
                addToUnwind(unwinds, tmpG);
            }
        }

        if (unwinds.size() > 0) {
            List<Map> unwindsMapList = new ArrayList<>();
            unwinds.forEach(u -> {
                Map m = new HashMap();
                m.put("$unwind", "$" + u);
                unwindsMapList.add(m);
            });
            return unwindsMapList;
        }

        return null;
    }

    private Map buildFindQuery() {
        Map<String, Object> query = new HashMap<>();
        List<Map<String, Object>> fields = getFields();
        /*
        Query Object:
        {
            "fields":"*",
            "where":{ "AND":[] },
            "orderBy":[{"name":"field1,"value":1, "func":"hour"]
        }
         */
        /*
        {
            fields:[{field1:1, field2: 1}],
            sort:{ field1:-1, field2: 1},
            find:{
                $and:[{field2:'123',field3: { $gte: 10 }},{ field4: $in:[1,2,3]}],
                $nor:[]
            }
        }
         */
        if (null != fields) {

            Map map = new HashMap();
            for (Map<String, Object> field : fields) {
                String fieldName = TypeUtils.resolveString(field.get("name"));

                map.put(fieldName, 1);
            }
            query.put("fields", map);
        }
        Map<String, Object> whereObj = getWhereConstraint(mapListResolver.resolveToMap(jsonObject.get("where")));
        if (null != whereObj) {
            query.put("find", whereObj);
        }
        Map<String, Object> orderBy = getOrderByConstraint(mapListResolver.resolveToList(jsonObject.get("orderBy")));
        if (null != orderBy) {
            query.put("sort", orderBy);
        }

        Integer limit = TypeUtils.resolveInteger(jsonObject.get("limit"));
        if (null != limit) {
            query.put("limit", limit);
        }
        Integer skip = TypeUtils.resolveInteger(jsonObject.get("skip"));
        if (null != skip) {
            query.put("skip", skip);
        }

        return query;
    }

    private List<Map<String, Object>> getFields() {
        Object fieldsObj = jsonObject.get("fields");
        if (null == fieldsObj)
            return null;
        List<Map<String, Object>> fields = null;
        if (!fieldsObj.equals("*")) {
            fields = mapListResolver.resolveToList(fieldsObj);
        }
        return fields;
    }

    private Map<String, Object> getOrderByConstraint(List<Map<String, Object>> orderByObject) {
        if (null != orderByObject) {
            /* sort: { field1: 1, field2: -1} */
            Map<String, Object> res = new LinkedHashMap<>();
            for (Map<String, Object> obj : orderByObject) {
                Object valOb = obj.get("value");
                Integer val = 1;
                if (valOb instanceof String) {
                    if (valOb.equals("DESC")) {
                        val = -1;
                    } else if (valOb.equals("ASC")) {
                        val = 1;
                    } else {
                        val = TypeUtils.resolveInteger(valOb);
                    }
                } else {
                    val = TypeUtils.resolveInteger(valOb);
                }
                res.put((String) obj.get("name"), val);
            }
            return res;
        }
        return null;
    }

    private Map<String, Object> processAggregationWhereSingleMap(Map<String, Object> map, List<String> unwinds) {
        if (null == map) {
            return null;
        }
        Map<String, Object> res = new HashMap<>();
        String funcObj = (String) map.get("func");
        String func = funcObj;
        if (whereFundKeywords.contains(funcObj)) {
            func = whereMapper.get(funcObj.toLowerCase());
            if (func == null) {
                func = funcObj;
            }
        }
        String name = (String) map.get("name");
        Object val = map.get("value");
        /*if (funcObj.equals("=")) {
            res.put(name, val);
        } else {
            Map m = new HashMap();
            m.put("$" + func.toLowerCase(), val);
            res.put(name, m);
        }*/

        // check if there is anything to unwind first
        List<String> tmpUnwinded = unwindByString(name);
        if (null != tmpUnwinded) {
            tmpUnwinded.forEach(r -> {
                if (!unwinds.contains(r)) {
                    unwinds.add(r);
                }
            });
        }

        if (val instanceof String) {
            if (((String)
                    val).contains("::")) {
                Integer index = ((String) val).indexOf("::");
                String valueType = ((String) val).substring(0, index);
                String value = ((String) val).substring(index + 2);

                if (null != valueType) {
                    switch (valueType) {
                        case "DATE":
                            Map dtFromString = new HashMap();
                            Map dtString = new HashMap();
                            dtString.put("dateString", value);
                            dtFromString.put("$dateFromString", dtString);
                            val = dtFromString;
                            break;
                    }
                }
            }
        }

        List listVals = new ArrayList();
        listVals.add("$" + name);
        listVals.add(val);
        res.put("$" + func.toLowerCase(), listVals);


        return res;
    }

    private Map<String, Object> processWhereSingleMap(Map<String, Object> map) {
        if (null == map) {
            return null;
        }
        Map<String, Object> res = new HashMap<>();
        String funcObj = (String) map.get("func");
        String func = funcObj;
        if (whereFundKeywords.contains(funcObj)) {
            func = whereMapper.get(funcObj.toLowerCase());
            if (func == null) {
                func = funcObj;
            }
        }
        String name = (String) map.get("name");
        Object val = map.get("value");
        if (funcObj.equals("=")) {
            res.put(name, val);
        } else {
            Map m = new HashMap();
            m.put("$" + func.toLowerCase(), val);
            res.put(name, m);
        }

        return res;
    }

    private Map<String, Object> getWhereConstraint(Map<String, Object> whereObject) {
        if (null != whereObject) {
            Map<String, Object> res = new HashMap<>();
            /* "where":{ "AND":[{"name":"asd","value":123, "func":"="},{"name":"fied3","func":">=","value":10}] }, */
            /* $and:[{field2:'123',field3: { $gte: 10 }},{ field4: $in:[1,2,3]}] */
            if (whereObject.containsKey("AND") || whereObject.containsKey("OR") || whereObject.containsKey("NOR")) {
                for (String key : whereObject.keySet()) {
                    Object val = whereObject.get(key);
                    boolean keyword = whereKeywords.contains(key);
                    if (keyword) {
                        List l = mapListResolver.resolveToList(val);
                        List tmpList = new ArrayList();
                        for (Object o : l) {
                            Map m = mapListResolver.resolveToMap(o);
                            if (null != m) {
                                Map tmp = null;
                                if (m.containsKey("AND") || m.containsKey("OR") || m.containsKey("NOR")) {
                                    tmp = getWhereConstraint(m);
                                } else {
                                    tmp = processWhereSingleMap(m);
                                }
                                if (null != tmp) {
                                    tmpList.add(tmp);
                                }
                            }
                        }
                        res.put("$" + key.toLowerCase(), tmpList);
                    }
                }
            } else {
                return processWhereSingleMap(whereObject);
            }
            return res;
        }
        return null;
    }


    private Map<String, Object> getAggregationWhereConstraint(Map<String, Object> whereObject, List<String> toUnwind) {
        if (null != whereObject) {
            Map<String, Object> res = new HashMap<>();
            /* "where":{ "AND":[{"name":"asd","value":123, "func":"="},{"name":"fied3","func":">=","value":10}] }, */
            /* $and:[{field2:'123',field3: { $gte: 10 }},{ field4: $in:[1,2,3]}] */
            if (whereObject.containsKey("AND") || whereObject.containsKey("OR") || whereObject.containsKey("NOR")) {
                for (String key : whereObject.keySet()) {
                    Object val = whereObject.get(key);
                    boolean keyword = whereKeywords.contains(key);
                    if (keyword) {
                        List l = mapListResolver.resolveToList(val);
                        List tmpList = new ArrayList();
                        for (Object o : l) {
                            Map m = mapListResolver.resolveToMap(o);
                            if (null != m) {
                                Map tmp = null;
                                if (m.containsKey("AND") || m.containsKey("OR") || m.containsKey("NOR")) {
                                    tmp = getAggregationWhereConstraint(m, toUnwind);
                                } else {
                                    tmp = processAggregationWhereSingleMap(m, toUnwind);
                                }
                                if (null != tmp) {
                                    tmpList.add(tmp);
                                }
                            }
                        }
                        res.put("$" + key.toLowerCase(), tmpList);
                    }
                }
            } else {
                return processAggregationWhereSingleMap(whereObject, toUnwind);
            }
            return res;
        }
        return null;
    }

    private Map<String, Object> getConstraintsStage(Map<String, Object> havingObj) {
        if (null != havingObj) {
            Map<String, Object> matchMap = getWhereConstraint(havingObj);
            if (null == matchMap)
                return null;
            Map<String, Object> res = new HashMap<>();
            res.put("$match", matchMap);
            return res;
        }
        return null;
    }

    private Map<String, Object> getFlattenStage(List<Map<String, Object>> fields, List<Map<String, Object>> groupByList) {
        if (null != groupByList && !groupByList.isEmpty() && null != fields && !fields.isEmpty()) {

            List mergedObjectsList = new ArrayList();
            Map<String, Object> idMap = getGroupByIdMap(groupByList);

            for (String key : idMap.keySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put(key, "$_id." + key);
                mergedObjectsList.add(map);
            }

            for (Object fieldObj : fields) {
                Map<String, Object> field = mapListResolver.resolveToMap(fieldObj);
                Map<String, Object> m = getFieldAliasFuncMap(field);
                for (String key : m.keySet()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(key, "$" + key);
                    mergedObjectsList.add(map);
                }
            }

            Map mergedObjects = new HashMap();
            mergedObjects.put("$mergeObjects", mergedObjectsList);

            Map newRoot = new HashMap();
            newRoot.put("newRoot", mergedObjects);

            Map replaceRoot = new HashMap();
            replaceRoot.put("$replaceRoot", newRoot);

            return replaceRoot;
        }
        return null;
    }

    private Map<String, Object> getGroupByIdMap(List<Map<String, Object>> groupByList) {
        /* { $group: { _id: { "$groupBuyFields" }, funcName_fieldName_or_alias: { func: "$fieldName"}*/
        Map<String, Object> idMap = null;
        if (null != groupByList) {
            idMap = new LinkedHashMap<>();
            for (Object o : groupByList) {
                Map<String, Object> groupMap = mapListResolver.resolveToMap(o);

                String name = (String) groupMap.get("name");
                String alias = null;
                if (groupMap.containsKey("alias")) {
                    alias = (String) groupMap.get("alias");
                }
                if (groupMap.containsKey("func")) {

                    Map<String, Object> tmpMap = new HashMap<>();
                    String func = (String) groupMap.get("func");
                    Map funcParams = getFuncParams(groupMap);
                    if (null == funcParams) {
                        tmpMap.put("$" + func, "$" + name);
                    } else {
                        tmpMap.put("$" + func, funcParams);
                    }

                    if (null == alias) {
                        String tmpName = name.replaceAll("\\.", "_");
                        alias = tmpName + func.substring(0, 1).toUpperCase() + func.substring(1);
                    }

                    idMap.put(alias, tmpMap);
                } else {
                    if (null == alias) {
                        String tmpName = name.replaceAll("\\.", "_");
                        alias = tmpName;
                    }
                    idMap.put(alias, "$" + name);
                }
            }
        }
        return idMap;
    }

    private Map<String, Object> getFuncParams(Map<String, Object> groupMap) {
        if (groupMap.containsKey("funcParams")) {
            Map<String, Object> m = mapListResolver.resolveToMap(groupMap.get("funcParams"));
            return m;
        }
        return null;
    }

    private Map<String, Object> getFieldAliasFuncMap(Map<String, Object> field) {
        String name = (String) field.get("name");
        String func = (String) field.get("func");
        String alias = null;
        if (field.containsKey("alias")) {
            alias = (String) field.get("alias");
        } else {
            if (name.equals("*")) {
                alias = func.toLowerCase();
            } else {
                String func1 = func.toLowerCase();
                String tmpName = name.replaceAll("\\.", "_");
                alias = tmpName + func1.substring(0, 1).toUpperCase() + func1.substring(1);
            }
        }
        Map<String, Object> funcMap = new HashMap<>();
        /* { $sum: { $multiply: [ "$price", "$quantity" ] } }
         * { $avg: "$quantity" }
         * { $sum: 1 }
         *  */
        String fn = func.toLowerCase();
        if (fn.equals("count")) {
            funcMap.put("$sum", 1);
        } else {
            funcMap.put("$" + fn, "$" + name);
        }
        Map m = new HashMap();
        m.put(alias, funcMap);
        return m;
    }

    private Map<String, Object> getGroupStage(List<Map<String, Object>> fields, List<Map<String, Object>> groupByList) {
        if (null != groupByList && !groupByList.isEmpty() && null != fields && !fields.isEmpty()) {

            Map<String, Object> idMap = getGroupByIdMap(groupByList);

            Map<String, Object> groupMap = new LinkedHashMap<>();
            groupMap.put("_id", idMap);
            for (Object obj : fields) {
                Map<String, Object> field = mapListResolver.resolveToMap(obj);
                Map aliasFuncMap = getFieldAliasFuncMap(field);
                groupMap.putAll(aliasFuncMap);
            }

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("$group", groupMap);
            return res;
        }
        return null;
    }

    private Map getFilterStage(List<String> toUnwind, Map<String, Object> whereObj) {
        Map<String, Object> res = new HashMap<>();

        Map<String, Object> match = getAggregationWhereConstraint(whereObj, toUnwind);
        if (null == match) {
            return null;
        }

        Map<String, Object> expr = new HashMap<>();
        expr.put("$expr", match);

        res.put("$match", expr);
        return res;
    }
}


/*

if (keyword){
                    String kkey = lowKey;
                    if(whereMappaer.containsKey(lowKey)){
                        kkey = whereMappaer.get(lowKey);
                    }
                    if (val instanceof List){
                        List tmpL = new ArrayList();

                        List l = mapListResolver.resolveToList(val);
                        for(Object obj: l){
                            if (obj instanceof Map){
                                Map<String,Object> r = getWhereConstraint((Map)obj);
                                tmpL.add(r);
                            }else{
                                tmpL.add(obj);
                            }
                        }
                        res.put("$"+kkey, tmpL);
                    }else if (val instanceof Map){
                        Map<String,Object> r = getWhereConstraint((Map)val);
                        res.put("$"+kkey, r);
                    }else{
                        res.put("$"+kkey, val);
                    }

                }else{

                }
 */


   /*List tmpL = new ArrayList();

                List<Map<String,Object>> list = mapListResolver.resolveToList(whereObject.get(key));
                for(Object obj: list){
                    if (obj instanceof Map){
                        Map<String,Object> r = getWhereConstraint((Map)obj);
                    }else{
                        tmpL.add(obj);
                    }

                }
                res.put("$"+key.toLowerCase(), tmpL);*/