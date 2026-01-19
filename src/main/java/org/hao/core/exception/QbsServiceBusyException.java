package org.hao.core.exception;

import cn.hutool.core.util.StrUtil;

/**
 * QBS请求量过大导致服务繁忙的异常类
 * 继承自HaoException，专门用于处理QBS服务因请求量过大而繁忙的情况
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/6/7 22:34
 */
public class QbsServiceBusyException extends HaoException {
    private static final long serialVersionUID = 1L;

    /**
     * QBS服务繁忙的错误码
     */
    private static final int QBS_BUSY_ERROR_CODE = 9001;

    /**
     * 构造函数，使用默认的服务繁忙消息
     */
    public QbsServiceBusyException() {
        super("QBS请求量过大，服务繁忙，请稍后再试", QBS_BUSY_ERROR_CODE);
    }

    /**
     * 构造函数，使用自定义消息
     *
     * @param message 自定义异常消息
     */
    public QbsServiceBusyException(String message) {
        super(message, QBS_BUSY_ERROR_CODE);
    }

    /**
     * 构造函数，使用自定义消息和底层异常
     *
     * @param message 自定义异常消息
     * @param cause 底层异常原因
     */
    public QbsServiceBusyException(String message, Throwable cause) {
        super(message, QBS_BUSY_ERROR_CODE, cause);
    }

    /**
     * 构造函数，基于底层异常创建
     *
     * @param cause 底层异常原因
     */
    public QbsServiceBusyException(Throwable cause) {
        super("QBS请求量过大，服务繁忙，请稍后再试", QBS_BUSY_ERROR_CODE, cause);
    }

    /**
     * 根据请求频率过高标志抛出异常
     * 当检测到QBS请求频率过高时抛出此异常
     *
     * @param flag 布尔标志，为true时抛出异常
     */
    public static void throwIfRequestTooFrequent(Boolean flag) {
        if (flag) {
            throw new QbsServiceBusyException();
        }
    }

    /**
     * 根据请求频率过高标志抛出异常，支持自定义消息
     * 当检测到QBS请求频率过高时抛出此异常
     *
     * @param flag 布尔标志，为true时抛出异常
     * @param template 自定义消息模板
     * @param args 消息格式化参数
     */
    public static void throwIfRequestTooFrequent(Boolean flag, String template, Object... args) {
        if (flag) {
            throw new QbsServiceBusyException(StrUtil.format(template, args));
        }
    }
}
