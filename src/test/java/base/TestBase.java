package base;

import org.joda.time.DateTime;
import org.joda.time.Hours;

import java.util.*;

/**
 * Created by admin@datazup on 11/25/16.
 */
public abstract class TestBase {

    protected List<Map<String,Object>> getListOfMaps(){
        List<Map<String,Object>> list1 = new ArrayList<>();
        for (int i = 0;i<10;i++) {
            Map<String,Object> o = new HashMap<>();
            o.put("price", 10*i);
            o.put("name", "item"+(i%2));
            o.put("type", "type"+(i%2));
            o.put("amount",2*(10-i));


            list1.add(o);
        }
        return list1;
    }

    protected List<Map<String,Object>> getNestedListOfMaps(){
        List<Map<String,Object>> list1 = new ArrayList<>();
        for (int i = 0;i<10;i++) {
            Map<String,Object> o = new HashMap<>();
            o.put("price", 10*i);
            o.put("date", DateTime.now().minusDays(i%3));
            o.put("name", "item"+(i%2));
            o.put("type", "type"+(i%2));
            o.put("amount",2*(10-i));

            List<Map<String,Object>> list2 = new ArrayList<>();

            for (int j = 0;j<10;j++) {
                Map<String, Object> r = new HashMap<>();
                r.put("price1", 10 * j);
                r.put("name1", "item" + (j % 2));
                r.put("type1", "type" + (j % 2));
                r.put("amount1", 2 * (10 - j));

                list2.add(r);
            }
            o.put("child", list2);

            list1.add(o);
        }
        return list1;
    }



    public Map<String, Object> getData() {
        Map<String, Object> child = new HashMap<>();
        child.put("name", "child");
        child.put("value", 1);
        child.put("value2", 15);
        child.put("value3", -5);
        child.put("valueTrue", true);
        child.put("valueStringFalse", "false");

        Map<String, Object> parent = new HashMap<>();

        List<Object> list = new ArrayList<>();
        list.add("Hello");
        list.add("Hi");
        list.add("Nice");

        Map<String, Object> mp = new HashMap<>();
        mp.put("first", 1);
        mp.put("second", "hah");

        Map<String, Object> cmp = new HashMap<>();
        cmp.put("thirdChild", "yupi");

        List<Object> ll = new ArrayList<>();
        ll.add("thirdhaha");
        ll.add("thirdopa");
        ll.add("thirdjope");
        cmp.put("thirdlist", ll);

        mp.put("third", cmp);

        List<Object> l = new ArrayList<>();
        l.add("childHello");
        l.add("child2Hello");
        l.add("child3Hello");
        mp.put("fourth", l);
        list.add(mp);

        List<Object> list1 = new ArrayList<>();
        Map map = new HashMap();
        map.put("n", "n");
        list1.add(map);

        child.put("list", list);
        child.put("html", "<a href=\"http://twitter.com/download/iphone\" rel=\"nofollow\">Twitter for iPhone</a>");

        parent.put("child", child);
        parent.put("fieldPrice", "20 KM");
        parent.put("fieldPrice1", "20,15 KM");
        parent.put("fieldPrice2", "20.15 KM");
        parent.put("list", list1);
        parent.put("dateTime", new DateTime());
        parent.put("dateTime1", (new DateTime()).minus(Hours.hours(12)));
        parent.put("date", new Date());
        parent.put("dateTimeString", (new DateTime()).toString());
        parent.put("tweetCreatedAt", "Tue Apr 18 18:35:35 +0000 2017");

        parent.put("text", "this is some longer text for testing purposes and we'll make it more longer having different keywords in it");

        parent.put("log", "o.a.s.m.n.Client client-boss-1 [ERROR] connection attempt 959 to Netty-Client-/45.79.165.184:6701 failed: java.net.ConnectException: Connection refused: /45.79.165.184:6701");

        parent.put("ts", 1464811823300L);
        parent.put("tz", 240);

        return parent;
    }

}
