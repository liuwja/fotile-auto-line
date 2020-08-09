package com.fotile.util;

import com.datasweep.compatibility.ui.Time;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTimeUtils {

    public static String formatDate(Time time, String pattern) {
        return formatDate(
                transformDate(time), pattern);
    }

    public static java.util.Date transformDate(Time time) {
        if (time != null) {
            return time.getCalendar().getTime();
        }
        return null;
    }

    public static String formatDate(java.util.Date date, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.format(date.getTime());
        } catch (Exception e) {
            return "";
        }
    }
    public static com.datasweep.compatibility.ui.Time parseDateOfPnut(String text, String pattern)
    {
        return transformDate(parseDate(
                text, pattern));
    }

    /**
     * parseDate("2012-12-01","yyyy-MM-dd"),若发生异常(传入的日期字符串与指定的时间格式不符),返回null
     *
     * @param text
     * @param pattern
     * @return 根据指定格式将字符串转换后的日期
     */
    public static java.util.Date parseDate(String text, String pattern)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(text);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 将java.util.Date转换成com.datasweep.compatibility.ui.Time
     *
     * @param date
     * @return com.datasweep.compatibility.ui.Time 即ftpc中createTime()方法返回值的类型
     */
    public static com.datasweep.compatibility.ui.Time transformDate(java.util.Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        com.datasweep.compatibility.ui.Time date2 = new com.datasweep.compatibility.ui.Time(c);
        return date2;
    }
}
