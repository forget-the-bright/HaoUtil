package org.hao.core.cache;

import com.alicp.jetcache.anno.aop.CacheAdvisor;
import com.alicp.jetcache.anno.aop.JetCacheInterceptor;
import org.hao.annotation.EnableAutoMethodCache;
import org.hao.spring.SpringRunUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * JetCache代理配置类，用于配置方法级缓存的AOP代理
 * 该类实现了ImportAware接口来获取@EnableAutoMethodCache注解的属性，
 * 并实现ApplicationContextAware接口来获取应用上下文
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/14 16:45
 */
@Configuration
public class JetCacheProxyConfiguration implements ImportAware, ApplicationContextAware {
    protected AnnotationAttributes enableMethodCache;
    private ApplicationContext applicationContext;

    /**
     * 设置导入元数据，解析@EnableAutoMethodCache注解的属性
     * 验证注解是否存在并保存其属性信息
     *
     * @param importMetadata 导入的注解元数据
     */
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableMethodCache = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableAutoMethodCache.class.getName(), false));
        if (this.enableMethodCache == null) {
            throw new IllegalArgumentException("@EnableMethodCache is not present on importing class " + importMetadata.getClassName());
        }
    }

    /**
     * 设置应用上下文
     *
     * @param applicationContext 应用上下文
     * @throws BeansException 设置过程中可能出现的异常
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 创建JetCache的缓存顾问Bean
     * 该顾问负责将缓存拦截器应用到指定包下的方法上
     *
     * @param jetCacheInterceptor JetCache拦截器实例
     * @return 配置好的CacheAdvisor实例
     */
    @Bean(
            name = {"jetcache2.internalCacheAdvisor"}
    )
    @Role(2)
    public CacheAdvisor jetcacheAdvisor(JetCacheInterceptor jetCacheInterceptor) {
        CacheAdvisor advisor = new CacheAdvisor();
        advisor.setAdvice(jetCacheInterceptor);
        String basePackage = ClassUtils.getPackageName(SpringRunUtil.startUpClass);
        //this.enableMethodCache.getStringArray("basePackages")
        advisor.setBasePackages(new String[]{basePackage});
        advisor.setOrder((Integer) this.enableMethodCache.getNumber("order"));
        return advisor;
    }

    /**
     * 创建JetCache拦截器Bean
     * 该拦截器负责处理缓存相关的逻辑
     *
     * @return 新的JetCacheInterceptor实例
     */
    @Bean
    @Role(2)
    public JetCacheInterceptor jetCacheInterceptor() {
        return new JetCacheInterceptor();
    }
}
