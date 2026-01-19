package org.hao.annotation;

import com.alicp.jetcache.anno.config.CommonConfiguration;
import org.hao.core.cache.JetcacheConfigSelector;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用自动方法缓存功能的注解
 *
 * 该注解用于开启基于JetCache的方法级缓存功能，通过导入相关配置类来实现
 * 自动化的方法缓存机制。支持代理模式配置和执行顺序控制。
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/14 16:43
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CommonConfiguration.class, JetcacheConfigSelector.class}) // 关键：用自己的 ConfigSelector
public @interface EnableAutoMethodCache {
    /**
     * 是否使用CGLIB代理目标类本身，而不是目标类的接口
     * 默认使用JDK动态代理（仅适用于实现了接口的类）
     * 当设置为true时，将使用CGLIB代理，可以代理没有实现接口的类
     *
     * @return true表示使用CGLIB代理目标类，false表示使用JDK动态代理
     */
    boolean proxyTargetClass() default false;

    /**
     * 指定AOP代理的处理模式
     * PROXY模式：创建基于代理的AOP增强
     *
     * @return AOP代理的处理模式，默认为PROXY模式
     */
    AdviceMode mode() default AdviceMode.PROXY;

    /**
     * 指定当前配置的执行顺序
     * 数值越小优先级越高，Integer.MAX_VALUE表示最低优先级
     *
     * @return 配置执行顺序，默认为Integer.MAX_VALUE（最低优先级）
     */
    int order() default Integer.MAX_VALUE;
}
