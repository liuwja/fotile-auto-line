package com.fotile.constant;

public interface IDateFormat {
    /**
     * yyyy-MM-dd
     */
    String TIME_STANDARD = "yyyy-MM-dd";

    /**
     * yyyy-MM
     */
    String TIME_NO_DAY = "yyyy-MM";

    /**
     * MM-dd HH:mm:ss
     */
    String TIME_STAMPING = "HH:mm:ss";

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    String TIME_LONG = "yyyy-MM-dd HH:mm:ss";

    /**
     * HH:mm:ss
     */
    String TIME_SHORT = "HH:mm:ss";

    /**
     * HH:mm
     */
    String TIME_HOUR_MIN = "HH:mm";

    /**
     * yyyyMMdd
     */
    String TIME_DAY = "yyyyMMdd";

    /**
     * yyyyMMddHH
     */
    String TIME_HOUR = "yyyyMMddHH";

    /**
     * yyMMdd
     */
    String TIME_SHORT_DAY = "yyMMdd";

    /**
     * yyyyMMddHHmmssSSS
     */
    String TIME_SEQUENCE = "yyyyMMddHHmmssSSS";

    /**
     * 年月日
     */
    String TIME_CHINA_DAY = "yyyy年MM月dd日";
}
