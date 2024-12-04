package org.hao.core;

import cn.hutool.core.util.ObjectUtil;

import java.util.Date;

/**
 * 提供内置函数的类，主要用于数学计算和日期处理
 */
public class ExpressionBuiltInFunc {

    // 相加
    public static double add(double a, double b) {
        return a + b;
    }

    // 相减
    public static double sub(double a, double b) {
        return a - b;
    }

    // 打印
    public static void print(Object obj) {
        System.out.println(obj);
    }

    /**
     * 向上取整
     * 使用 java.lang.Math.ceil() 来向上取整
     */
    public static double ceil(double a) {
        return java.lang.Math.ceil(a);
    }

    /**
     * 向下取整
     * 使用 java.lang.Math.floor() 来向下取整
     */
    public static double floor(double a) {
        return java.lang.Math.floor(a);
    }

    /**
     * 四舍五入
     * 使用 java.lang.Math.round() 进行四舍五入
     */
    public static long round(double a) {
        return java.lang.Math.round(a);
    }

    /**
     * 获取最大值
     * 使用 java.lang.Math.max() 获取最大值
     */
    public static int max(int a, int b) {
        return java.lang.Math.max(a, b);
    }

    /**
     * 获取最小值
     * 使用 java.lang.Math.min() 获取最小值
     */
    public static int min(int a, int b) {
        return java.lang.Math.min(a, b);
    }

    /**
     * 求幂 (base^exp)
     * 使用 java.lang.Math.pow() 计算 base 的 exp 次幂
     */
    public static double power(double base, double exp) {
        return java.lang.Math.pow(base, exp);
    }

    /**
     * 求平方根
     * 使用 java.lang.Math.sqrt() 来计算平方根
     */
    public static double sqrt(double a) {
        return java.lang.Math.sqrt(a);
    }

    /**
     * 获取随机数
     * 使用 java.lang.Math.random() 生成随机数
     */
    public static double random() {
        return java.lang.Math.random();
    }

    /**
     * 计算正弦
     * 使用 java.lang.Math.sin() 计算正弦（弧度制）
     */
    public static double sin(double a) {
        return java.lang.Math.sin(a);
    }

    /**
     * 计算余弦
     * 使用 java.lang.Math.cos() 计算余弦（弧度制）
     */
    public static double cos(double a) {
        return java.lang.Math.cos(a);
    }

    /**
     * 计算正切
     * 使用 java.lang.Math.tan() 计算正切（弧度制）
     */
    public static double tan(double a) {
        return java.lang.Math.tan(a);
    }

    /**
     * 求自然对数
     * 使用 java.lang.Math.log() 计算自然对数
     */
    public static double log(double a) {
        return java.lang.Math.log(a);
    }

    /**
     * 求以10为底的对数
     * 使用 java.lang.Math.log10() 计算以 10 为底的对数
     */
    public static double log10(double a) {
        return java.lang.Math.log10(a);
    }

    /**
     * 求指数 (e^x)
     * 使用 java.lang.Math.exp() 计算 e^x
     */
    public static double exp(double a) {
        return java.lang.Math.exp(a);
    }

    /**
     * 求反正弦
     * 使用 java.lang.Math.asin() 计算反正弦（弧度制）
     */
    public static double asin(double a) {
        return java.lang.Math.asin(a);
    }

    /**
     * 求反余弦
     * 使用 java.lang.Math.acos() 计算反余弦（弧度制）
     */
    public static double acos(double a) {
        return java.lang.Math.acos(a);
    }

    /**
     * 求反正切
     * 使用 java.lang.Math.atan() 计算反正切（弧度制）
     */
    public static double atan(double a) {
        return java.lang.Math.atan(a);
    }

    /**
     * 求坐标点 (x, y) 和 x 轴的夹角
     * 使用 java.lang.Math.atan2() 计算坐标 (x, y) 和 x 轴的夹角（弧度制）
     */
    public static double atan2(double y, double x) {
        return java.lang.Math.atan2(y, x);
    }

    /**
     * 去掉小数部分
     * 使用 java.lang.Math.floor() 去掉小数部分
     */
    public static double trunc(double a) {
        return java.lang.Math.floor(a);
    }

    /**
     * 获取当前日期时间
     *
     * @return 当前日期时间
     */
    public static Date now() {
        return new java.util.Date();
    }

    /**
     * 将对象转换为日期
     * 如果 date 为空，则返回当前日期时间
     * 如果 date 是 Date 类型，则直接返回
     * 否则，将 date 的字符串表示转换为日期
     *
     * @param date 日期对象
     * @return 转换后的日期
     */
    public static Date toDate(Object date) {
        if (ObjectUtil.isEmpty(date)) {
            return new cn.hutool.core.date.DateTime();
        }
        if (date instanceof Date) {
            return (Date) date;
        }
        return new cn.hutool.core.date.DateTime(date.toString());
    }

    /**
     * 格式化日期时间
     * 如果 date 为空，则格式化当前日期时间
     *
     * @param date 日期对象
     * @return 格式化后的日期时间字符串
     */
    public static String dateTime(Date date) {
        if (date == null) {
            return cn.hutool.core.date.DateUtil.formatDateTime(now());
        }
        return cn.hutool.core.date.DateUtil.formatDateTime(date);
    }

    /**
     * 日期偏移周数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移周数
     * @return 偏移后的日期
     */
    public static Date offsetWeek(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offsetWeek(date, offset);
    }

    /**
     * 日期偏移月数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移月数
     * @return 偏移后的日期
     */
    public static Date offsetMonth(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offsetMonth(date, offset);
    }

    /**
     * 日期偏移年数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移年数
     * @return 偏移后的日期
     */
    public static Date offsetYear(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offset(date, cn.hutool.core.date.DateField.YEAR, offset);
    }

    /**
     * 日期偏移天数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移天数
     * @return 偏移后的日期
     */
    public static Date offsetDay(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offsetDay(date, offset);
    }

    /**
     * 日期偏移小时数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移小时数
     * @return 偏移后的日期
     */
    public static Date offsetHour(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offsetHour(date, offset);
    }

    /**
     * 日期偏移分钟数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移分钟数
     * @return 偏移后的日期
     */
    public static Date offsetMinute(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offsetMinute(date, offset);
    }
}
