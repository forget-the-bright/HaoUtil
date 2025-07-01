package org.hao.core.failsafe;

import dev.failsafe.*;
import dev.failsafe.event.ExecutionAttemptedEvent;
import dev.failsafe.event.ExecutionCompletedEvent;

import java.time.Duration;

/**
 * 提供失败安全处理机制的接口，用于定义策略初始化和执行前后回调逻辑。
 * 实现该接口的类可以自定义任务的失败处理策略（如重试、熔断等），
 * 并通过回调方法监控任务的执行状态（如成功、失败、重试、完成）。
 *
 * @param <T> 表示执行过程中涉及的结果或数据类型的泛型参数
 * @author wanghao
 * @since 2025-07-01
 */
public interface FailSafeHandler<T> {


    /**
     * 初始化安全策略,默认构建器是 RetryPolicyBuilder
     * 这里返回类型只要是 Policy 就可以了,可以替换对应的实现类。
     * https://failsafe.dev/policies/ 策略概览
     * 重试策略构建器 {@link RetryPolicyBuilder} https://failsafe.dev/retry/
     * 熔断策略构建器 {@link CircuitBreakerBuilder} https://failsafe.dev/circuit-breaker/
     * 限流策略构建器 {@link RateLimiterBuilder} https://failsafe.dev/rate-limiter/
     * 超时策略构建器 {@link TimeoutBuilder} https://failsafe.dev/timeout/
     * 隔离并发策略构建器 {@link BulkheadBuilder} https://failsafe.dev/bulkhead/
     * 降级策略构建器 {@link FallbackBuilder} https://failsafe.dev/fallback/
     *
     * @return 策略对象
     */
    default Policy<T> initFailSafe() {
        // https://failsafe.dev/retry/ 关于重试策略构建器参考文档
        RetryPolicyBuilder<T> builder = RetryPolicy.builder();
        RetryPolicy<T> build = builder
                .handle(Exception.class) // 默认处理所有异常
                .withMaxRetries(3) // 最大重试次数
                .withDelay(Duration.ofSeconds(1)) // 默认延迟1秒
                //withBackoff 会覆盖withDelay,默认等待1秒，最大等待10秒，三次重试达到最大值后不变
                .withBackoff(Duration.ofSeconds(1), Duration.ofSeconds(10), 3)
                .onSuccess(this::onSuccess) // 成功回调
                .onFailure(this::onFailure) // 失败回调
                .onRetry(this::onRetry) // 重试回调
                .build();
        return build;
    }

    /**
     * 执行前配置
     * 超时配置可以在这里做,参考文档：https://failsafe.dev/timeout/
     *
     * @param build safe策略 {@link #initFailSafe()} 返回的策略对象
     * @return 执行器对象
     */
    default FailsafeExecutor<T> beforeRun(Policy<T> build) {
        return Failsafe.with(build).onComplete(this::onComplete);
    }

    /**
     * 执行完成回调
     * 无论是成功或者失败都会执行,只要处理器流程走完，都会执行此方法
     *
     * @param tExecutionCompletedEvent 执行完成事件
     */
    default void onComplete(ExecutionCompletedEvent<T> tExecutionCompletedEvent) {

    }

    /**
     * 重试回调
     * 出现错误重试时会执行此方法,时机在执行重试逻辑之前调用
     *
     * @param objectExecutionAttemptedEvent 执行失败事件
     */
    default void onRetry(ExecutionAttemptedEvent<T> objectExecutionAttemptedEvent) {

    }

    /**
     * 失败回调
     * 当处理器所有流程全部执行失败会执行此方法
     *
     * @param objectExecutionCompletedEvent 执行完成事件
     */
    default void onFailure(ExecutionCompletedEvent<T> objectExecutionCompletedEvent) {

    }

    /**
     * 成功回调
     * 当处理器所有流程最终有一个执行成功会执行此方法
     *
     * @param objectExecutionCompletedEvent 执行完成事件
     */
    default void onSuccess(ExecutionCompletedEvent<T> objectExecutionCompletedEvent) {

    }

}
