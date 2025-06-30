package org.hao.core.failsafe;

import dev.failsafe.FailsafeExecutor;
import dev.failsafe.Policy;
import dev.failsafe.event.ExecutionAttemptedEvent;
import dev.failsafe.event.ExecutionCompletedEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的失败处理类
 *
 * @author wanghao
 * @date 2025-07-01
 */
@Slf4j
public class DefaultFailSafeHandler<Object> implements FailSafeHandler<Object> {
    @Override
    public Policy<Object> initFailSafe() {
        return FailSafeHandler.super.initFailSafe();
    }

    @Override
    public FailsafeExecutor<Object> beforeRun(Policy<Object> build) {
        return FailSafeHandler.super.beforeRun(build);
    }

    /**
     * 执行完成回调
     * 无论是成功或者失败都会执行,只要处理器流程走完，都会执行此方法
     *
     * @param objectExecutionCompletedEvent
     */
    @Override
    public void onComplete(ExecutionCompletedEvent<Object> objectExecutionCompletedEvent) {
        log.debug("Operation completed with result: {}", objectExecutionCompletedEvent.getResult());
    }

    /**
     * 重试回调
     * 出现错误重试时会执行此方法,时机在执行重试逻辑之前调用
     *
     * @param objectExecutionAttemptedEvent
     */
    @Override
    public void onRetry(ExecutionAttemptedEvent<Object> objectExecutionAttemptedEvent) {
        log.debug("Retrying after failure, attempt {} ,Last exception: {}",
                objectExecutionAttemptedEvent.getAttemptCount(), objectExecutionAttemptedEvent.getLastException().getMessage());
    }

    /**
     * 失败回调
     * 当处理器所有流程全部执行失败会执行此方法
     *
     * @param objectExecutionCompletedEvent
     */
    @Override
    public void onFailure(ExecutionCompletedEvent<Object> objectExecutionCompletedEvent) {
        log.debug("Operation failed after {} attempts", objectExecutionCompletedEvent.getAttemptCount());
    }

    /**
     * 成功回调
     * 当处理器所有流程最终有一个执行成功会执行此方法
     *
     * @param objectExecutionCompletedEvent
     */
    @Override
    public void onSuccess(ExecutionCompletedEvent<Object> objectExecutionCompletedEvent) {
        log.debug("Operation succeeded on attempt {}", objectExecutionCompletedEvent.getAttemptCount());
    }
}
