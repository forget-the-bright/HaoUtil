package org.hao.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类，提供一系列常用的字符串处理功能。
 *
 * <p>此类包含静态方法用于字符串格式化、检查占位符、转义特殊字符等功能。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2025/07/09 14:05
 */

public class StrUtil {
    // 正则表达式模式，用于匹配模板中的占位符 {arg}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(.*?)\\}");

    /**
     * 使用指定的模板和参数进行格式化字符串操作。
     *
     * <p>此方法通过替换模板中的占位符来构造最终的字符串。占位符用 {} 表示，</p>
     * <p> 其中可以是空的（{}）或带有格式化指令（如 {:format}）。</p>
     *
     * @param charSequence 字符序列，作为格式化模板
     * @param args         格式化参数，用于替换模板中的占位符
     * @return 格式化后的字符串，如果模板为null，则返回null
     */
    public static String formatM(CharSequence charSequence, Object... args) {
        // 如果模板为null，直接返回null
        if (charSequence == null) {
            return null;
        }
        String template = charSequence.toString();
        // 如果没有提供参数，直接返回模板字符串
        if (args == null || args.length == 0) {
            return template;
        }

        // 使用正则表达式匹配模板中的占位符
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        int placeholderCount = 0; // 用于记录占位符数量
        // 遍历模板中的所有占位符
        while (matcher.find()) {
            placeholderCount++; // 每找到一个就计数 +1
            String content = matcher.group(1);
            // 根据占位符的内容决定替换方式
            if (content.isEmpty()) {
                matcher.appendReplacement(sb, "%s");
            } else if (content.startsWith(":")) {
                String replacement = "%" + content.substring(1);
                matcher.appendReplacement(sb, replacement);
            } else {
                matcher.appendReplacement(sb, "%s");
            }
        }
        matcher.appendTail(sb);
        // ✅ 参数不足就补 null
        Object[] safeArgs = args;
        // 如果提供的参数不足以填充所有占位符，用null填充缺少的参数
        if (args.length < placeholderCount) {
            safeArgs = new Object[placeholderCount];
            System.arraycopy(args, 0, safeArgs, 0, args.length);
            // 剩下的位置填充 null
        }
        // 使用格式化字符串和安全的参数数组生成最终的字符串
        return String.format(sb.toString(), safeArgs);
    }

    /**
     * 根据给定的模板和参数，格式化字符串
     * 此方法通过替换模板中的占位符来构造最终的字符串模板中的占位符用{arg}表示，
     * 其中arg是参数的索引或带冒号的格式化指令
     *
     * @param charSequence 可字符序列，将被转换为模板字符串
     * @param args         参数数组，用于替换模板中的占位符
     * @return 格式化后的字符串，如果charSequence为null则返回null，如果args为空则返回原始模板
     */
    public static String format(CharSequence charSequence, Object... args) {
        // 如果输入的charSequence为null，则直接返回null
        if (charSequence == null) {
            return null;
        }
        String template = charSequence.toString();
        // 如果args为空或长度为0，则返回原始模板字符串
        if (args == null || args.length == 0) {
            return template;
        }

        // 使用正则表达式匹配模板中的占位符
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();

        int argCount = args.length;
        int placeholderCount = 0; // 用于记录占位符数量

        // 遍历模板中的所有占位符
        while (matcher.find()) {
            placeholderCount++; // 每找到一个就计数 +1
            String content = matcher.group(1);
            Object val = null;
            // 根据占位符的顺序获取对应的参数值
            if (placeholderCount <= argCount) {
                val = args[placeholderCount - 1];
            }
            // 根据占位符的内容是否为空或是否以冒号开头，进行不同的替换处理
            if (content.isEmpty()) {
                matcher.appendReplacement(sb, val == null ? "null" : val.toString());
            } else if (content.startsWith(":")) {
                // 对 replacement 做转义处理
                matcher.appendReplacement(sb, formatSingleArg(val, content.substring(1)));
            } else {
                matcher.appendReplacement(sb, val == null ? "null" : val.toString());
            }
        }

        // 将模板中剩余的部分添加到StringBuffer中
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * 根据模板和参数格式化字符串
     * 此方法允许在模板中使用占位符来表示参数，然后根据提供的参数进行替换
     * 占位符的格式为 {} 或 {:format}，其中 {} 表示按参数顺序替换，{:format} 表示按参数顺序并根据 format 格式化
     *
     * @param charSequence 要格式化的模板字符串，可以包含占位符
     * @param args         用于替换模板中占位符的参数数组
     * @return 格式化后的字符串，如果模板为 null，则返回 null
     */
    public static String formatFast(CharSequence charSequence, Object... args) {
        if (charSequence == null) {
            return null;
        }
        String template = charSequence.toString();
        if (args == null || args.length == 0) {
            return template;
        }
        int argCount = args.length;
        int templateLength = template.length();
        StringBuilder result = new StringBuilder();
        int argIndex = 0;
        for (int i = 0; i < templateLength; i++) {
            char c = template.charAt(i);
            if (c == '{') {
                // 判断是否为占位符开始
                if (i + 1 < templateLength && template.charAt(i + 1) == '{') {
                    result.append('{');
                    i++; // 跳过第二个{
                    continue;
                }

                // 从下标 i 开始处查找结束 }
                int end = template.indexOf('}', i);
                if (end == -1) {
                    //  未找到结束 } 直接拼接剩余的字符串 跳出循环
                    //  throw new IllegalArgumentException("Unclosed placeholder starting at index " + i);
                    result.append(template.substring(i, templateLength));
                    break;
                }
                // 截取占位符内容 "{:xxx}" = ":xxx", "{}" = ""
                String content = template.substring(i + 1, end);
                // 移动指针到 }
                i = end;

                if (content.isEmpty()) {
                    // 处理 {}
                    int index = argIndex++;
                    Object val = null;
                    if (index + 1 <= argCount) {
                        val = args[index];
                    }
                    result.append(val);
                } else if (content.startsWith(":")) {
                    // 处理 {:xxx}，使用 String.format()
                    int index = argIndex++;
                    Object val = null;
                    if (index + 1 <= argCount) {
                        val = args[index];
                    }
                    result.append(formatSingleArg(val, content.substring(1)));
                } else {
                    // throw new IllegalArgumentException("Unsupported placeholder: {" + content + "}");
                    result.append("{").append(content).append("}");
                }
                continue;
            }
            result.append(c);
        }

        return result.toString();
    }

    /**
     * 根据指定的格式规范格式化单个参数
     * <p>
     * 此方法尝试将给定的对象按照指定的格式规范进行格式化如果对象为空，则直接返回空值
     * 在格式化过程中，如果遇到异常（例如：对象类型不适用于给定的格式规范），则默认将对象转换为字符串或原样输出
     *
     * @param obj        要格式化的对象，可以是任何类型
     * @param formatSpec 格式规范字符串，描述如何格式化对象
     * @return 格式化后的字符串，如果输入为null或格式化失败则返回null或对象的字符串表示
     */
    private static String formatSingleArg(Object obj, String formatSpec) {
        // 检查输入对象是否为null
        if (obj == null) {
            // 如果对象为null，直接返回null
            return null;
        }
        try {
            // 尝试按照指定的格式规范格式化对象
            return String.format("%" + formatSpec, obj);
        } catch (Exception e) {
            // 如果格式化过程中出现异常，将对象默认转换为字符串或原样输出
            // 这种情况下，使用对象的toString方法或直接输出对象的字符串表示
            return obj.toString();
        }
    }

    /**
     * 判断给定的数字对象是否为整数类型
     * <p>
     * 此方法通过检查对象的类型来确定它是否属于整数类型整数类型包括Byte、Short、Integer和Long
     * 这种类型检查是通过Java的instanceof关键字实现的，它可以安全地检查对象是否属于指定的类型或其子类型
     *
     * @param number 要进行类型检查的数字对象
     * @return 如果数字对象是整数类型，则返回true；否则返回false
     */
    public static boolean isIntegerType(Number number) {
        return number instanceof Byte ||
                number instanceof Short ||
                number instanceof Integer ||
                number instanceof Long;
    }

    /**
     * 判断给定的数字是否是浮点类型
     *
     * @param number 要检查的数字对象
     * @return 如果数字是浮点类型（Float或Double），则返回true；否则返回false
     */
    public static boolean isFloatingPointType(Number number) {
        // 检查数字是否为Float或Double类型，从而确定是否为浮点类型
        return number instanceof Float || number instanceof Double;
    }

    /**
     * ✅安全地转义 replacement 字符串中的 $ 和 \
     * <p>
     * 在进行字符串替换操作时，确保 $ 和 \ 被正确处理，不会导致意外的行为或安全问题
     * $ 被转义为 \$, \ 被转义为 \\，这样它们就不会被解释为正则表达式中的特殊字符
     *
     * @param s 待转义的字符串
     * @return 转义后的字符串
     */
    private static String escapeForReplacement(String s) {
        return s.replace("\\", "\\\\$").replace("$", "\\$");
    }
}
