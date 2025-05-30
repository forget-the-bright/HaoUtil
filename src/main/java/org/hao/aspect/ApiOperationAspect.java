package org.hao.aspect;

import io.swagger.annotations.ApiOperation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 切面类，用于处理带有 {@link io.swagger.annotations.ApiOperation} 注解的方法，
 * 在方法执行前后打印接口操作日志信息。
 *
 * <p>该切面依赖于 {@link PrintInterfaceUtil} 提供具体的日志输出行为。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @version 1.0
 * @since 2024.10.31
 */
@Aspect
@Component
public class ApiOperationAspect {

    @Around("@annotation(apiOperation)")
    public Object printLnTimeAround(ProceedingJoinPoint joinPoint, ApiOperation apiOperation) throws Throwable {
        Class<?> aClass = joinPoint.getTarget().getClass();
        String className = aClass.getSimpleName();
        String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        String value = ":" + apiOperation.value() + ": ";
        PrintInterfaceUtil.executeBefore(className, methodName, value,aClass);
        //执行方法
        long startTime = System.currentTimeMillis();   //获取开始时间
        //注意，如果调用joinPoint.proceed()方法，则修改的参数值不会生效，必须调用joinPoint.proceed(Object[] args)
        Object proceed = null;
        try {
            //注意，如果调用joinPoint.proceed()方法，则修改的参数值不会生效，必须调用joinPoint.proceed(Object[] args)
            proceed = joinPoint.proceed();
            //方法执行完成后台操作
            PrintInterfaceUtil.executeAfter(startTime, className, methodName, value,false,null,aClass);
        } catch (Exception e) {
            //方法执行完成后台操作
            PrintInterfaceUtil.executeAfter(startTime, className, methodName, value,true,e,aClass);
            throw e;
        }
        //如果这里不返回result，则目标对象实际返回值会被置为null
        return proceed;
    }



}
