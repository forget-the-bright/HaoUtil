package org.hao.aspect;

import cn.hutool.core.util.StrUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.hao.core.ip.IPUtils;
import org.hao.core.print.PrintUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 打印接口调用日志工具类，用于在方法执行前后输出请求信息、耗时、用户 IP、执行时间等。
 *
 * <p>该类通常被切面类如 {@link PrintAspect} 调用，提供统一的日志格式输出，
 * 支持控制台和日志框架（如 SLF4J）的彩色日志打印。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2024/10/31
 */
public class PrintInterfaceUtil {
    //private static Logger logger = LoggerFactory.getLogger(PrintInterfaceUtil.class);

    public static void executeBefore(String className, String methodName, String value,Class<?> aClass) {
        Logger logger = LoggerFactory.getLogger(aClass);
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String requestURI = request.getRequestURI();
        String userName = IPUtils.getIpAddr(request);
        System.out.println();
        String printImprotMethod = "==================  进入  " + className + ":方法:" + methodName + value + "URL: "+requestURI+"=====================================";
        //PrintUtil.BLUE.Println(printImprotMethod);
        logger.info(PrintUtil.BLUE.getColorStr(printImprotMethod));
        String zhanwei = StrUtil.repeat('=', (int) Math.ceil(printImprotMethod.length() / 3));

        String userword = " 用户 ";
        String user = " " + userName + " ";
        String zhanwei2 = StrUtil.repeat('=', zhanwei.length() - (userword.length() + user.length()));
        //PrintUtil.CYAN.Println(zhanwei + userword + zhanwei2 + user + zhanwei);
        logger.info(PrintUtil.CYAN.getColorStr(zhanwei + userword + zhanwei2 + user + zhanwei));
    }


    public static void executeAfter(long startTime, String className, String methodName, String value,Boolean flag,Exception error,Class<?> aClass) {
        Logger logger = LoggerFactory.getLogger(aClass);
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
        /*PrintUtil.RED.Println(className + "类的方法:" + methodName + value + "执行时间： " + hour + "小时,"
                + minute + "分钟,"
                + second + "秒,共" + interval + "毫秒"
        );*/
        if(flag){
            logger.error(PrintUtil.RED.getColorStr("错误信息: "+error.getMessage()),error);
        }
        logger.info(PrintUtil.RED.getColorStr(className + "类的方法:" + methodName + value + "执行时间： " + hour + "小时,"
                + minute + "分钟,"
                + second + "秒,共" + interval + "毫秒"
        ));
        //PrintUtil.BLUE.Println("===================================================  方法执行完毕  ===================================================");
        logger.info(PrintUtil.BLUE.getColorStr("=======================================================  方法执行"+(flag?"失败":"成功")+"  ======================================================="));
        System.out.println();

    }
}
