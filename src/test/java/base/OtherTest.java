package base;

import junit.framework.Assert;
import org.datazup.utils.DateTimeUtils;
import org.joda.time.DateTime;
import org.junit.Test;

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
}
