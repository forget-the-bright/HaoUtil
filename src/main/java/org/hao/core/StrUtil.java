package org.hao.core;

import org.aspectj.weaver.ast.Var;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StrUtil {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(.*?)\\}");

    public static String format(CharSequence charSequence, Object... args) {
        if (charSequence == null || args == null || args.length == 0) {
            return charSequence != null ? charSequence.toString() : null;
        }
        String template = charSequence.toString();
        int argCount = args.length;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        int placeholderCount = 0; // 用于记录占位符数量
        while (matcher.find()) {
            placeholderCount++; // 每找到一个就计数 +1
            String content = matcher.group(1);
            Object val = null;
            if (placeholderCount <= argCount) {
                val = args[placeholderCount - 1];
            }
            if (content.isEmpty()) {
                matcher.appendReplacement(sb, val == null ? "null" : val.toString());
            } else if (content.startsWith(":")) {
                String replacement = "%" + content.substring(1);
                // 对 replacement 做转义处理
                try {
                    matcher.appendReplacement(sb, String.format(replacement, val));
                } catch (Exception e) {
                    matcher.appendReplacement(sb, val == null ? "null" : val.toString());
                }
            } else {
                //  matcher.appendReplacement(sb, "%s");
                matcher.appendReplacement(sb, val == null ? "null" : val.toString());
            }
        }
        matcher.appendTail(sb);
        //return String.format(sb.toString(), safeArgs);.
        return sb.toString();
    }

    // ✅ 安全地转义 replacement 字符串中的 $ 和 \
    private static String escapeForReplacement(String s) {
        return s.replace("\\", "\\\\$").replace("$", "\\$");
    }


    public static String formatFast(String template, Object... args) {
        if (template == null || args == null || args.length == 0) {
            return template;
        }
        int argCount = args.length;

        StringBuilder result = new StringBuilder();
        int argIndex = 0;

        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);

            if (c == '{') {
                // 判断是否为占位符开始
                if (i + 1 < template.length() && template.charAt(i + 1) == '{') {
                    result.append('{');
                    i++; // 跳过第二个{
                    continue;
                }

                // 查找结束 }
                int end = template.indexOf('}', i);
                if (end == -1) {
                    throw new IllegalArgumentException("Unclosed placeholder starting at index " + i);
                }

                String content = template.substring(i + 1, end);
                i = end; // 移动指针到 }

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
                    throw new IllegalArgumentException("Unsupported placeholder: {" + content + "}");
                }

            } else if (c == '}') {
                if (i + 1 < template.length() && template.charAt(i + 1) == '}') {
                    result.append('}');
                    i++;
                } else {
                    throw new IllegalArgumentException("Unmatched '}' at index " + i);
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private static String formatSingleArg(Object obj, String formatSpec) {
        if (obj == null) {
            return null;
        }
        // 默认按字符串处理或原样输出
        return String.format("%" + formatSpec, obj);
    }

    public static boolean isIntegerType(Number number) {
        return number instanceof Byte ||
                number instanceof Short ||
                number instanceof Integer ||
                number instanceof Long;
    }

    public static boolean isFloatingPointType(Number number) {
        return number instanceof Float || number instanceof Double;
    }
}
