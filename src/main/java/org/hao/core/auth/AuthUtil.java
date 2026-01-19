package org.hao.core.auth;

import java.util.function.Supplier;

/**
 * 认证工具类，提供用户名获取和设置功能
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/7/3 16:36
 */
public class AuthUtil {
    /**
     * 用户名获取器，默认返回"admin"
     */
    public static Supplier<String> userName = () -> {
        return "admin";
    };

    /**
     * 设置用户名获取方法
     *
     * @param method 用户名获取方法，不能为null
     * @throws NullPointerException 当method参数为null时抛出
     */
    public static void setUserName(Supplier<String> method) {
        if (method == null) throw new NullPointerException("method can not be null");
        AuthUtil.userName = method;
    }

    /**
     * 获取当前用户名
     *
     * @return 当前用户名字符串
     */
    public static String getUserName() {
        return userName.get();
    }
}
