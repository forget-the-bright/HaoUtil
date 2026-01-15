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
 * TODO
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/14 16:45
 */
@Configuration
public class JetCacheProxyConfiguration implements ImportAware, ApplicationContextAware {
    protected AnnotationAttributes enableMethodCache;
    private ApplicationContext applicationContext;

    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableMethodCache = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableAutoMethodCache.class.getName(), false));
        if (this.enableMethodCache == null) {
            throw new IllegalArgumentException("@EnableMethodCache is not present on importing class " + importMetadata.getClassName());
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

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

    @Bean
    @Role(2)
    public JetCacheInterceptor jetCacheInterceptor() {
        return new JetCacheInterceptor();
    }
}