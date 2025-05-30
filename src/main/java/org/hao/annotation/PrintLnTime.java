package org.hao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记需要打印执行耗时日志的方法或类。
 *
 * <p>该注解可应用于方法或类级别，配合切面使用可实现自动记录方法执行时间，
 * 常用于性能监控、日志追踪等场景。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2021.07.22
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrintLnTime {
}
