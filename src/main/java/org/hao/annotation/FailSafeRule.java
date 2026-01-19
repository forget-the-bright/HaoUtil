package org.hao.annotation;


import org.hao.core.failsafe.FailSafeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 容错规则注解，用于标记需要进行容错处理的方法或类型
 * 该注解可以应用在方法级别或类型级别，运行时保留以便反射使用
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FailSafeRule {
    /**
     * 指定容错处理器的实现类
     * @return 实现了FailSafeHandler接口的处理器类
     */
    Class<? extends FailSafeHandler> handler() ;//default DemoFailSafeHandler.class;
}
