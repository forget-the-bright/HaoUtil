package org.hao.core.print;

/**
 * 表示字体样式类型的枚举类，定义了常见的文本样式选项，如粗体、斜体、下划线等。
 * <p>
 * 每个字体样式对应一个整型值（code），用于在底层系统中标识不同的样式效果。
 * 注意：部分样式可能在某些终端或显示设备上不被支持。
 * </p>
 */
public enum FontSytle {
    /**
     * 重置所有属性到默认状态。
     */
    RESET(0),

    /**
     * 粗体，使文本显示为加粗样式。
     */
    BOLD(1),

    /**
     * 淡色或次亮度，部分终端可能不支持。
     */
    DIM(2),

    /**
     * 斜体，使文本显示为倾斜样式，部分终端可能不支持。
     */
    ITALIC(3),

    /**
     * 下划线，为文本添加下划线样式。
     */
    UNDERLINE(4),

    /**
     * 缓慢闪烁，文本以较慢频率闪烁显示，很少有终端支持。
     */
    BLINK_SLOW(5),

    /**
     * 快速闪烁，文本以较快频率闪烁显示，很少有终端支持。
     */
    BLINK_FAST(6),

    /**
     * 反转前景色和背景色，交换当前文本和背景颜色。
     */
    REVERSE(7),

    /**
     * 隐藏文本，文本不可见但仍然占据空间。
     */
    HIDDEN(8),

    /**
     * 删除线，为文本添加删除线样式。
     */
    STRIKETHROUGH(9);

    private Integer code;

    /**
     * 构造函数，用于将枚举常量与对应的样式代码进行绑定。
     *
     * @param code 样式代码，用于标识不同的文本样式
     */
    FontSytle(Integer code) {
        this.code = code;
    }
    public Integer getCode() {
        return code;
    }
}
