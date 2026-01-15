package org.hao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解，用于标记需要进行QPS（每秒查询率）限制的方法
 * 该注解可以应用在方法上，在运行时通过反射获取注解信息来实现限流功能
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 每秒允许的最大请求数，默认值为1.0
     * <p>
     * 例如，@RateLimit(qps = 5.0)表示每秒最多允许5个请求
     * <p>
     * 例如，@RateLimit(qps = 1.0 / (5 * 60))表示每5分钟最多允许1个请求
     *
     * @return QPS（Queries Per Second）限制值，表示每秒最多允许的请求数量
     */
    double qps() default 1.0;
}
