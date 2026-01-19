package org.hao.config;

import cn.hutool.core.util.ReflectUtil;
import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.autoconfigure.AutoConfigureBeans;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.redis.springdata.RedisSpringDataCacheBuilder;
import com.alicp.jetcache.support.*;
import org.hao.annotation.EnableAutoMethodCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JetCache配置类，用于配置Redis远程缓存和Caffeine本地缓存
 * 支持方法级缓存注解和创建缓存注解功能
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

    /**
     * 配置缓存构建器，初始化远程Redis缓存和本地Caffeine缓存
     * 设置默认的序列化方式、过期时间和缓存限制等参数
     */
    @PostConstruct
    public void configureCache() {

        // 构建Redis远程缓存构建器，配置连接工厂、序列化方式和过期时间
        RedisSpringDataCacheBuilder remoteBuilder =
                RedisSpringDataCacheBuilder.createBuilder()
                        .connectionFactory(redisConnectionFactory) // ← 注入连接工厂
                        .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
                        .valueEncoder(Fastjson2ValueEncoder.INSTANCE)
                        .valueDecoder(Fastjson2ValueDecoder.INSTANCE)
                        .expireAfterWrite(300, TimeUnit.SECONDS); // 默认 TTL

        // 构建Caffeine本地缓存构建器，配置最大缓存数量和过期时间
        CaffeineCacheBuilder localBuilder = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100) // 最大缓存条目数
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .expireAfterWrite(300, TimeUnit.SECONDS);  // 写入后过期时间


        Map<String, CacheBuilder> remoteBuilders = new HashMap<>();
        remoteBuilders.put(CacheConsts.DEFAULT_AREA, remoteBuilder);
        autoConfigureBeans.setRemoteCacheBuilders(remoteBuilders);

        Map<String, CacheBuilder> localBuilders = new HashMap<>();
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);
        autoConfigureBeans.setLocalCacheBuilders(localBuilders);

        // GlobalCacheConfig config = new GlobalCacheConfig();
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setStatIntervalMinutes(15);
        globalCacheConfig.setAreaInCacheName(false);

        // 通过反射调用SpringConfigProvider的初始化方法
        ReflectUtil.invoke(cp, "doInit");
        //cp.init();
        cacheManager.setCacheBuilderTemplate(cp.getCacheBuilderTemplate());


        DecoderMap decoderMap = DecoderMap.defaultInstance();
        // 设置fastjson2的解码器
        decoderMap.register(-153049663, Fastjson2ValueDecoder.INSTANCE);
        // return config;
    }
}
