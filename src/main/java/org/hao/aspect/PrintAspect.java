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
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String userName = IPUtils.getIpAddr(request);

        Signature signature = joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Method method = ((MethodSignature) signature).getMethod();
        String methodName = method.getName();

        ApiOperation annotation = method.getAnnotation(ApiOperation.class);
        String value = "";
        if (ObjectUtil.isNotEmpty(annotation)) {
            value = ":" + annotation.value() + ": ";
        }
        System.out.println();
        String printImprotMethod = "==================  进入  " + className + ":方法:" + methodName + value + "=====================================";
        PrintUtil.BLUE.Println(printImprotMethod);
        StringBuffer stringBufferOne = new StringBuffer();
        StringBuffer stringBufferTwo = new StringBuffer();
        for (int i = 0; i < Math.ceil(printImprotMethod.length() / 3); i++) {
            stringBufferOne.append("=");
        }
        String userword = " 用户 ";
        String user = " " + userName + " ";
        String zhanwei = stringBufferOne.toString();
        Integer threeLength = zhanwei.length();
        int i1 = threeLength - (userword.length() + user.length());
        for (int i = 0; i < i1; i++) {
            stringBufferTwo.append("=");
        }
        PrintUtil.CYAN.Println(zhanwei + userword + stringBufferTwo + user + zhanwei);
        //执行方法
        long startTime = System.currentTimeMillis();   //获取开始时间
        //注意，如果调用joinPoint.proceed()方法，则修改的参数值不会生效，必须调用joinPoint.proceed(Object[] args)
        Object proceed = joinPoint.proceed();
        //方法执行完成后台操作
        long endTime = System.currentTimeMillis(); //获取结束时间
        long interval = endTime - startTime;//运行的时间 毫秒
        SimpleDateFormat formatter = new SimpleDateFormat("HH");//初始化Formatter的转换格式。
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String hour = formatter.format(interval);
        formatter.applyPattern("mm");
        String minute = formatter.format(interval);
        formatter.applyPattern("ss");
        String second = formatter.format(interval);
        PrintUtil.RED.Println(className + "类的方法:" + methodName + value + "执行时间： " + hour + "小时,"
                + minute + "分钟,"
                + second + "秒,共" + interval + "毫秒"
        );
        PrintUtil.BLUE.Println("===================================================  方法执行完毕  ===================================================");
        System.out.println();

        //如果这里不返回result，则目标对象实际返回值会被置为null
        return proceed;
    }


}
