package org.hao.config;

import cn.hutool.extra.spring.SpringUtil;
import org.hao.aspect.*;
import org.hao.core.ws.WSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 自动配置类，用于根据 `hao-util` 相关配置项初始化工具组件 Bean。
 *
 * <p>该类基于 Spring Boot 的条件注解控制 Bean 的加载行为，
 * 包括切面类、日志配置类及 WebSocket 工具类等。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2024/10/30
 */

@Configuration // 表示该类是一个配置类
@EnableConfigurationProperties({HaoUtilProperties.class}) // 该注解的作用是为 xxxProperties 开启属性配置功能，并将这个类以组件的形式注入到容器中
@ConditionalOnProperty(prefix = "hao-util", value = "enabled", havingValue = "true") // 当指定的配置项等于你想要的时候，配置类生效
@Import({SpringUtil.class})//PrintAspect.class
public class HaoUtilAutoConfig {
    @Autowired
    private HaoUtilProperties haoUtilProperties;

    @Bean // @Bean：该注解用于将方法的返回值以 Bean 对象的形式添加到容器中
    // @ConditionalOnMissingBean(xxx.class)：该注解表示当容器中没有 xxx 类时，该方法才生效
    @ConditionalOnProperty(prefix = "hao-util", value = "print-interface", havingValue = "false")
    public PrintAspect PrintAspect() {
        return new PrintAspect();
    }

    @Bean // @Bean：该注解用于将方法的返回值以 Bean 对象的形式添加到容器中
    // @ConditionalOnMissingBean(xxx.class)：该注解表示当容器中没有 xxx 类时，该方法才生效
    @ConditionalOnProperty(prefix = "hao-util", value = "print-interface", havingValue = "true")
    public ApiOperationAspect ApiOperationAspect() {
        return new ApiOperationAspect();
    }

    @Bean
    public LogDefineConfig logDefineConfig() {
        return new LogDefineConfig();
    }

    @Bean
    public LogAspect logAspect(LogDefineConfig logDefineConfig) {
        return new LogAspect(logDefineConfig);
    }


    @Bean // @Bean：该注解用于将方法的返回值以 Bean 对象的形式添加到容器中
    // @ConditionalOnMissingBean(xxx.class)：该注解表示当容器中没有 xxx 类时，该方法才生效
    @ConditionalOnProperty(prefix = "hao-util", value = "enable-ws", havingValue = "true")
    public WSUtil wsUtil() {
        return new WSUtil(haoUtilProperties);
    }


    @Bean // @Bean：该注解用于将方法的返回值以 Bean 对象的形式添加到容器中
    // @ConditionalOnMissingBean(xxx.class)：该注解表示当容器中没有 xxx 类时，该方法才生效
    @ConditionalOnProperty(prefix = "hao-util", value = "enable-failsafe", havingValue = "true")
    public FailSafeAspect failSafeAspect() {
        return new FailSafeAspect();
    }
}
