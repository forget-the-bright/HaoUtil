package org.hao.aspect;

import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hao.annotation.RateLimit;
import org.hao.core.exception.QbsServiceBusyException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面类，用于处理带有RateLimit注解的方法进行限流控制
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/14 11:17
 */
@Order(Integer.MIN_VALUE) // 数字小，优先级高，先执行
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
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中可能抛出的异常
     */
    @Around("@annotation(org.hao.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        String requestURI = "本地请求";
        String userName = "local_system";
        //如果是web请求,这里才会判断qps
        if (attributes != null && (request = attributes.getRequest()) != null) {
            // 获取目标方法上的 @RateLimit 注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            RateLimit rateLimit = method.getAnnotation(RateLimit.class);
            // 根据方法签名生成唯一标识作为限流器的key
            String key = joinPoint.getSignature().toLongString();
            // 如果不存在对应的限流器则创建新的限流器，qps值从注解中获取
            RateLimiter limiter = limiters.computeIfAbsent(key, k -> RateLimiter.create(rateLimit.qps()));
            // 尝试获取令牌，如果获取失败则抛出系统繁忙异常
            QbsServiceBusyException.throwIfRequestTooFrequent(!limiter.tryAcquire(), "当前接口Qps请求量为 {} ,目前请求量过大,系统繁忙,请稍后重试", rateLimit.qps());
        }
        // 执行原方法
        return joinPoint.proceed();
    }
}
