package org.hao.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.hao.annotation.FailSafeRule;
import org.hao.core.failsafe.FailSafeHandler;
import org.hao.core.failsafe.FailSafeHandlerExecuteor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 实现失败安全机制的切面类。
 * 该切面用于拦截带有 {@link FailSafeRule} 注解的方法，并通过配置的失败处理策略（如重试、熔断等）执行目标方法。
 * 支持 void 和非 void 返回类型的方法，并自动根据返回类型调用合适的执行方式。
 *
 * @author wanghao
 * @since 2025-07-01
 */
@Aspect
@Component
public class FailSafeAspect {

    /**
     * 定义切点：拦截所有使用 {@link FailSafeRule} 注解的方法。
     */
    @Pointcut("@annotation(org.hao.annotation.FailSafeRule)")
    public void failSafePointCut() {
    }

    /**
     * 环绕通知：在目标方法执行前后应用失败安全处理逻辑。
     * <p>
     * 1. 获取目标方法的注解信息，判断是否配置了失败处理策略；
     * 2. 若配置了，则创建对应的 FailSafeHandler 实例；
     * 3. 根据方法返回类型选择调用无返回值或有返回值的执行器；
     * 4. 执行目标方法并应用失败策略（如重试、熔断等）。
     *
     * @param joinPoint 连接点对象，封装了被拦截方法的信息和执行上下文
     * @return 目标方法的执行结果，若为 void 方法则返回 null
     * @throws Throwable 若目标方法或失败处理器抛出异常
     */

    @Around("failSafePointCut()")
    @SuppressWarnings("unchecked")
    public Object failSafeAspectAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        FailSafeRule failSafeRule = method.getAnnotation(FailSafeRule.class);
        Class<? extends FailSafeHandler> handler = failSafeRule.handler();
        // 如果没有配置失败处理策略，则直接执行目标方法
        if (handler == null) {
            return joinPoint.proceed();
        }
        // 判断是否为接口, 如果是则返回
        if (handler.isInterface()) {
            return joinPoint.proceed();
        }
        FailSafeHandler handlerInstance = handler.newInstance();
        // 获取返回类型
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(Void.TYPE)) {
            System.out.println("该方法返回类型为 void");
            // 执行目标方法（不处理返回值）
            FailSafeHandlerExecuteor.execute(handlerInstance, () -> {
                joinPoint.proceed();
            });
            return null;
        } else {
            System.out.println("该方法返回类型为: " + returnType.getName());
            // 可以在这里对返回值做增强处理
            Object result = FailSafeHandlerExecuteor.execute(handlerInstance, () -> {
                Object proceed = joinPoint.proceed();
                return proceed;
            });
            return result;
        }
    }
}
