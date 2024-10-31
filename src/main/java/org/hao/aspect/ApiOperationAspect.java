package org.hao.aspect;

import io.swagger.annotations.ApiOperation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Description:
 * ClassName: ApiOperationAspect
 * Author: wanghao(helloworlwh @ 163.com)
 * date: 2024.10.31 10:07
 * version: 1.0
 */
@Aspect
@Component
public class ApiOperationAspect {

    @Around("@annotation(apiOperation)")
    public Object printLnTimeAround(ProceedingJoinPoint joinPoint, ApiOperation apiOperation) throws Throwable {

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        String value = ":" + apiOperation.value() + ": ";
        PrintInterfaceUtil.executeBefore(className, methodName, value);
        //执行方法
        long startTime = System.currentTimeMillis();   //获取开始时间
        //注意，如果调用joinPoint.proceed()方法，则修改的参数值不会生效，必须调用joinPoint.proceed(Object[] args)
        Object proceed = null;
        try {
            //注意，如果调用joinPoint.proceed()方法，则修改的参数值不会生效，必须调用joinPoint.proceed(Object[] args)
            proceed = joinPoint.proceed();
            //方法执行完成后台操作
            PrintInterfaceUtil.executeAfter(startTime, className, methodName, value,false,"");
        } catch (Exception e) {
            //方法执行完成后台操作
            PrintInterfaceUtil.executeAfter(startTime, className, methodName, value,true,e.getMessage());
            throw e;
        }
        //如果这里不返回result，则目标对象实际返回值会被置为null
        return proceed;
    }



}
