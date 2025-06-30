package org.hao;

import cn.hutool.core.util.RandomUtil;
import dev.failsafe.Failsafe;
import dev.failsafe.Policy;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import dev.failsafe.event.ExecutionAttemptedEvent;
import dev.failsafe.event.ExecutionCompletedEvent;
import lombok.extern.slf4j.Slf4j;

import org.hao.core.failsafe.FailSafeHandler;
import org.hao.core.failsafe.FailSafeHandlerExecuteor;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * TODO
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/6/30 17:22
 */
@Slf4j
public class TestFailsafeJob {
    @Test
    public void testFailsafeHandler() throws Exception {
        FailSafeHandler<Object> failSafeHandler = new FailSafeHandler<Object>() {

            @Override
            public void onRetry(ExecutionAttemptedEvent<Object> event) {
                log.info("Retrying after failure, attempt {} ,Last exception: {}", event.getAttemptCount(), event.getLastException().getMessage());
            }

            @Override
            public void onSuccess(ExecutionCompletedEvent<Object> objectExecutionCompletedEvent) {
                log.info("Operation succeeded on attempt {}", objectExecutionCompletedEvent.getAttemptCount());
            }

            @Override
            public void onFailure(ExecutionCompletedEvent<Object> objectExecutionCompletedEvent) {
                log.info("Operation failed after {} attempts", objectExecutionCompletedEvent.getAttemptCount());
            }

            @Override
            public void onComplete(ExecutionCompletedEvent<Object> objectExecutionCompletedEvent) {
                log.info("Operation completed with result: {}", objectExecutionCompletedEvent.getResult());
            }
        };
/*        FailSafeHandlerExecuteor.execute(failSafeHandler, () -> {
            // 模拟操作
            int i = RandomUtil.randomInt(0, 5);
            log.info("i = {}", i);
            if (i != 6) {
                throw new Exception("操作失败");
            }
        });*/
        FailSafeHandlerExecuteor.execute(failSafeHandler, () -> {
            // 模拟操作
            return 123;
        });
    }

    @Test
    public void testFailsafeOne() throws Exception {
        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .handle(Exception.class)
                .withMaxRetries(4)
                .withDelay(Duration.ofSeconds(1))
                //.withBackoff(Duration.ofSeconds(1), Duration.ofSeconds(10), 3)
                .onSuccess(event -> System.out.println("Operation succeeded on attempt " + event.getAttemptCount()))
                .onFailure(event -> System.out.println("Operation failed after " + event.getAttemptCount() + " attempts"))
                .onRetry(event -> {
                    log.info("Retrying after failure, attempt {} ,Last exception: {}", event.getAttemptCount(), event.getLastException().getMessage());
                })
                .build();

        Failsafe.with(retryPolicy)
                .onComplete(event -> {
                    System.out.println("completed 全部操作执行成功 result: " + event.getResult());
                }).run(() -> {
                    // 可能会失败的操作
                    int i = RandomUtil.randomInt(0, 3);
                    log.info("i = {}", i);
                    if (i != 2) {
                        throw new Exception("操作失败");
                    }
                });
    }
}
