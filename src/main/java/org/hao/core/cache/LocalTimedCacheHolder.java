package org.hao.core.cache;

import cn.hutool.cache.impl.TimedCache;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存持有者，使用 Hutool 的 TimedCache 实现。
 * <p>
 * 该类根据配置中的 token 过期时间设置缓存的过期时间，并提供基本的缓存操作方法。
 * 默认情况下，缓存会在 token 过期前 5 秒自动刷新。
 *
 * @param <K> 缓存键的类型
 * @param <V> 缓存值的类型
 * @author wanghao (helloworlwh @ 163.com)
 * @since 2025-03-10
 */
public class LocalTimedCacheHolder<K, V> implements CacheHolder<K, V> {

    /**
     * Hutool 的 TimedCache 实例，用于存储缓存数据。
     */
    private final TimedCache<K, V> cache;


    /**
     * 默认提前刷新 Token 的时间缓冲，单位为毫秒。
     */
    private static final long DEFAULT_EXPIRE_BUFFER = 5000; // 提前 5 秒刷新 Token

    /**
     * 构造函数，初始化 TimedCache 并设置过期时间。
     *
     */
    public LocalTimedCacheHolder(long seconds) {
        long timeout = seconds == 0 ? DEFAULT_EXPIRE_BUFFER : TimeUnit.SECONDS.toMillis(seconds);
        this.cache = new TimedCache<>(timeout);
        //启动定时任务
        this.cache.schedulePrune(timeout);
    }

    public LocalTimedCacheHolder() {
        this.cache = new TimedCache<>(DEFAULT_EXPIRE_BUFFER);
        this.cache.schedulePrune(DEFAULT_EXPIRE_BUFFER);
    }

    /**
     * 根据键获取缓存值。
     *
     * @param key 缓存键
     * @return 缓存值，如果不存在则返回 null
     */
    @Override
    public V get(K key) {
        return cache.get(key, false);
    }

    /**
     * 根据键移除缓存值。
     *
     * @param key 缓存键
     */
    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    /**
     * 将键值对存入缓存，并设置过期时间。
     *
     * @param key     缓存键
     * @param object  缓存值
     * @param timeout 过期时间，单位为毫秒
     */
    @Override
    public void put(K key, V object, long timeout) {
        cache.put(key, object, timeout);
    }

    /**
     * 将键值对存入缓存，使用默认的 token 过期时间减去提前刷新缓冲时间作为过期时间。
     *
     * @param key    缓存键
     * @param object 缓存值
     */
    @Override
    public void put(K key, V object) {
        cache.put(key, object);
    }
}
