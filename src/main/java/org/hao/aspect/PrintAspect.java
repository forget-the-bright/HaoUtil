package org.hao.aspect;

import cn.hutool.core.util.ObjectUtil;
import io.swagger.annotations.ApiOperation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hao.annotation.PrintLnTime;
import org.hao.core.ip.IPUtils;
import org.hao.core.print.PrintUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Description:
 * ClassName: MyAspect
 * Author: wanghao
 * date: 2021.07.22 10:07
 * version: 1.0
 */
@Aspect
@Component
public class PrintAspect {

    @Around("@annotation(printLnTime)")
    public Object printLnTimeAround(ProceedingJoinPoint joinPoint, PrintLnTime printLnTime) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getName();
        ApiOperation annotation = method.getAnnotation(ApiOperation.class);
        String value = "";
        if (ObjectUtil.isNotEmpty(annotation)) {
            value = ":" + annotation.value() + ": ";
        }
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
