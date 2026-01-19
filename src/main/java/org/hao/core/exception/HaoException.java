package org.hao.core.exception;

import cn.hutool.core.util.StrUtil;

/**
 * 自定义异常类，继承自RuntimeException
 * 用于处理系统业务异常，支持错误码设置
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/6/7 22:34
 */
public class HaoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 返回给前端的错误code
     */
    private int errCode = 500;

    /**
     * 构造函数，使用默认错误码500
     *
     * @param message 异常消息
     */
    public HaoException(String message) {
        super(message);
    }

    /**
     * 构造函数，指定错误码
     *
     * @param message 异常消息
     * @param errCode 错误码
     */
    public HaoException(String message, int errCode) {
        super(message);
        this.errCode = errCode;
    }

    /**
     * 构造函数，指定错误码和异常原因
     *
     * @param message 错误消息
     * @param cause   异常原因
     * @param errCode 错误码
     */
    public HaoException(String message, int errCode, Throwable cause) {
        super(message, cause);
        this.errCode = errCode;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public int getErrCode() {
        return errCode;
    }

    /**
     * 构造函数，基于Throwable创建异常
     *
     * @param cause 异常原因
     */
    public HaoException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造函数，同时指定消息和异常原因
     *
     * @param message 异常消息
     * @param cause   异常原因
     */
    public HaoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 根据布尔标志抛出异常
     * 当flag为true时抛出HaoException异常
     *
     * @param message 异常消息
     * @param flag    布尔标志，为true时抛出异常
     */
    public static void throwByFlag(String message, Boolean flag) {
        if (flag) {
            throw new HaoException(message);
        }
    }

    /**
     * 根据布尔标志抛出异常，支持字符串格式化
     * 当flag为true时抛出HaoException异常
     *
     * @param flag     布尔标志，为true时抛出异常
     * @param template 字符串模板
     * @param value    格式化参数
     */
    public static void throwByFlag(Boolean flag, String template, Object... value) {
        if (flag) {
            throw new HaoException(StrUtil.format(template, value));
        }
    }
}
