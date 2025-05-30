package org.hao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 日志定义注解，用于标记方法或类的日志行为，支持自定义日志类型和描述信息。
 *
 * <p>该注解可用于方法或类级别，通过 {@link #value()} 指定日志类型或名称，
 * 通过 {@link #description()} 提供更详细的业务描述。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2021.07.22
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogDefine {
    String value() default "";
    String description() default "";
}
