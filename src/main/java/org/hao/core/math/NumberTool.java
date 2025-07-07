package org.hao.core.math;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

/**
 * TODO
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/7/7 11:09
 */
public class NumberTool {

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
        // 将数字转换为字符串（避免科学计数法）
        String numberStr = NumberUtil.toBigDecimal(number).toPlainString();

        // 分割整数部分和小数部分
        if (!StrUtil.contains(numberStr, '.')) {
            // 没有小数部分，直接返回原值或补零
            return numberStr; //StrUtil.padAfter("", scale, "0")
        }
        String[] parts = StrUtil.splitToArray(numberStr, '.');
        String integerPart = parts[0];
        String decimalPart = parts[1];

        int decimalPartLength = decimalPart.length();
        // 如果scale大于小数部分的长度，则直接返回原字符串
        if (scale > decimalPartLength) {
            return numberStr;
        }

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
        String resultStr = decimalPart.substring(0, subLength);
        resultStr = StrUtil.trim(resultStr, 1, character -> character == '0');
        return integerPart + "." + resultStr;
    }
}
