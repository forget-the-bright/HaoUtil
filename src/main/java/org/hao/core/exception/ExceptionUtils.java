package org.hao.core.exception;

import cn.hutool.core.text.StrFormatter;

/**
 * 异常处理工具类
 * 提供异常相关的通用方法，用于简化异常的创建和抛出过程
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2024/11/25
 */

public class ExceptionUtils {

    /**
     * 根据格式化字符串和参数抛出RuntimeException
     * 此方法用于简化异常的抛出过程，通过接受一个格式化字符串和一组参数，
     * 使用StrFormatter.format方法格式化字符串，然后抛出包含格式化信息的RuntimeException
     *
     * @param strPattern 格式化字符串的模式，这通常是一个包含占位符的字符串，
     *                   用于说明如何插入参数以构建最终的字符串
     * @param argArray   一组参数，用于替换格式化字符串中的占位符，这些参数
     *                   应按照它们在格式化字符串中出现的顺序提供
     *                   <p>
     *                   注意：这个方法的设计允许在抛出异常时提供更灵活和丰富的错误信息，
     *                   它抽象了异常信息的构建过程，使得调用者可以以一种更简洁和可读的方式
     *                   提供异常的上下文信息
     */
    public static void throwRuntimEx(String strPattern, Object... argArray) {
        throw new RuntimeException(StrFormatter.format(strPattern, argArray));
    }

}
