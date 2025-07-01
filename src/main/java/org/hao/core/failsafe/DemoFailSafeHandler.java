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
 * @since 2025-07-01
 */
@Slf4j
public class DemoFailSafeHandler<Object> implements FailSafeHandler<Object> {
    /**
     * 初始化安全策略
     *
     * @return 安全策略对象
     */
    @Override
    public Policy<Object> initFailSafe() {
        return FailSafeHandler.super.initFailSafe();
    }

    /**
     * 创建执行器
     *
     * @param build 安全策略对象 {@link #initFailSafe()} 返回的策略对象
     * @return 执行器对象, 这里可以修改执行器配置, 最后拿执行器来执行任务
     */
    @Override
    public FailsafeExecutor<Object> beforeRun(Policy<Object> build) {
        return FailSafeHandler.super.beforeRun(build);
    }

    /**
     * 执行完成回调
     * 无论是成功或者失败都会执行,只要处理器流程走完，都会执行此方法
     *
     * @param objectExecutionCompletedEvent 执行完成事件
     */
    @Override
    public void onComplete(ExecutionCompletedEvent<Object> objectExecutionCompletedEvent) {
        log.info("策略处理器流程,操作已完成并返回结果: {}", objectExecutionCompletedEvent.getResult());
    }

    /**
     * 重试回调
     * 出现错误重试时会执行此方法,时机在执行重试逻辑之前调用
     *
     * @param objectExecutionAttemptedEvent 执行尝试事件
     */
    @Override
    public void onRetry(ExecutionAttemptedEvent<Object> objectExecutionAttemptedEvent) {
        log.info("失败后重试, 尝试第 {} 次 ,最后的异常 : {}",
                objectExecutionAttemptedEvent.getAttemptCount(), objectExecutionAttemptedEvent.getLastException().getMessage());
    }

    /**
     * 失败回调
     * 当处理器所有流程全部执行失败会执行此方法
     *
     * @param objectExecutionCompletedEvent 执行完成事件
     */
    @Override
    public void onFailure(ExecutionCompletedEvent<Object> objectExecutionCompletedEvent) {
        log.info("尝试 {} 次后操作失败", objectExecutionCompletedEvent.getAttemptCount());
    }

    /**
     * 成功回调
     * 当处理器所有流程最终有一个执行成功会执行此方法
     *
     * @param objectExecutionCompletedEvent 执行完成事件
     */
    @Override
    public void onSuccess(ExecutionCompletedEvent<Object> objectExecutionCompletedEvent) {
        log.info("尝试 {} 次后操作成功", objectExecutionCompletedEvent.getAttemptCount());
    }
}
