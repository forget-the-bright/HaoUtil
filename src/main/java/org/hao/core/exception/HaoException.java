package org.hao.core.exception;

import cn.hutool.core.util.StrUtil;

/**
 * TODO
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

    public HaoException(String message) {
        super(message);
    }

    public HaoException(String message, int errCode) {
        super(message);
        this.errCode = errCode;
    }
    public int getErrCode() {
        return errCode;
    }
    public HaoException(Throwable cause) {
        super(cause);
    }

    public HaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public static void throwByFlag(String message, Boolean flag) {
        if (flag) {
            throw new HaoException(message);
        }
    }

    public static void throwByFlag(Boolean flag, String template, Object... value) {
        if (flag) {
            throw new HaoException(StrUtil.format(template, value));
        }
    }
}
