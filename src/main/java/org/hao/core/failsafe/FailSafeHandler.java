package org.hao.core.failsafe;

import dev.failsafe.*;
import dev.failsafe.event.ExecutionAttemptedEvent;
import dev.failsafe.event.ExecutionCompletedEvent;

import java.time.Duration;

public interface FailSafeHandler<T> {

    /**
     * 初始化安全策略,默认构建器是 RetryPolicyBuilder
     * 这里返回类型只要是 Policy<T> 就可以了,可以替换对应的实现类。
     * BulkheadBuilder.class, CircuitBreakerBuilder.class, DelayablePolicyBuilder.class
     * FailurePolicyBuilder.class ,FallbackBuilder.class, PolicyBuilder.class,
     * RateLimiterBuilder.class ,RetryPolicyBuilder.class,TimeoutBuilder.class
     *
     * @return
     */
    default Policy<T> initFailSafe() {
        RetryPolicyBuilder<T> builder = RetryPolicy.builder();
        RetryPolicy<T> build = builder
                .handle(Exception.class) // 默认处理所有异常
                .withDelay(Duration.ofSeconds(1)) // 默认延迟1秒
                .onSuccess(this::onSuccess) // 成功回调
                .onFailure(this::onFailure) // 失败回调
                .onRetry(this::onRetry) // 重试回调
                .build();
        return build;
    }

    /**
     * 执行前配置
     * 超时配置在这里做,参考文档：https://failsafe.dev/timeout/
     *
     * @param build
     * @return
     */
    default FailsafeExecutor<T> beforeRun(Policy<T> build) {
        return Failsafe.with(build);
    }


    default void onRetry(ExecutionAttemptedEvent<T> objectExecutionAttemptedEvent) {

    }


    default void onFailure(ExecutionCompletedEvent<T> objectExecutionCompletedEvent) {

    }


    default void onSuccess(ExecutionCompletedEvent<T> objectExecutionCompletedEvent) {

    }

}
