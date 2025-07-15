package org.hao.core.print;

import org.hao.core.StrUtil;

/**
 * 构建带颜色和样式的控制台文本的构建器类。
 * <p>
 * 通过链式调用设置文本颜色、背景色和字体样式，最终调用 {@link #build(String)} 方法生成带有 ANSI 控制码的字符串。
 * 支持组合多种样式（如加粗、斜体、下划线等）和前景/背景颜色。
 * </p>
 *
 * <pre>{@code
 * String coloredText = ColorText.Builder()
 *     .FgRed()             // 设置前景(字体)红色
 *     .BgBlack()         // 设置背景黑色
 *     .FontBold()            // 字体加粗
 *     .build("Hello");   // 构建带样式的字符串
 * }</pre>
 *
 * @author wanghao (helloworlwh @ 163.com)
 * @since 2025/7/15 13:55
 */
public class ColorText {
    private final StringBuilder sytleBuilder = new StringBuilder();

    /**
     * 私有构造函数，防止外部直接实例化。
     * 使用 {@link #Builder()} 获取实例。
     */
    private ColorText() {
    }

    /**
     * 静态工厂方法，创建一个新的 {@link ColorText}  实例。
     *
     * @return 新的 {@link ColorText}  实例
     */
    public static ColorText Builder() {
        return new ColorText();
    }

    /**
     * 将指定的样式代码添加到当前样式构建器中。
     *
     * @param sytle 样式代码，例如前景色、背景色或字体样式
     */
    private void addSytle(Object sytle) {
        if (sytleBuilder.length() > 0) {
            sytleBuilder.append(";");
        }
        sytleBuilder.append(sytle);
    }

    //region Foreground 前景(字体)色

    /**
     * 设置文本颜色为黑色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgBlack() {
        addSytle(PrintUtil.BLACK.getColor());
        return this;
    }

    /**
     * 设置文本颜色为红色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgRed() {
        addSytle(PrintUtil.RED.getColor());
        return this;
    }

    /**
     * 设置文本颜色为绿色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgGreen() {
        addSytle(PrintUtil.GREEN.getColor());
        return this;
    }

    /**
     * 设置文本颜色为黄色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgYellow() {
        addSytle(PrintUtil.YELLOW.getColor());
        return this;
    }

    /**
     * 设置文本颜色为蓝色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgBlue() {
        addSytle(PrintUtil.BLUE.getColor());
        return this;
    }

    /**
     * 设置文本颜色为紫色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgPurple() {
        addSytle(PrintUtil.PURPULE.getColor());
        return this;
    }

    /**
     * 设置文本颜色为青色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgCyan() {
        addSytle(PrintUtil.CYAN.getColor());
        return this;
    }

    /**
     * 设置文本颜色为白色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgWhite() {
        addSytle(PrintUtil.WHITE.getColor());
        return this;
    }

    /**
     * 设置文本颜色为高亮黑色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgBrightBlack() {
        addSytle(PrintUtil.BRIGHT_BLACK.getColor());
        return this;
    }

    /**
     * 设置文本颜色为高亮红色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgBrightRed() {
        addSytle(PrintUtil.BRIGHT_RED.getColor());
        return this;
    }

    /**
     * 设置文本颜色为高亮绿色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgBrightGreen() {
        addSytle(PrintUtil.BRIGHT_GREEN.getColor());
        return this;
    }

    /**
     * 设置文本颜色为高亮黄色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgBrightYellow() {
        addSytle(PrintUtil.BRIGHT_YELLOW.getColor());
        return this;
    }

    /**
     * 设置文本颜色为高亮蓝色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgBrightBlue() {
        addSytle(PrintUtil.BRIGHT_BLUE.getColor());
        return this;
    }

    /**
     * 设置文本颜色为高亮紫色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgBrightPurple() {
        addSytle(PrintUtil.BRIGHT_PURPLE.getColor());
        return this;
    }

    /**
     * 设置文本颜色为高亮青色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgBrightCyan() {
        addSytle(PrintUtil.BRIGHT_CYAN.getColor());
        return this;
    }

    /**
     * 设置文本颜色为高亮白色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FgBrightWhite() {
        addSytle(PrintUtil.BRIGHT_WHITE.getColor());
        return this;
    }
    // endregion

    // region Background 背景色

    /**
     * 设置背景颜色为黑色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgBlack() {
        addSytle(BackColorSytle.BLACK.getCode());
        return this;
    }

    /**
     * 设置背景颜色为红色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgRed() {
        addSytle(BackColorSytle.RED.getCode());
        return this;
    }

    /**
     * 设置背景颜色为绿色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgGreen() {
        addSytle(BackColorSytle.GREEN.getCode());
        return this;
    }

    /**
     * 设置背景颜色为黄色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgYellow() {
        addSytle(BackColorSytle.YELLOW.getCode());
        return this;
    }

    /**
     * 设置背景颜色为蓝色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgBlue() {
        addSytle(BackColorSytle.BLUE.getCode());
        return this;
    }

    /**
     * 设置背景颜色为紫色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgPurple() {
        addSytle(BackColorSytle.PURPULE.getCode());
        return this;
    }

    /**
     * 设置背景颜色为青色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgCyan() {
        addSytle(BackColorSytle.CYAN.getCode());
        return this;
    }

    /**
     * 设置背景颜色为白色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgWhite() {
        addSytle(BackColorSytle.WHITE.getCode());
        return this;
    }

    /**
     * 设置背景颜色为高亮黑色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgBrightBlack() {
        addSytle(BackColorSytle.BRIGHT_BLACK.getCode());
        return this;
    }

    /**
     * 设置背景颜色为高亮红色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgBrightRed() {
        addSytle(BackColorSytle.BRIGHT_RED.getCode());
        return this;
    }

    /**
     * 设置背景颜色为高亮绿色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgBrightGreen() {
        addSytle(BackColorSytle.BRIGHT_GREEN.getCode());
        return this;
    }

    /**
     * 设置背景颜色为高亮黄色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgBrightYellow() {
        addSytle(BackColorSytle.BRIGHT_YELLOW.getCode());
        return this;
    }

    /**
     * 设置背景颜色为高亮蓝色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgBrightBlue() {
        addSytle(BackColorSytle.BRIGHT_BLUE.getCode());
        return this;
    }

    /**
     * 设置背景颜色为高亮紫色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgBrightPurple() {
        addSytle(BackColorSytle.BRIGHT_PURPULE.getCode());
        return this;
    }

    /**
     * 设置背景颜色为高亮青色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgBrightCyan() {
        addSytle(BackColorSytle.BRIGHT_CYAN.getCode());
        return this;
    }

    /**
     * 设置背景颜色为高亮白色。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText BgBrightWhite() {
        addSytle(BackColorSytle.BRIGHT_WHITE.getCode());
        return this;
    }
    // endregion


    // region Font 字体样式

    /**
     * 设置文本为加粗样式。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FontBold() {
        addSytle(FontSytle.BOLD.getCode());
        return this;
    }

    /**
     * 设置文本为淡色或次亮度，部分终端可能不支持。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FontDim() {
        addSytle(FontSytle.DIM.getCode());
        return this;
    }

    /**
     * 设置文本为斜体样式。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FontItalic() {
        addSytle(FontSytle.ITALIC.getCode());
        return this;
    }

    /**
     * 设置文本为下划线样式。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FontUnderline() {
        addSytle(FontSytle.UNDERLINE.getCode());
        return this;
    }

    /**
     * 设置文本为缓慢闪烁样式（部分终端支持）。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FontBlink() {
        addSytle(FontSytle.BLINK_SLOW.getCode());
        return this;
    }

    /**
     * 设置文本为快速闪烁样式（部分终端支持）。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FontBlinkFast() {
        addSytle(FontSytle.BLINK_FAST.getCode());
        return this;
    }

    /**
     * 设置文本为前景色与背景色反转样式。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FontReverse() {
        addSytle(FontSytle.REVERSE.getCode());
        return this;
    }

    /**
     * 设置文本为隐藏样式（不可见，但占据空间）。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FontHidden() {
        addSytle(FontSytle.HIDDEN.getCode());
        return this;
    }

    /**
     * 设置文本为删除线样式。
     *
     * @return 当前 {@link ColorText} 实例，支持链式调用
     */
    public ColorText FontStrikeThrough() {
        addSytle(FontSytle.STRIKETHROUGH.getCode());
        return this;
    }
    // endregion

    /**
     * 构建最终带样式的字符串，使用当前配置的颜色和样式。
     * <pre>
     *  相同的样式已添加过，将忽略重复添加。
     *  同类型样式以最后一次添加的为准 如 红色字体色 + 绿色字体色，将只应用红色字体色 ,其他以此类推,背景色
     *  字体样式不同的可以同时添加, 如 粗体 和 斜体
     * </pre>
     *
     * @param content 要应用样式的文本内容
     * @return 带 ANSI 控制码的格式化字符串
     */
    public String build(String content) {
        StringBuilder formatStr = new StringBuilder();
        formatStr.append("\033[");
        formatStr.append(sytleBuilder);
        formatStr.append("m");
        formatStr.append(content);
        formatStr.append("\033[0m");
        return formatStr.toString();
    }

    /**
     * 构建带参数替换和样式的字符串。
     * 使用提供的参数对模板字符串进行格式化后应用样式。
     *
     * @param content 模板字符串，可包含格式化占位符
     * @param args    格式化参数列表
     * @return 带 ANSI 控制码的格式化字符串
     */
    public String build(CharSequence content, Object... args) {
        String formatStr = StrUtil.formatFast(content, args);
        return build(formatStr);
    }

    /**
     * 打印带当前样式的字符串到控制台，并自动换行。
     *
     * @param content 要打印的文本内容，将应用当前已配置的颜色和样式
     */
    public void Println(String content) {
        System.out.println(build(content));
    }

    /**
     * 格式化指定模板并打印带样式的字符串到控制台，自动换行。
     * 使用提供的参数对模板进行格式化后应用当前颜色和样式。
     *
     * @param content 模板字符串，可包含格式化占位符（如 %s, %d）
     * @param args    格式化参数列表
     */
    public void Println(CharSequence content, Object... args) {
        System.out.println(build(content, args));
    }

}
