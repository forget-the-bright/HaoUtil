package org.hao.core.math;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 数字工具类，提供数字字符串处理相关功能
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/7/7 11:09
 */
public class NumberUtils {

    /**
     * 保留数字字符串中的指定位数的有效小数。
     * 如果数字字符串不是有效的数字，则直接返回原字符串。
     * 如果数字字符串中没有小数部分，则在末尾补零以确保至少有指定的小数位数。
     *
     * @param number 要处理的数字字符串
     * @param scale  保留的有效小数位数
     * @return 处理后的数字字符串，保留指定位数的有效小数
     */
    public static String retainSignificantDecimals(String number, Integer scale) {
        if (ObjectUtil.isEmpty(scale) || scale <= 0) return number; // 如果scale小于等于0，则直接返回原字符串
        if (StrUtil.isEmpty(number) || !NumberUtil.isNumber(number)) return number; // 如果number不是数字，则直接返回原字符串

        // 将数字转换为普通字符串格式（避免科学计数法）
        String numberStr = NumberUtil.toBigDecimal(number).toPlainString();

        // 分割整数部分和小数部分
        if (!StrUtil.contains(numberStr, '.')) {
            // 没有小数部分，直接返回原值或补零
            return numberStr; //StrUtil.padAfter("", scale, "0")
        }

        // 分割数字字符串为整数部分和小数部分
        String[] parts = StrUtil.splitToArray(numberStr, '.');
        String integerPart = parts[0];
        String decimalPart = parts[1];

        int decimalPartLength = decimalPart.length();
        // 如果scale大于小数部分的长度，则直接返回原字符串
        if (scale > decimalPartLength) {
            return numberStr;
        }

        // 查找第一个非零字符的位置
        int lastSignificantIndex = -1;

        for (int i = 0; i < decimalPartLength; i++) {
            if (decimalPart.charAt(i) != '0') {
                lastSignificantIndex = i;
                break;
            }
        }
        // 如果没有有效数字，返回整数部分
        if (lastSignificantIndex == -1) {
            return integerPart; //StrUtil.padAfter("", scale, "0")
        }
        // 如果找到了有效数字，则截取到该位置+scale位,保留有效位数
        int subLength = lastSignificantIndex + scale;
        if (subLength > decimalPartLength) {//如果小数部分长度小于截取长度，则直接返回原字符串
            return numberStr;
        }

        // 截取指定长度的小数部分并去除末尾的零
        String resultStr = decimalPart.substring(0, subLength);
        resultStr = StrUtil.trim(resultStr, 1, character -> character == '0');
        return integerPart + "." + resultStr;
    }


    //region 数字相关

    /**
     * 对double类型数值进行四舍五入操作
     * 此方法旨在处理需要对浮点数进行精确舍入的场景，由于double类型的精度问题，
     * 使用BigDecimal来确保舍入操作的精确性
     *
     * @param v     待舍入的double类型数值
     * @param scale 舍入后的规模（小数点后的位数）
     * @return 舍入后的BigDecimal对象
     */
    public static BigDecimal round(double v, int scale) {
        // 当数值大于等于1.0时，直接使用setScale方法进行四舍五入
        if (v >= 1.0) {
            return new BigDecimal(v).setScale(scale, RoundingMode.HALF_UP);
        }
        // 当数值小于1.0时，使用round方法结合MathContext进行四舍五入，以保持有效数字的精度
        return new BigDecimal(v).round(new MathContext(scale, RoundingMode.HALF_UP));
    }


    /**
     * 对指定数值进行四舍五入操作
     *
     * @param v     待四舍五入的数值，以字符串形式表示，以支持精确计算
     * @param scale 小数点后的位数，表示需要保留的小数位
     * @return 四舍五入后的结果，以BigDecimal形式返回
     */
    public static BigDecimal round(String v, int scale) {
        // 创建BigDecimal对象，用于进行精确计算
        BigDecimal bigDecimal = new BigDecimal(v);
        // 判断数值是否大于等于1.0，以决定使用setScale还是round方法
        if (bigDecimal.doubleValue() >= 1.0) {
            // 如果大于等于1.0，使用setScale方法设置小数点后的位数，并进行四舍五入
            return bigDecimal.setScale(scale, RoundingMode.HALF_UP);
        }  // 如果小于1.0，使用round方法进行四舍五入，并指定精度和四舍五入模式
        return bigDecimal.round(new MathContext(scale, RoundingMode.HALF_UP));
    }


    /**
     * 将双精度浮点数四舍五入到指定小数位数，并以字符串形式返回
     * 此方法旨在处理不同情况下的双精度浮点数，包括大于等于1、等于0、不是数字（NaN）以及其他情况
     * 对于大于等于1的情况，直接使用setScale方法进行四舍五入
     * 对于等于0或不是数字的情况，直接返回"0.0"
     * 对于其他情况，使用MathContext进行四舍五入
     *
     * @param v     待处理的双精度浮点数
     * @param scale 小数点后的位数
     * @return 四舍五入后的字符串表示的数字
     */
    public static String roundStr(Double v, int scale) {
        // 保留有效数字两位，四舍五入
        if (v >= 1.0) {
            // 对于大于等于1的数字，直接设置小数点后的位数并四舍五入
            return new BigDecimal(v.toString()).setScale(scale, RoundingMode.HALF_UP).toString();
        } else if (v == 0.0) {
            // 对于等于0的情况，直接返回"0.0"
            return "0.0";
        } else if (Double.isNaN(v)) {
            // 对于不是数字的情况，也返回"0.0"
            return "0.0";
        }
        // 对于其他情况，使用MathContext进行四舍五入
        return new BigDecimal(v.toString()).round(new MathContext(scale, RoundingMode.HALF_UP)).toPlainString();
    }


    /**
     * 将给定的数值字符串四舍五入到指定的小数位数
     * 如果数值大于等于1，则直接四舍五入并返回结果
     * 如果数值等于0，则返回"0.0"，以确保至少有一位小数
     * 如果数值小于1且不等于0，则使用指定的有效数字位数进行四舍五入
     *
     * @param v     待处理的数值字符串
     * @param scale 小数位数或有效数字位数
     * @return 四舍五入后的数值字符串
     */
    public static String roundStr(String v, int scale) {
        // 创建BigDecimal对象以进行精确计算
        BigDecimal bigDecimal = new BigDecimal(v);
        // 当数值大于等于1时，直接四舍五入到指定小数位数
        if (bigDecimal.doubleValue() >= 1.0) {
            return bigDecimal.setScale(scale, RoundingMode.HALF_UP).toString();
        } else if (bigDecimal.doubleValue() == 0.0) {
            // 当数值等于0时，确保返回的字符串至少有一位小数
            return "0.0";
        }
        // 当数值小于1且不等于0时，按有效数字位数进行四舍五入
        return bigDecimal.round(new MathContext(scale, RoundingMode.HALF_UP)).toPlainString();
    }


    /**
     * 将double类型数值转换为字符串表示，特别处理特定数值情况
     *
     * @param v 待转换的double类型数值
     * @return 转换后的字符串表示，对于特殊值有特定处理
     */
    public static String doubleStr(double v) {
        // 当输入值为0.0时，直接返回"0.0"，避免因浮点数精度问题导致的不必要转换
        if (v == 0.0) {
            return "0.0";
            // 当输入值为非数字（NaN）时，返回"0.0"，统一处理无效数值输入
        } else if (Double.isNaN(v)) {
            return "0.0";
        }
        // 对于有效输入，使用BigDecimal进行转换和四舍五入，然后移除不必要的尾随零
        // 这样做可以得到一个精确且格式化的字符串表示
        return removeTrailingZeros(new BigDecimal(v).setScale(10, RoundingMode.HALF_UP).toString());
    }


    /**
     * 将字符串表示的数字转换为double类型，并格式化为字符串
     * 此方法主要用于处理财务计算等场景，需要高精度和正确舍入
     *
     * @param v 输入的字符串表示的数字
     * @return 格式化后的字符串表示的数字
     */
    public static String doubleStr(String v) {
        // 创建BigDecimal对象，并设置小数点后10位，使用四舍五入模式
        BigDecimal bigDecimal = new BigDecimal(v).setScale(10, RoundingMode.HALF_UP);

        // 如果值为0.0，返回"0.0"字符串，保证返回结果的一致性
        if (bigDecimal.doubleValue() == 0.0) {
            return "0.0";
        }

        // 移除BigDecimal.toString()产生的尾随零，简化输出格式
        return removeTrailingZeros(bigDecimal.toString());
    }


    /**
     * 移除字符串末尾的零
     * 该方法用于处理字符串形式的数值，移除其末尾的零，直到遇到非零字符或小数点
     * 如果字符串以小数点结尾，也会移除小数点
     *
     * @param value 待处理的字符串数值
     * @return 移除末尾零和可能的小数点后的字符串数值
     */
    public static String removeTrailingZeros(String value) {
        // 检查字符串中是否包含小数点，以确定是否需要进行处理
        if (value.contains(".")) {
            // 循环移除字符串末尾的零
            while (value.endsWith("0")) {
                value = value.substring(0, value.length() - 1);
            }
            // 如果字符串以小数点结尾，则移除小数点
            if (value.endsWith(".")) {
                value = value.substring(0, value.length() - 1);
            }
        }
        // 返回处理后的字符串
        return value;
    }
    //endregion
}
