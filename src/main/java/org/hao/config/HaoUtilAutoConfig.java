package org.hao.config;

import cn.hutool.extra.spring.SpringUtil;
import org.hao.aspect.PrintAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Description TODO
 * Author wanghao(helloworlwh @ 163.com)
 * Date 2024/10/30 上午9:52
 */
@Configuration // 表示该类是一个配置类
@EnableConfigurationProperties({HaoUtilProperties.class}) // 该注解的作用是为 xxxProperties 开启属性配置功能，并将这个类以组件的形式注入到容器中
@ConditionalOnProperty(prefix = "hao-util", value = "enabled") // 当指定的配置项等于你想要的时候，配置类生效
@Import({SpringUtil.class,PrintAspect.class})
public class HaoUtilAutoConfig {
    /*@Bean // @Bean：该注解用于将方法的返回值以 Bean 对象的形式添加到容器中
    @ConditionalOnMissingBean // @ConditionalOnMissingBean(xxx.class)：该注解表示当容器中没有 xxx 类时，该方法才生效
    public PrintAspect PrintAspect() {
        return new PrintAspect();
    }*/
}
