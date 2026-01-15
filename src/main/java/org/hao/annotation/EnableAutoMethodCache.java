package org.hao.annotation;

import com.alicp.jetcache.anno.config.CommonConfiguration;
import org.hao.core.cache.JetcacheConfigSelector;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * TODO
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/14 16:43
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CommonConfiguration.class, JetcacheConfigSelector.class}) // ← 关键：用自己的 ConfigSelector
public @interface EnableAutoMethodCache {
    // 不需要 basePackages 属性！我们自动计算
    boolean proxyTargetClass() default false;
    AdviceMode mode() default AdviceMode.PROXY;
    int order() default Integer.MAX_VALUE;
}