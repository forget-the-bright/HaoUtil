package org.hao.core.concurrent;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多租户限流器，为不同的用户ID提供独立的限流控制
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/14 10:56
 */
public class MultiTenantRateLimiter {
    /**
     * 存储用户ID与对应限流器的映射关系
     */
    private final Map<String, RateLimiter> userLimiters = new ConcurrentHashMap<>();

    /**
     * 为指定用户设置限流速率
     *
     * @param userId 用户唯一标识
     * @param qps    每秒允许的请求数量
     * @return 对应用户的限流器实例
     */
    public RateLimiter setRate(String userId, double qps) {
        RateLimiter limiter = userLimiters.computeIfAbsent(userId, k -> RateLimiter.create(qps));
        return limiter;
    }

    /**
     * 尝试获取一个令牌，用于限制指定用户的访问频率
     * 如果该用户不存在对应的限流器，则创建一个默认速率为1QPS的限流器
     *
     * @param userId 用户唯一标识
     * @return true表示获取令牌成功（允许访问），false表示获取令牌失败（被限流）
     */
    public boolean tryAcquire(String userId) {
        RateLimiter limiter = userLimiters.computeIfAbsent(userId, k -> RateLimiter.create(1));
        return limiter.tryAcquire();
    }

}
