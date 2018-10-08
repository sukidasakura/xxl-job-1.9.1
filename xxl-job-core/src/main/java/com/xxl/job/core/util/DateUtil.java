package com.xxl.job.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2018年09月21日 9:00
 */
public class DateUtil {

    /**
     * 把long类型时间转化为标准时间
     * @param date Date类型时间，例如Wed Sep 19 13:37:25 CST 2018
     * @return 标准时间，例如2018-05-28 14:08:16
     */
    public static String convertDateTime(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static String convertToLongTime(String dateTime){
        Date date = new Date();
        long time = date.getTime();
        return String.valueOf(time);
    }
}
