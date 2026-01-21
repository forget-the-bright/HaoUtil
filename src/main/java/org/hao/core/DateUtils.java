package org.hao.core;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Date;

/**
 * 日期工具类，提供日期格式验证、日期字段转换和日期范围计算等功能
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/20 16:30
 */
public class DateUtils {
    //region 时间相关

    /**
     * 判断字符串是否符合日期的月份格式
     *
     * @param time 待验证的字符串
     * @return 如果字符串符合yyyy-MM的格式，返回true；否则返回false
     */
    public static Boolean isDateMonthStr(String time) {
        // 使用正则表达式匹配年份和月份的格式
        // ^\d{4}-(0[1-9]|1[0-2])$ 表示以4位数字开始，后跟一个短横线，然后是01到12之间的两位数字
        return ReUtil.isMatch("^\\d{4}-(0[1-9]|1[0-2])$", time);
    }


    /**
     * 检验字符串是否为有效的日期时间格式
     * 该方法主要用于验证输入的字符串是否符合"yyyy-MM-dd"的日期格式
     * 这里的有效性检查指的是字符串是否严格按照指定的格式排列，并不涉及日期的实际存在性（例如2月30日）
     *
     * @param time 待验证的字符串
     * @return 如果字符串符合"yyyy-MM-dd"格式，则返回true；否则返回false
     */
    public static boolean isDateTimeStr(String time) {
        // 使用正则表达式匹配日期时间格式，格式为"yyyy-MM-dd"
        // 其中，^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$ 表示：
        // ^ 表示字符串的开始
        // \d{4} 表示四位数字的年份
        // (0[1-9]|1[0-2]) 表示月份，01到09或者10到12
        // (0[1-9]|[1-2][0-9]|3[0-1]) 表示日期，01到09、10到29或者30到31
        // $ 表示字符串的结束
        // 这个正则表达式用于验证字符串是否符合"yyyy-MM-dd"的格式
        return ReUtil.isMatch("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$", time);
    }


    /**
     * 检查字符串是否符合日期年份的格式
     * 该方法用于验证给定的字符串是否表示一个四位数的年份
     *
     * @param time 待验证的字符串
     * @return 如果字符串符合年份格式 yyyy（四位数字），则返回true；否则返回false
     */
    public static Boolean isDateYearStr(String time) {
        // 使用正则表达式匹配四位数字的年份格式
        return ReUtil.isMatch("^\\d{4}$", time);
    }


    /**
     * 将查询类型字符串转换为对应的日期字段枚举
     * 支持的类型包括：month、day、hour、minute、second、week
     *
     * @param queryType 查询类型字符串
     * @return 对应的DateField枚举值，如果类型不匹配则返回null
     */
    public static DateField convertDateField(String queryType) {
        if (cn.hutool.core.util.StrUtil.equals(queryType, "month")) {
            return DateField.MONTH;
        }
        if (cn.hutool.core.util.StrUtil.equals(queryType, "day")) {
            return DateField.DAY_OF_YEAR;
        }
        if (cn.hutool.core.util.StrUtil.equals(queryType, "hour")) {
            return DateField.HOUR;
        }
        if (cn.hutool.core.util.StrUtil.equals(queryType, "minute")) {
            return DateField.MINUTE;
        }
        if (cn.hutool.core.util.StrUtil.equals(queryType, "second")) {
            return DateField.SECOND;
        }
        if (StrUtil.equals(queryType, "week")) {
            return DateField.WEEK_OF_YEAR;
        }
        return null;
    }

    /**
     * 获取指定日期在指定时间单位的起始时间
     *
     * @param date      原始日期
     * @param dateField 时间单位字符串（如："year", "month", "day"等）
     * @return 指定时间单位的起始时间
     */
    public static DateTime beginOf(Date date, String dateField) {
        return beginOf(date, convertDateField(dateField));
    }

    /**
     * 获取指定日期在指定时间单位的起始时间
     * 根据不同的日期字段类型调用相应的起始时间计算方法
     *
     * @param date      原始日期
     * @param dateField 时间单位枚举
     * @return 指定时间单位的起始时间
     */
    public static DateTime beginOf(Date date, DateField dateField) {
        if (dateField == DateField.YEAR) {
            return cn.hutool.core.date.DateUtil.beginOfYear(date);
        }
        if (dateField == DateField.MONTH) {
            return cn.hutool.core.date.DateUtil.beginOfMonth(date);
        }
        if (dateField == DateField.DAY_OF_MONTH ||
                dateField == DateField.DAY_OF_WEEK ||
                dateField == DateField.DAY_OF_WEEK_IN_MONTH ||
                dateField == DateField.DAY_OF_YEAR) {
            return cn.hutool.core.date.DateUtil.beginOfDay(date);
        }
        if (dateField == DateField.WEEK_OF_MONTH ||
                dateField == DateField.WEEK_OF_YEAR) {
            return cn.hutool.core.date.DateUtil.beginOfWeek(date);
        }
        if (dateField == DateField.HOUR || dateField == DateField.HOUR_OF_DAY) {
            return cn.hutool.core.date.DateUtil.beginOfHour(date);
        }
        if (dateField == DateField.MINUTE) {
            return cn.hutool.core.date.DateUtil.beginOfMinute(date);
        }
        if (dateField == DateField.SECOND) {
            return cn.hutool.core.date.DateUtil.beginOfSecond(date);
        }
        return new DateTime(date);
    }

    /**
     * 获取指定日期在指定时间单位的结束时间
     *
     * @param date      原始日期
     * @param dateField 时间单位字符串（如："year", "month", "day"等）
     * @return 指定时间单位的结束时间
     */
    public static DateTime endOf(Date date, String dateField) {
        return endOf(date, convertDateField(dateField));
    }

    /**
     * 获取指定日期在指定时间单位的结束时间
     * 根据不同的日期字段类型调用相应的结束时间计算方法
     *
     * @param date      原始日期
     * @param dateField 时间单位枚举
     * @return 指定时间单位的结束时间
     */
    public static DateTime endOf(Date date, DateField dateField) {
        if (dateField == DateField.YEAR) {
            return cn.hutool.core.date.DateUtil.endOfYear(date);
        }
        if (dateField == DateField.MONTH) {
            return cn.hutool.core.date.DateUtil.endOfMonth(date);
        }
        if (dateField == DateField.DAY_OF_MONTH ||
                dateField == DateField.DAY_OF_WEEK ||
                dateField == DateField.DAY_OF_WEEK_IN_MONTH ||
                dateField == DateField.DAY_OF_YEAR) {
            return cn.hutool.core.date.DateUtil.endOfDay(date);
        }
        if (dateField == DateField.WEEK_OF_MONTH ||
                dateField == DateField.WEEK_OF_YEAR) {
            return cn.hutool.core.date.DateUtil.endOfWeek(date);
        }
        if (dateField == DateField.HOUR || dateField == DateField.HOUR_OF_DAY) {
            return cn.hutool.core.date.DateUtil.endOfHour(date);
        }
        if (dateField == DateField.MINUTE) {
            return cn.hutool.core.date.DateUtil.endOfMinute(date);
        }
        if (dateField == DateField.SECOND) {
            return DateUtil.endOfSecond(date);
        }
        return new DateTime(date);
    }
    //endregion
}
