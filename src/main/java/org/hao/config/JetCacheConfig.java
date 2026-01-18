package org.hao.config;

import cn.hutool.core.util.ReflectUtil;
import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.autoconfigure.AutoConfigureBeans;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.redis.springdata.RedisSpringDataCacheBuilder;
import com.alicp.jetcache.support.*;
import org.hao.annotation.EnableAutoMethodCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2026/1/14 16:02
 */
@Configuration
@ConditionalOnProperty(prefix = "hao-util", value = "enable-jetcache", havingValue = "true")
//@EnableMethodCache(basePackages = {})
@EnableAutoMethodCache // ← 替代 ConfigProvider
@EnableCreateCacheAnnotation                         // ← 启用 @CreateCache
public class JetCacheConfig {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory; // ← 复用 Spring 的连接工厂
    @Autowired
    private GlobalCacheConfig globalCacheConfig;
    @Autowired
    protected AutoConfigureBeans autoConfigureBeans;
    @Autowired
    private SpringConfigProvider cp;
    @Autowired
    private SimpleCacheManager cacheManager;

    @PostConstruct
    public void configureCache() {
        // 1. 构建远程缓存（关键：使用 SpringDataRedisCacheBuilder）
        RedisSpringDataCacheBuilder remoteBuilder =
                RedisSpringDataCacheBuilder.createBuilder()
                        .connectionFactory(redisConnectionFactory) // ← 注入连接工厂
                        .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                        .valueEncoder(Kryo5ValueEncoder.INSTANCE)
                        .valueDecoder(Kryo5ValueDecoder.INSTANCE)
                        .expireAfterWrite(300, TimeUnit.SECONDS); // 默认 TTL
        // 4. 构建本地缓存（Caffeine）
        CaffeineCacheBuilder localBuilder = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100) // 最大缓存条目数
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .expireAfterWrite(300, TimeUnit.SECONDS);  // 写入后过期时间

        // 2. 注册到 default area
        Map<String, CacheBuilder> remoteBuilders = new HashMap<>();
        remoteBuilders.put(CacheConsts.DEFAULT_AREA, remoteBuilder);
        autoConfigureBeans.setRemoteCacheBuilders(remoteBuilders);
        // 5. 注册本地缓存构建器到 default area
        Map<String, CacheBuilder> localBuilders = new HashMap<>();
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);
        autoConfigureBeans.setLocalCacheBuilders(localBuilders);
        // 3. 创建全局配置（必须设置 ConfigProvider！）
        // GlobalCacheConfig config = new GlobalCacheConfig();
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setStatIntervalMinutes(15);
        globalCacheConfig.setAreaInCacheName(false);

        ReflectUtil.invoke(cp,"doInit");
        //cp.init();
        cacheManager.setCacheBuilderTemplate(cp.getCacheBuilderTemplate());
        // return config;
    }
}
