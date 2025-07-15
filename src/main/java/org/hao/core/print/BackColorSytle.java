package org.hao.core.print;

/**
 * 表示背景颜色样式的枚举类，定义了常见的终端背景色选项。
 * <p>
 * 每个颜色对应一个整型值（code），通常用于控制台或终端中设置文本背景颜色。
 * 颜色代码遵循 ANSI 转义码标准，可能在部分终端环境中不被支持。
 * </p>
 *
 * @author wanghao (helloworlwh @ 163.com)
 * @since 2025/7/15 11:53
 */
public enum BackColorSytle {

    /**
     * 重置背景色到默认状态。
     */
    RESET(0),

    /**
     * 黑色背景。
     */
    BLACK(40),

    /**
     * 红色背景。
     */
    RED(41),

    /**
     * 绿色背景。
     */
    GREEN(42),

    /**
     * 黄色背景。
     */
    YELLOW(43),

    /**
     * 蓝色背景。
     */
    BLUE(44),

    /**
     * 紫色背景。
     */
    PURPULE(45),

    /**
     * 青色背景。
     */
    CYAN(46),

    /**
     * 白色背景。
     */
    WHITE(47),
    /**
     * 高亮黑色背景。
     */
    BRIGHT_BLACK(100),

    /**
     * 高亮红色背景，通常显示为更鲜艳或明亮的红色。
     */
    BRIGHT_RED(101),

    /**
     * 高亮绿色背景，通常显示为更鲜艳或明亮的绿色。
     */
    BRIGHT_GREEN(102),

    /**
     * 高亮黄色背景，通常显示为更鲜艳或明亮的黄色。
     */
    BRIGHT_YELLOW(103),

    /**
     * 高亮蓝色背景，通常显示为更鲜艳或明亮的蓝色。
     */
    BRIGHT_BLUE(104),

    /**
     * 高亮紫色背景，通常显示为更鲜艳或明亮的紫色。
     */
    BRIGHT_PURPLE(105),

    /**
     * 高亮青色背景，通常显示为更鲜艳或明亮的青色。
     */
    BRIGHT_CYAN(106),

    /**
     * 高亮白色背景，通常显示为更明亮的白色。
     */
    BRIGHT_WHITE(107),

    ;
    private Integer code;

    /**
     * 构造函数，用于绑定枚举常量与对应的背景色代码。
     *
     * @param code 背景色代码，用于标识不同的背景颜色样式
     */
    BackColorSytle(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
