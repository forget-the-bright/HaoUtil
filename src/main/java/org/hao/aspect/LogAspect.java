package org.hao.aspect;

import cn.hutool.core.util.StrUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hao.annotation.LogDefine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Description:
 * ClassName: MyAspect
 * Author: wanghao
 * date: 2021.07.22 10:07
 * version: 1.0
 */
@Aspect
@Component
public class LogAspect {

    private LogDefineConfig logDefineConfig;


    public LogAspect(LogDefineConfig logDefineConfig) {
        this.logDefineConfig = logDefineConfig;
    }

    @Around("@annotation(logDefine)")
    public Object printLnTimeAround(ProceedingJoinPoint joinPoint, LogDefine logDefine) throws Throwable {
        Class<?> aClass = joinPoint.getTarget().getClass();
        String className = aClass.getSimpleName();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        String methodName = StrUtil.isEmpty(logDefine.description()) ? method.getName() : logDefine.description();

        logDefineConfig.getBeforeMethod(logDefine.value()).apply(className, methodName, aClass);
        //执行方法
        long startTime = System.currentTimeMillis();   //获取开始时间
        //注意，如果调用joinPoint.proceed()方法，则修改的参数值不会生效，必须调用joinPoint.proceed(Object[] args)
        Object proceed = null;

        try {
            //注意，如果调用joinPoint.proceed()方法，则修改的参数值不会生效，必须调用joinPoint.proceed(Object[] args)
            proceed = joinPoint.proceed();
            //方法执行完成后台操作
            logDefineConfig.getAfterMethod(logDefine.value()).apply(className, methodName, aClass, null, startTime);
        } catch (Exception error) {
            //方法执行完成后台操作
            logDefineConfig.getAfterMethod(logDefine.value()).apply(className, methodName, aClass, error, startTime);
            throw error;
        }
        //如果这里不返回result，则目标对象实际返回值会被置为null
        return proceed;
    }


}
