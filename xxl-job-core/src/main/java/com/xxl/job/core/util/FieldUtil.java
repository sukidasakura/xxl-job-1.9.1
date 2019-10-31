package com.xxl.job.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年10月25日 15:04
 */
public class FieldUtil {

    public static final char UNDERLINE = '_';

    public static final String P_REGEX = "[A-Z]([a-z\\d]+)?";

    public static final String L_REGEX = "([A-Za-z\\d]+)(_)?";

    /**
     *
     * @Description:驼峰转下划线
     * @author: yaozhiheng@supcon.com
     * @param param
     * @return
     * @date: 2016年8月1日 下午4:37:47
     */
    public static String camelToUnderline(String param) {
        if (StringUtils.isBlank(param)) {
            return StringUtils.EMPTY;
        }
        param = String.valueOf(param.charAt(0)).toUpperCase().concat(param.substring(1));
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile(P_REGEX);
        Matcher matcher = pattern.matcher(param);
        while (matcher.find()) {
            String word = matcher.group();
            sb.append(word.toUpperCase());
            sb.append(matcher.end() == param.length() ? "" : UNDERLINE);
        }
        return sb.toString();
    }

    public static String camelToLine(String param) {
        if (StringUtils.isBlank(param)) {
            return StringUtils.EMPTY;
        }
        param = String.valueOf(param.charAt(0)).toUpperCase().concat(param.substring(1));
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile(P_REGEX);
        Matcher matcher = pattern.matcher(param);
        while (matcher.find()) {
            String word = matcher.group();
            sb.append(word.toUpperCase());
            sb.append(matcher.end() == param.length() ? "" : "-");
        }
        return sb.toString();
    }

    /**
     *
     * @Description:下划线转驼峰
     * @author: yaozhiheng@supcon.com
     * @param line
     * @return
     * @date: 2016年8月1日 下午4:38:05
     */
    public static String underlineToCamel(String line) {
        if (StringUtils.isBlank(line)) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile(L_REGEX);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String word = matcher.group();
            sb.append(matcher.start() == 0 ? Character.toLowerCase(word.charAt(0)) : Character.toUpperCase(word.charAt(0)));
            int index = word.lastIndexOf(UNDERLINE);
            if (index > 0) {
                sb.append(word.substring(1, index).toLowerCase());
            } else {
                sb.append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * 表名转换为驼峰命名(首字母大写)
     */
    public static String underlineToCamelBig(String str) {
        String result = "";

        String[] strArr = str.trim().split("_");
        for (String s : strArr) {
            if (s.length() > 1) {
                result += s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            } else {
                result += s.toUpperCase();
            }
        }

        return result;
    }

    /**
     *
     * @Description:关联查询属性命名规则
     * @author: chenxingjian@supcon.com
     * @param line
     * @return
     * @date: 2017年6月19日 上午10:51:21
     */
    public static String underlineToCamelFirst(String line) {
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile(L_REGEX);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String word = matcher.group();
            sb.append(word.substring(0, 1).toLowerCase());
        }
        return sb.toString();
    }
}
