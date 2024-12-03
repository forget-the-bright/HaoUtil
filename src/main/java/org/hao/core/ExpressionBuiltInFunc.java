package org.hao.core;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Date;

public class ExpressionBuiltInFunc {

    //相加
    public static double add(double a, double b) {
        return a + b;
    }

    //相减
    public static double sub(double a, double b) {
        return a - b;
    }

    //打印
    public static void print(Object obj) {
        System.out.println(obj);
    }

    public static double ceil(double a) {
        return java.lang.Math.ceil(a); // 使用 java.lang.Math.ceil() 来向上取整
    }

    // 2. 向下取整
    public static double floor(double a) {
        return java.lang.Math.floor(a); // 使用 java.lang.Math.floor() 来向下取整
    }

    // 3. 四舍五入
    public static long round(double a) {
        return java.lang.Math.round(a); // 使用 java.lang.Math.round() 进行四舍五入
    }

    // 4. 获取最大值
    public static int max(int a, int b) {
        return java.lang.Math.max(a, b); // 使用 java.lang.Math.max() 获取最大值
    }

    // 5. 获取最小值
    public static int min(int a, int b) {
        return java.lang.Math.min(a, b); // 使用 java.lang.Math.min() 获取最小值
    }

    // 6. 求幂 (base^exp)
    public static double power(double base, double exp) {
        return java.lang.Math.pow(base, exp); // 使用 java.lang.Math.pow() 计算 base 的 exp 次幂
    }

    // 7. 求平方根
    public static double sqrt(double a) {
        return java.lang.Math.sqrt(a); // 使用 java.lang.Math.sqrt() 来计算平方根
    }

    // 8. 获取随机数
    public static double random() {
        return java.lang.Math.random(); // 使用 java.lang.Math.random() 生成随机数
    }

    // 9. 计算正弦
    public static double sin(double a) {
        return java.lang.Math.sin(a); // 使用 java.lang.Math.sin() 计算正弦（弧度制）
    }

    // 10. 计算余弦
    public static double cos(double a) {
        return java.lang.Math.cos(a); // 使用 java.lang.Math.cos() 计算余弦（弧度制）
    }

    // 11. 计算正切
    public static double tan(double a) {
        return java.lang.Math.tan(a); // 使用 java.lang.Math.tan() 计算正切（弧度制）
    }

    // 12. 求自然对数
    public static double log(double a) {
        return java.lang.Math.log(a); // 使用 java.lang.Math.log() 计算自然对数
    }

    // 13. 求以10为底的对数
    public static double log10(double a) {
        return java.lang.Math.log10(a); // 使用 java.lang.Math.log10() 计算以 10 为底的对数
    }

    // 14. 求指数 (e^x)
    public static double exp(double a) {
        return java.lang.Math.exp(a); // 使用 java.lang.Math.exp() 计算 e^x
    }

    // 15. 求反正弦
    public static double asin(double a) {
        return java.lang.Math.asin(a); // 使用 java.lang.Math.asin() 计算反正弦（弧度制）
    }

    // 16. 求反余弦
    public static double acos(double a) {
        return java.lang.Math.acos(a); // 使用 java.lang.Math.acos() 计算反余弦（弧度制）
    }

    // 17. 求反正切
    public static double atan(double a) {
        return java.lang.Math.atan(a); // 使用 java.lang.Math.atan() 计算反正切（弧度制）
    }

    // 18. 求坐标点 (x, y) 和 x 轴的夹角
    public static double atan2(double y, double x) {
        return java.lang.Math.atan2(y, x); // 使用 java.lang.Math.atan2() 计算坐标 (x, y) 和 x 轴的夹角（弧度制）
    }

    // 19. 去掉小数部分
    public static double trunc(double a) {
        return java.lang.Math.floor(a); // 使用 java.lang.Math.floor() 去掉小数部分
    }


    public static Date now() {
        return new java.util.Date();
    }

    public static Date toDate(Object date) {
        if (ObjectUtil.isEmpty(date)) {
            return new cn.hutool.core.date.DateTime();
        }
        if (date instanceof Date) {
            return (Date) date;
        }
        return new cn.hutool.core.date.DateTime(date.toString());
    }

    public static String dateTime(Date date) {
        if (date == null) {
            return cn.hutool.core.date.DateUtil.formatDateTime(now());
        }
        return cn.hutool.core.date.DateUtil.formatDateTime(date);
    }

    public static Date offsetWeek(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offsetWeek(date, offset);
    }

    public static Date offsetMonth(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offsetMonth(date, offset);
    }

    public static Date offsetYear(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offset(date, cn.hutool.core.date.DateField.YEAR, offset);
    }

    public static Date offsetDay(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offsetDay(date, offset);
    }

    public static Date offsetHour(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offsetHour(date, offset);
    }

    public static Date offsetMinute(Date date, int offset) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.offsetMinute(date, offset);
    }
}

