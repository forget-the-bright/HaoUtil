package org.hao.core.auth;

import java.util.function.Supplier;

/**
 * TODO
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/7/3 16:36
 */
public class AuthUtil {
    public static Supplier<String> userName = () -> {
        return "admin";
    };

    public static void setUserName(Supplier<String> method) {
        if (method == null) throw new NullPointerException("method can not be null");
        AuthUtil.userName = method;
    }

    public static String getUserName() {
        return userName.get();
    }
}
