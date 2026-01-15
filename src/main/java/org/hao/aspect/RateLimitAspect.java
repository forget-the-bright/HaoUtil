package org.hao.aspect;

import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hao.annotation.RateLimit;
import org.hao.core.exception.HaoException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面类，用于处理带有RateLimit注解的方法进行限流控制
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/14 11:17
 */
@Aspect
@Component
public class RateLimitAspect {
    /**
     * 存储限流器的缓存映射，key为方法签名，value为对应的限流器实例
     */
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    /**
     * 环绕通知方法，对带有RateLimit注解的方法执行限流控制
     *
     * @param joinPoint 切点连接对象，包含被拦截方法的相关信息
     * @param rateLimit 限流注解对象，包含限流配置信息
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中可能抛出的异常
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 根据方法签名生成唯一标识作为限流器的key
        String key = joinPoint.getSignature().toLongString();
        // 如果不存在对应的限流器则创建新的限流器，qps值从注解中获取
        RateLimiter limiter = limiters.computeIfAbsent(key, k -> RateLimiter.create(rateLimit.qps()));
        // 尝试获取令牌，如果获取失败则抛出系统繁忙异常
        HaoException.throwByFlag(!limiter.tryAcquire(), "系统繁忙,请稍后重试");
        // 执行原方法
        return joinPoint.proceed();
    }
}
