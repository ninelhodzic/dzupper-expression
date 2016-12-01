
package org.datazup.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;


/**
 * Created by ninel on 11/25/16.
 */

public class DateTimeUtils {

    public static DateTime resolve(Object obj){
        if (obj instanceof DateTime){
            return (DateTime)obj;
        }
        else if (obj instanceof Long)
            return resolve((Long)obj);
        else if (obj instanceof String){
            try {
                DateTime dt = resolve((String) obj, "EEE MMM dd HH:mm:ss Z yyyy");

                return dt;
            }catch (Exception e){
                try {
                    DateTime dt1 = resolve((String) obj);
                    return dt1;
                }catch (Exception e1){

                }
            }
        }
        return null;
    }

    public static DateTime resolve(Long timestamp){
        return new DateTime(timestamp);
    }

    public static DateTime resolve(String datetime){
        return new DateTime(datetime);
    }

    public static DateTime resolve(String datetime, String format){
        DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withLocale(Locale.ENGLISH);
        DateTime dt = fmt.parseDateTime(datetime).toDateTime(DateTimeZone.UTC);
        return dt;
    }
}
