package org.hao;

import cn.hutool.core.util.RandomUtil;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;

/**
 * TODO
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/6/30 17:22
 */
@Slf4j
public class TestFailsafeJob {
    @Test
    public void testFailsafeOne() throws Exception {
        RetryPolicy.builder()
        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .handle(Exception.class)
                .withMaxRetries(3)
                .onSuccess(event -> System.out.println("Operation succeeded on attempt " + event.getAttemptCount()))
                .onFailure(event -> System.out.println("Operation failed after " + event.getAttemptCount() + " attempts"))
                .onRetry(event -> {
                    System.out.println("Retrying after failure, attempt " + event.getAttemptCount());
                    System.out.println("Last exception: " + event.getLastException().getMessage());
                })
                .build();

        Failsafe.with(retryPolicy).onSuccess(event -> {
            System.out.println("Operation succeeded on attempt " + event.getAttemptCount());
        }).onComplete(event -> {
            System.out.println("Operation completed with result: " + event.getResult());
        }).run(() -> {
            // 可能会失败的操作
            int i = RandomUtil.randomInt(0, 3);
            if (i == 3) {
                throw new Exception("操作失败");
            }
        });
    }
}
