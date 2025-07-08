package org.hao.core.print;

import cn.hutool.core.util.StrUtil;

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
    RED {
        public Integer getColor() {
            return 31;
        }
    },
    GREEN {
        public Integer getColor() {
            return 32;
        }
    },
    YELLOW {
        public Integer getColor() {
            return 33;
        }
    },
    BLUE {
        public Integer getColor() {
            return 34;
        }
    },
    PURPULE {
        public Integer getColor() {
            return 35;
        }
    },
    CYAN {
        public Integer getColor() {
            return 36;
        }
    },
    WHITE {
        public Integer getColor() {
            return 37;
        }
    },
    BLACK {
        public Integer getColor() {
            return 30;
        }
    };

    public Integer getColor() {
        throw new AbstractMethodError();
    }

    public void Println(CharSequence template, Object... params) {
        String format = StrUtil.format(template, params);
        printSingleColor(getColor(), 2, format);
    }

    public void Println(CharSequence template, PrintUtil background, Object... params) {
        String format = StrUtil.format(template, params);
        printSingleColor(getColor(), background.getColor() + 10, 2, format);
    }

    public String getColorStr(CharSequence template, Object... params) {
        String format = StrUtil.format(template, params);
        return getColorString(getColor(), 2, format);
    }

    public String getColorStr(CharSequence template, PrintUtil background, Object... params) {
        String format = StrUtil.format(template, params);
        return getColorString(getColor(), background.getColor() + 10, 2, format);
    }

    /**
     * @param code    颜色代号：背景颜色代号(41-46)；前景色代号(31-36)
     * @param n       数字+m：1加粗；3斜体；4下划线
     * @param content 要打印的内容
     *                格式：System.out.println("\33[前景色代号;背景色代号;数字m")
     *                %s是字符串占位符，%d 是数字占位符
     */
    private void printSingleColor(int code, int n, String content) {
        System.out.format("\33[%d;%dm%s\r\n", code, n, content + "\33[0;39m");
    }

    private void printSingleColor(int code, int backCode, int n, String content) {
        System.out.format("\33[%d;%d;%dm%s\r\n", code, backCode, n, content + "\33[0;39m");
    }

    private String getColorString(int code, int n, String content) {
        return String.format("\33[%d;%dm%s\33[0;39m", code, n, content);
    }

    private String getColorString(int code, int backCode, int n, String content) {
        return String.format("\33[%d;%d;%dm%s\33[0;39m", code, backCode, n, content);
    }

}
