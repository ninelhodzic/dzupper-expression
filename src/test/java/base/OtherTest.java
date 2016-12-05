package base;

import junit.framework.Assert;
import org.datazup.utils.DateTimeUtils;
import org.datazup.utils.JsonUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ninel on 11/30/16.
 */
public class OtherTest {

    @Test
    public void dateTimeTwitterFormatTest(){
        Object twitterCreatedAt = "Wed Aug 27 13:08:45 +0900 2008";
        DateTime dt = DateTimeUtils.resolve(twitterCreatedAt);
        Assert.assertNotNull(dt);
    }

    @Test
    public void isJsonKeyValid(){
        Map<String,Object> map = new HashMap<>();

        map.put("ime~prezime", 123);
        map.put("ime#prezime", 234);
        map.put("ime%prezime", 456);
        map.put("ime.prezime", 567);
        String json = JsonUtils.getJsonFromObject(map);

        Map<String,Object> map1 = JsonUtils.getMapFromJson(json);

        System.out.println(JsonUtils.getJsonFromObject(map1));
    }
}
