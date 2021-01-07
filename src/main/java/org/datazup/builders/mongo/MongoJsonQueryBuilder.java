package org.datazup.builders.mongo;

import org.datazup.pathextractor.AbstractResolverHelper;
import org.datazup.utils.TypeUtils;

import java.util.*;

public class MongoJsonQueryBuilder {
    private static Set<String> whereKeywords = new HashSet<>(Arrays.asList("AND", "OR", "NOR"));
    private static Set<String> whereFundKeywords = new HashSet<>(Arrays.asList("and", "or", "in", "=", ">", ">=", "<=", "<", "!", "nor", "eq"));
    private static Map<String, String> whereMapper = new HashMap<String, String>() {{
        put("=", "eq");
        put(">", "gt");
        put(">=", "gte");
        put("<", "lt");
        put("<=", "lte");
        put("!", "not");
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

        List<Map<String, Object>> fields = getFields();

        Map filterStage = getFilterStage(fields, whereObj);
        // Map sumStage = getSumStage(groupByList);
        Map groupStage = getGroupStage(fields, groupByList);
        Map flattenStage = getFlattenStage(fields, groupByList);
        Map constraintsStage = getConstraintsStage(havingObj);

        Map sortingStage = getOrderByConstraint(orderByObj);


        if (null != filterStage) {
            aggregations.add(filterStage);
        }

        if (null != groupStage)
            aggregations.add(groupStage);
        if (null != flattenStage)
            aggregations.add(flattenStage);
        if (null != constraintsStage)
            aggregations.add(constraintsStage);

        if (null!=sortingStage){
            Map sort = new HashMap();
            sort.put("$sort", sortingStage);
            aggregations.add(sort);
        }

        query.put("AGGREGATIONS", aggregations);

        return query;
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

        return query;
    }

    private List<Map<String, Object>> getFields() {
        Object fieldsObj = jsonObject.get("fields");
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

            for (Map<String, Object> field : fields) {
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
            for (Map<String, Object> groupMap : groupByList) {
                String name = (String) groupMap.get("name");
                String func = null;
                String alias = null;
                if (groupMap.containsKey("alias")) {
                    alias = (String) groupMap.get("alias");
                }
                if (groupMap.containsKey("func")) {
                    Map<String, Object> tmpMap = new HashMap<>();
                    func = (String) groupMap.get("func");
                    tmpMap.put("$" + func, "$" + name);

                    if (null == alias)
                        alias = name + func.substring(0, 1).toUpperCase() + func.substring(1);

                    idMap.put(alias, tmpMap);
                } else {
                    if (null == alias)
                        alias = name;
                    idMap.put(alias, "$" + name);
                }
            }
        }
        return idMap;
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
                alias = name + func1.substring(0, 1).toUpperCase() + func1.substring(1);
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
            for (Map<String, Object> field : fields) {
                Map aliasFuncMap = getFieldAliasFuncMap(field);
                groupMap.putAll(aliasFuncMap);
            }

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("$group", groupMap);
            return res;
        }
        return null;
    }

    private Map getFilterStage(List<Map<String, Object>> fields, Map<String, Object> whereObj) {
        Map<String, Object> res = new HashMap<>();
        Map<String, Object> match = getWhereConstraint(whereObj);
        if (null == match) {
            return null;
        }
        res.put("$match", match);
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