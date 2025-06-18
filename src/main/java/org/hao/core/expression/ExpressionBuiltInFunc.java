package org.hao.core.expression;

import cn.hutool.core.util.ObjectUtil;

import java.util.Date;

/**
 * 提供一组内置函数，用于支持表达式引擎中的数学运算与日期处理。
 *
 * <p>该类包含常用数学函数（如加减乘除、三角函数、指数对数等）和日期操作函数
 * （如获取当前时间、格式化、偏移等），适用于在 MVEL 表达式中直接调用。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2024/12/3
 */

public class ExpressionBuiltInFunc {

    /**
     * 返回两个双精度浮点数的和。
     *
     * @param a 第一个加数
     * @param b 第二个加数
     * @return 两数之和
     */
    public static double add(double a, double b) {
        return a + b;
    }

    /**
     * 返回两个双精度浮点数的差。
     *
     * @param a 被减数
     * @param b 减数
     * @return 差值
     */
    public static double sub(double a, double b) {
        return a - b;
    }

    /**
     * 打印指定对象到控制台。
     *
     * @param obj 要打印的对象
     */
    public static void print(Object obj) {
        System.out.println(obj);
    }

    /**
     * 返回大于或等于指定双精度浮点数的最小整数。
     *
     * @param a 要向上取整的数值
     * @return 向上取整后的结果
     */
    public static double ceil(double a) {
        return java.lang.Math.ceil(a);
    }

    /**
     * 返回小于或等于指定双精度浮点数的最大整数。
     *
     * @param a 要向下取整的数值
     * @return 向下取整后的结果
     */
    public static double floor(double a) {
        return java.lang.Math.floor(a);
    }

    /**
     * 返回最接近指定双精度浮点数的长整型数值。
     *
     * @param a 要四舍五入的数值
     * @return 四舍五入后的结果
     */
    public static long round(double a) {
        return java.lang.Math.round(a);
    }

    /**
     * 返回两个整数中的最大值。
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 最大值
     */
    public static int max(int a, int b) {
        return java.lang.Math.max(a, b);
    }

    /**
     * 返回两个整数中的最小值。
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 最小值
     */
    public static int min(int a, int b) {
        return java.lang.Math.min(a, b);
    }

    /**
     * 返回底数的指数次幂。
     *
     * @param base 底数
     * @param exp  指数
     * @return 结果值
     */
    public static double power(double base, double exp) {
        return java.lang.Math.pow(base, exp);
    }

    /**
     * 返回指定双精度浮点数的平方根。
     *
     * @param a 非负数
     * @return 平方根
     */
    public static double sqrt(double a) {
        return java.lang.Math.sqrt(a);
    }

    /**
     * 返回一个介于 0.0（包含）和 1.0（不包含）之间的随机双精度浮点数。
     *
     * @return 随机数
     */
    public static double random() {
        return java.lang.Math.random();
    }

    /**
     * 返回指定弧度角的正弦值。
     *
     * @param a 弧度
     * @return 正弦值
     */
    public static double sin(double a) {
        return java.lang.Math.sin(a);
    }

    /**
     * 返回指定弧度角的余弦值。
     *
     * @param a 弧度
     * @return 余弦值
     */
    public static double cos(double a) {
        return java.lang.Math.cos(a);
    }

    /**
     * 返回指定弧度角的正切值。
     *
     * @param a 弧度
     * @return 正切值
     */
    public static double tan(double a) {
        return java.lang.Math.tan(a);
    }

    /**
     * 返回指定双精度浮点数的自然对数。
     *
     * @param a 正数
     * @return 自然对数
     */
    public static double log(double a) {
        return java.lang.Math.log(a);
    }

    /**
     * 返回指定双精度浮点数以 10 为底的对数。
     *
     * @param a 正数
     * @return 以 10 为底的对数
     */
    public static double log10(double a) {
        return java.lang.Math.log10(a);
    }

    /**
     * 返回 e 的指定幂次方。
     *
     * @param a 指数
     * @return e 的 a 次幂
     */
    public static double exp(double a) {
        return java.lang.Math.exp(a);
    }

    /**
     * 返回指定双精度浮点数的反正弦值。
     *
     * @param a 范围在 [-1, 1] 内的数值
     * @return 反正弦值（弧度）
     */
    public static double asin(double a) {
        return java.lang.Math.asin(a);
    }

    /**
     * 返回指定双精度浮点数的反余弦值。
     *
     * @param a 范围在 [-1, 1] 内的数值
     * @return 反余弦值（弧度）
     */
    public static double acos(double a) {
        return java.lang.Math.acos(a);
    }

    /**
     * 返回指定双精度浮点数的反正切值。
     *
     * @param a 任意数值
     * @return 反正切值（弧度）
     */
    public static double atan(double a) {
        return java.lang.Math.atan(a);
    }

    /**
     * 返回坐标点 (x, y) 和 x 轴的夹角。
     *
     * @param y 坐标点的 y 值
     * @param x 坐标点的 x 值
     * @return 夹角（弧度）
     */
    public static double atan2(double y, double x) {
        return java.lang.Math.atan2(y, x);
    }

    /**
     * 去掉小数部分，返回整数部分。
     *
     * @param a 任意双精度浮点数
     * @return 整数部分
     */
    public static double trunc(double a) {
        return java.lang.Math.floor(a);
    }

    /**
     * 获取当前日期时间。
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
    public static Date offsetWeek(Object date, int offset) {
        if (date == null) {
            return null;
        }
        Date dateTime = null;
        if (date instanceof Date) {
            dateTime = (Date) date;
        } else {
            dateTime = toDate(date);
        }
        return cn.hutool.core.date.DateUtil.offsetWeek(dateTime, offset);
    }

    /**
     * 日期偏移月数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移月数
     * @return 偏移后的日期
     */
    public static Date offsetMonth(Object date, int offset) {
        if (date == null) {
            return null;
        }
        Date dateTime = null;
        if (date instanceof Date) {
            dateTime = (Date) date;
        } else {
            dateTime = toDate(date);
        }
        return cn.hutool.core.date.DateUtil.offsetMonth(dateTime, offset);
    }

    /**
     * 日期偏移年数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移年数
     * @return 偏移后的日期
     */
    public static Date offsetYear(Object date, int offset) {
        if (date == null) {
            return null;
        }
        Date dateTime = null;
        if (date instanceof Date) {
            dateTime = (Date) date;
        } else {
            dateTime = toDate(date);
        }
        return cn.hutool.core.date.DateUtil.offset(dateTime, cn.hutool.core.date.DateField.YEAR, offset);
    }

    /**
     * 日期偏移天数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移天数
     * @return 偏移后的日期
     */
    public static Date offsetDay(Object date, int offset) {
        if (date == null) {
            return null;
        }
        Date dateTime = null;
        if (date instanceof Date) {
            dateTime = (Date) date;
        } else {
            dateTime = toDate(date);
        }
        return cn.hutool.core.date.DateUtil.offsetDay(dateTime, offset);
    }

    /**
     * 日期偏移小时数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移小时数
     * @return 偏移后的日期
     */
    public static Date offsetHour(Object date, int offset) {
        if (date == null) {
            return null;
        }
        Date dateTime = null;
        if (date instanceof Date) {
            dateTime = (Date) date;
        } else {
            dateTime = toDate(date);
        }
        return cn.hutool.core.date.DateUtil.offsetHour(dateTime, offset);
    }

    /**
     * 日期偏移分钟数
     * 如果 date 为空，则返回 null
     *
     * @param date   日期对象
     * @param offset 偏移分钟数
     * @return 偏移后的日期
     */
    public static Date offsetMinute(Object date, int offset) {
        if (date == null) {
            return null;
        }
        Date dateTime = null;
        if (date instanceof Date) {
            dateTime = (Date) date;
        } else {
            dateTime = toDate(date);
        }
        return cn.hutool.core.date.DateUtil.offsetMinute(dateTime, offset);
    }
}
