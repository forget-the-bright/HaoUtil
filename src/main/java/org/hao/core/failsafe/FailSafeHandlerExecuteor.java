package org.hao.core.failsafe;

import dev.failsafe.FailsafeExecutor;
import dev.failsafe.Policy;
import dev.failsafe.function.CheckedRunnable;
import dev.failsafe.function.CheckedSupplier;

/**
 * 执行带有失败安全机制任务的协调器。
 * 该类封装了通过 FailsafeExecutor 执行任务的通用逻辑，
 * 支持运行无返回值的任务（Runnable）和有返回值的操作（Supplier）。
 *
 * @author wanghao
 * @since 2025-07-01
 */
public class FailSafeHandlerExecuteor {
    /**
     * 执行给定的任务，并应用失败安全处理逻辑
     * 该方法通过使用提供的处理程序初始化失败安全策略，并在执行任务前进行必要的设置
     *
     * @param handler  处理失败安全逻辑的处理程序，定义了如何初始化失败安全策略以及执行任务前的设置
     * @param runnable 要执行的任务，可能抛出异常
     * @param <T>      处理程序和策略的类型参数，允许处理不同的数据类型
     */
    public static <T> void execute(FailSafeHandler<T> handler, CheckedRunnable runnable) {
        // 初始化失败安全策略
        Policy<T> tPolicy = handler.initFailSafe();
        // 在执行任务前，根据策略进行必要的设置
        FailsafeExecutor<T> tFailsafeExecutor = handler.beforeRun(tPolicy);
        // 执行任务
        tFailsafeExecutor.run(runnable);
    }


    /**
     * 执行一个带有失败安全机制的操作或任务
     * 该方法通过使用提供的策略和供应者来执行操作，并在遇到异常时根据策略提供保护措施
     *
     * @param handler  处理失败安全策略的处理器，用于定义在执行过程中如何处理失败
     * @param supplier 一个供应者，用于执行可能抛出检查异常的操作
     * @param <T>      泛型参数，表示操作的结果类型
     * @return 操作的结果，类型为T
     */
    public static <T> T execute(FailSafeHandler<T> handler, CheckedSupplier<T> supplier) {
        // 初始化失败安全策略
        Policy<T> tPolicy = handler.initFailSafe();
        // 在执行前应用策略前的准备工作
        FailsafeExecutor<T> tFailsafeExecutor = handler.beforeRun(tPolicy);
        // 执行操作并获取结果
        T t = tFailsafeExecutor.get(supplier);
        // 返回操作结果
        return t;
    }

}
