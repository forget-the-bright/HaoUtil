package org.hao.core.cache;

/**
 * 缓存持有者接口，定义了基本的缓存操作方法。
 * <p>
 * 该接口提供了获取、移除和存储缓存数据的方法。
 *
 * @param <K> 缓存键的类型
 * @param <V> 缓存值的类型
 * @author wanghao (helloworlwh @ 163.com)
 * @since 2025-03-03
 */
public interface CacheHolder<K, V> {

    /**
     * 根据键获取缓存值。
     *
     * @param key 缓存键
     * @return 缓存值，如果不存在则返回 null
     */
    V get(K key);

    /**
     * 根据键移除缓存值。
     *
     * @param key 缓存键
     */
    void remove(K key);

    /**
     * 将键值对存入缓存，并设置过期时间。
     *
     * @param key     缓存键
     * @param object  缓存值
     * @param timeout 过期时间，单位为毫秒
     */
    void put(K key, V object, long timeout);

    /**
     * 将键值对存入缓存，使用默认的过期时间。
     *
     * @param key    缓存键
     * @param object 缓存值
     */
    void put(K key, V object);
}
