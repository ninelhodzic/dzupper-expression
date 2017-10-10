
package org.datazup.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by ninel on 11/25/16.
 */

public class DateTimeUtils {

    private static List<DateTimeFormatter> COMMON_DATE_TIME_FORMATS =
            Arrays.asList(ISODateTimeFormat.dateTime(),
                    getFormatter("yyyy-MM-dd'T'hh:mm:ss"),
                    getFormatter("EEE MMM dd HH:mm:ss Z yyyy"),
                    getFormatter("EEE MMM dd HH:mm:ss z yyyy"));

    private static DateTimeFormatter getFormatter(String format) {
       DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withZoneUTC().withLocale(Locale.ENGLISH);
        return fmt;
    }

    private static DateTimeFormatter getFormatter(String format, Locale locale) {

        DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withZoneUTC().withLocale(locale);
        return fmt;
    }

    public static Instant resolve(Object obj) {
        if (obj instanceof DateTime) {
            return from((DateTime)obj);
        } else if (obj instanceof Long)
            return Instant.ofEpochMilli((Long) obj);
        else if (obj instanceof String) {
            Instant dt = null;
            try {
                dt = Instant.parse((String)obj);
                return dt;
            }catch (Exception e){
               // nothing
            }

            if (null==dt) {
                for (DateTimeFormatter fmt : COMMON_DATE_TIME_FORMATS) {
                    dt = resolve(fmt, (String) obj);
                    if (null != dt) {
                        return dt;
                    }
                }
            }
        }
        return null;
    }

    public static Instant from(DateTime dt){
        Instant i = Instant.ofEpochMilli(dt.getMillis());
        return i;
    }

    public static Instant resolve(DateTimeFormatter fmt, String dateString){

        try {
            DateTime dt = fmt.parseDateTime(dateString);
            return resolve(dt);
        }catch (Exception e){
           // e.printStackTrace();
            return null;
        }
    }

    public static Instant resolve(Long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

    public static Instant resolve(String datetime) {
        return Instant.parse(datetime);
    }

    public static Instant resolve(String datetime, String format) {
        DateTimeFormatter fmt = getFormatter(format);
        return resolve(fmt, datetime);
    }

    public static int getSecond(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getSecond();
    }
    public static int getMinute(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getMinute();
    }
    public static int getHour(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getHour();
    }
    public static int getDayOfMonth(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getDayOfMonth();
    }

    public static int getMonth(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getMonthValue();
    }
    public static int getYear(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getYear();
    }

    public static Instant format(Instant dt, String format) {
        DateTimeFormatter formatter = getFormatter(format);
        DateTime dti = new DateTime( dt.toEpochMilli());
        String dtString = dti.toString(formatter);
        return resolve(dtString, format);
    }
    public static Instant format(DateTime dt, String format) {
        String dtString = dt.toString(format);
        return resolve(dtString, format);
    }

    public static Instant format(Date dt, String format) {
        Instant instant = dt.toInstant();
        return format(instant, format);
    }

    public static Instant format(Long dt, String format) {
        Instant instant = Instant.ofEpochMilli(dt);
        return format(instant, format);
    }

}
