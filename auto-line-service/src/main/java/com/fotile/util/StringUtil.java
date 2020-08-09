package com.fotile.util;

import com.datasweep.compatibility.ui.Time;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {
    public static boolean isNotNull(String str){
        return !isNull(str);
    }

    public static boolean isNull(String str){
        if(str == null || str.trim().isEmpty())
            return true;
        return false;
    }

    public static boolean isNumber(String str)
    {
        try{
            new BigDecimal(str);
            return true;
        }catch (NumberFormatException e) {
            return false;
        }
    }
    // 格式化时间
    public static String formatTime(Time time, String formatter)
    {
        if(time!=null)
        {
            Date date = time.getCalendar().getTime();
            return new SimpleDateFormat(formatter).format(date);
        }

        return "";
    }
}
