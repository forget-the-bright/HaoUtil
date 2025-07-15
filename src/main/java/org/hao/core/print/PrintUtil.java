package org.hao.core.print;


import org.hao.core.StrUtil;

/**
 * 控制台彩色打印工具类，通过 ANSI 转义序列实现不同颜色和样式的文本输出。
 *
 * <p>该枚举定义了常用前景色，并提供打印带颜色文本、生成带颜色字符串的方法，
 * 支持设置背景色和文本样式（如加粗、斜体、下划线）。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2024/5/30
 */

public enum PrintUtil {
    /**
     * 默认颜色，通常映射到终端默认前景色（通常是白色或浅灰色）。
     */
    DEFAULT {
        public Integer getColor() {
            return 0;
        }
    },

    /**
     * 黑色文本颜色。
     */
    BLACK {
        public Integer getColor() {
            return 30;
        }
    },

    /**
     * 红色文本颜色。
     */
    RED {
        public Integer getColor() {
            return 31;
        }
    },

    /**
     * 绿色文本颜色。
     */
    GREEN {
        public Integer getColor() {
            return 32;
        }
    },

    /**
     * 黄色文本颜色。
     */
    YELLOW {
        public Integer getColor() {
            return 33;
        }
    },

    /**
     * 蓝色文本颜色。
     */
    BLUE {
        public Integer getColor() {
            return 34;
        }
    },

    /**
     * 紫色文本颜色。
     */
    PURPULE {
        public Integer getColor() {
            return 35;
        }
    },

    /**
     * 青色文本颜色。
     */
    CYAN {
        public Integer getColor() {
            return 36;
        }
    },

    /**
     * 白色文本颜色。
     */
    WHITE {
        public Integer getColor() {
            return 37;
        }
    },


    /**
     * 高亮黑色文本颜色，也称为亮黑色。
     */
    BRIGHT_BLACK {
        public Integer getColor() {
            return 90;
        }
    },

    /**
     * 高亮红色文本颜色，比标准红色更鲜艳。
     */
    BRIGHT_RED {
        public Integer getColor() {
            return 91;
        }
    },

    /**
     * 高亮绿色文本颜色，比标准绿色更鲜艳。
     */
    BRIGHT_GREEN {
        public Integer getColor() {
            return 92;
        }
    },

    /**
     * 高亮黄色文本颜色，比标准黄色更鲜艳。
     */
    BRIGHT_YELLOW {
        public Integer getColor() {
            return 93;
        }
    },

    /**
     * 高亮蓝色文本颜色，比标准蓝色更鲜艳。
     */
    BRIGHT_BLUE {
        public Integer getColor() {
            return 94;
        }
    },

    /**
     * 高亮紫色文本颜色，比标准紫色更鲜艳。
     */
    BRIGHT_PURPLE {
        public Integer getColor() {
            return 95;
        }
    },

    /**
     * 高亮青色文本颜色，比标准青色更鲜艳。
     */
    BRIGHT_CYAN {
        public Integer getColor() {
            return 96;
        }
    },

    /**
     * 高亮白色文本颜色，通常显示为非常明亮的白色。
     */
    BRIGHT_WHITE {
        public Integer getColor() {
            return 97;
        }
    };

    public Integer getColor() {
        throw new AbstractMethodError();
    }

    //region 彩色控制台打印方法

    /**
     * 打印带颜色的文本，使用当前枚举常量的颜色，并重置背景色和字体样式。
     *
     * @param template 模板字符串，支持格式化参数
     * @param params   格式化参数列表
     */
    public void Println(CharSequence template, Object... params) {
        printSingleColor(template, this, BackColorSytle.RESET, FontSytle.RESET, params);
    }

    /**
     * 打印带颜色和指定背景色的文本，字体样式重置为默认。
     *
     * @param template   模板字符串，支持格式化参数
     * @param background 背景色样式，参考 {@link BackColorSytle}
     * @param params     格式化参数列表
     */
    public void Println(CharSequence template, BackColorSytle background, Object... params) {
        printSingleColor(template, this, background, FontSytle.RESET, params);
    }

    /**
     * 打印带颜色并应用指定字体样式的文本，背景色重置为默认。
     *
     * @param template  模板字符串，支持格式化参数
     * @param fontSytle 应用的字体样式，如加粗、斜体等，参考 {@link FontSytle}
     * @param params    格式化参数列表
     */
    public void Println(CharSequence template, FontSytle fontSytle, Object... params) {
        printSingleColor(template, this, BackColorSytle.RESET, fontSytle, params);
    }

    /**
     * 打印带有指定颜色、背景色和字体样式的文本。
     *
     * @param template   模板字符串，支持格式化参数
     * @param background 背景色样式，参考 {@link BackColorSytle}
     * @param fontSytle  字体样式，如加粗、斜体等，参考 {@link FontSytle}
     * @param params     格式化参数列表
     */
    public void Println(CharSequence template, BackColorSytle background, FontSytle fontSytle, Object... params) {
        printSingleColor(template, this, background, fontSytle, params);
    }
    //endregion

    //region 获取带颜色字符串方法

    /**
     * 生成一个带颜色的字符串，使用当前枚举常量的颜色，并重置背景色和字体样式。
     *
     * @param template 模板字符串，支持格式化参数
     * @param params   格式化参数列表
     * @return 带 ANSI 颜色控制码的字符串
     */
    public String getColorStr(CharSequence template, Object... params) {
        return getColorString(template, this, BackColorSytle.RESET, FontSytle.RESET, params);
    }

    /**
     * 生成一个带颜色和指定背景色的字符串，字体样式重置为默认。
     *
     * @param template   模板字符串，支持格式化参数
     * @param background 背景色样式，参考 {@link BackColorSytle}
     * @param params     格式化参数列表
     * @return 带 ANSI 颜色和背景色控制码的字符串
     */
    public String getColorStr(CharSequence template, BackColorSytle background, Object... params) {
        return getColorString(template, this, background, FontSytle.RESET, params);
    }

    /**
     * 生成一个带颜色并应用指定字体样式的字符串，背景色重置为默认。
     *
     * @param template  模板字符串，支持格式化参数
     * @param fontSytle 应用的字体样式，参考 {@link FontSytle}
     * @param params    格式化参数列表
     * @return 带 ANSI 颜色和字体样式控制码的字符串
     */
    public String getColorStr(CharSequence template, FontSytle fontSytle, Object... params) {
        return getColorString(template, this, BackColorSytle.RESET, fontSytle, params);
    }

    /**
     * 生成一个带有指定颜色、背景色和字体样式的字符串。
     *
     * @param template   模板字符串，支持格式化参数
     * @param background 背景色样式，参考 {@link BackColorSytle}
     * @param fontSytle  字体样式，参考 {@link FontSytle}
     * @param params     格式化参数列表
     * @return 带 ANSI 颜色、背景色和样式控制码的字符串
     */
    public String getColorStr(CharSequence template, BackColorSytle background, FontSytle fontSytle, Object... params) {
        return getColorString(template, this, background, fontSytle, params);
    }
    //endregion

    //region 获取带颜色字符串静态方法

    /**
     * 打印单色加背景色内容
     *
     * @param template      模板字符串，支持格式化参数
     * @param fontColorCode 文本颜色代码（ANSI 颜色代码，如 30-37 或 90-97）
     * @param backColorCode 背景颜色代码（ANSI 背景色代码 如 40-47 或 100-107）
     * @param fontStyle     字体样式代码，如 1（加粗）、3（斜体）、4（下划线）
     * @param params        格式化参数列表
     */
    public static void printSingleColor(CharSequence template, PrintUtil fontColorCode, BackColorSytle backColorCode, FontSytle fontStyle, Object... params) {
        String colorString = getColorString(template, fontColorCode, backColorCode, fontStyle, params);
        System.out.println(colorString);
    }

    /**
     * 打印单色加背景色内容
     *
     * @param fontColorCode 文本颜色代码（ANSI 颜色代码，如 30-37 或 90-97）
     * @param backColorCode 背景颜色代码（ANSI 背景色代码 如 40-47 或 100-107）
     * @param fontStyle     字体样式代码，如 1（加粗）、3（斜体）、4（下划线）
     * @param content       要打印的内容
     */
    public static void printSingleColor(PrintUtil fontColorCode, BackColorSytle backColorCode, FontSytle fontStyle, String content) {
        String colorString = getColorString(fontColorCode, backColorCode, fontStyle, content);
        System.out.println(colorString);
    }

    /**
     * 生成带有指定文本颜色、背景颜色和样式的字符串。
     *
     * @param template      模板字符串，支持格式化参数
     * @param fontColorCode 文本颜色代码（ANSI 颜色代码，如 30-37 或 90-97）
     * @param backColorCode 背景颜色代码（ANSI 背景色代码 如 40-47 或 100-107）
     * @param fontStyle     字体样式代码，如 1（加粗）、3（斜体）、4（下划线）
     * @param params        格式化参数列表
     * @return 包含 ANSI 颜色、背景色和样式控制码的字符串
     */
    public static String getColorString(CharSequence template, PrintUtil fontColorCode, BackColorSytle backColorCode, FontSytle fontStyle, Object... params) {
        String format;
        if (params.length == 0) {
            format = template.toString();
        } else {
            format = StrUtil.formatFast(template, params);
        }
        return getColorString(fontColorCode, backColorCode, fontStyle, format);
    }



    /**
     * <p>生成带有指定文本颜色、背景颜色和样式的字符串。
     *
     * <p> \033 是八进制表示的 ESC 控制字符（ASCII 27），用于启动 ANSI 转义序列
     * <p> \33 同样代表 ESC 控制字符，但它是用八进制表示的。虽然在这个情况下它也能工作（因为 \33 等价于 \033），
     * <p> 但是通常更推荐使用 \033 来避免混淆。这是因为不同的系统或环境中对转义序列的理解可能不同，明确使用 \033 可以减少出错的可能性。
     * <p> 在 PowerShell (pwsh) 中，由于其语法与 Unix shell 不同  使用 `e 来表示 ESC 字符，而不是使用八进制或十六进制编码。这使得 PowerShell 脚本在这种情况下更加直观和易读。
     *
     * @param fontColorCode 文本颜色代码（ANSI 颜色代码，如 30-37 或 90-97）
     * @param backColorCode 背景颜色代码（ANSI 背景色代码 如 40-47 或 100-107）
     * @param fontStyle     字体样式代码，如 1（加粗）、3（斜体）、4（下划线）
     * @param content       要包裹颜色样式的文本内容
     * @return 包含 ANSI 颜色、背景色和样式控制码的字符串
     */
    public static String getColorString(PrintUtil fontColorCode, BackColorSytle backColorCode, FontSytle fontStyle, String content) {
        if (fontColorCode == null) {
            fontColorCode = PrintUtil.DEFAULT;
        }
        StringBuilder formatStr = new StringBuilder();
        formatStr.append("\033[");
        formatStr.append(fontColorCode.getColor());
        if (backColorCode != null && backColorCode != BackColorSytle.RESET) {
            formatStr.append(';').append(backColorCode.getCode());
        }
        if (fontStyle != null && fontStyle != FontSytle.RESET) {
            formatStr.append(';').append(fontStyle.getCode());
        }
        formatStr.append('m');
        formatStr.append(content);
        formatStr.append("\033[0m");
        //  return String.format("\33[%d;%d;%dm%s\33[0m", fontColorCode, backColorCode.getCode(), fontStyle.getCode(), content);
        return formatStr.toString();
    }
    //endregion

}
