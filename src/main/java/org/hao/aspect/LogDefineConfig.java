package org.hao.aspect;

import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.core.util.ObjectUtil;
import org.hao.core.print.PrintUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.TimeZone;


/**
 * 自定义日志配置类
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/4/24 下午5:13
 */
@Component
public class LogDefineConfig {
    public static final FastDateFormat formatterHour = FastDateFormat.getInstance("HH", TimeZone.getTimeZone("GMT+00:00"));
    public static final FastDateFormat formatterMinute = FastDateFormat.getInstance("mm", TimeZone.getTimeZone("GMT+00:00"));
    public static final FastDateFormat formatterSecond = FastDateFormat.getInstance("ss", TimeZone.getTimeZone("GMT+00:00"));

    private HashMap<String, LogBeforeMethod> defineBeforeMap = new HashMap<String, LogBeforeMethod>() {{
        put("default", (className, methodName, aClass) -> {
            Logger logger = LoggerFactory.getLogger(aClass);
            String printImprotMethod = "==================  进入  " + className + ":方法:" + methodName + "=====================================";
            logger.info(PrintUtil.BLUE.getColorStr(printImprotMethod));
            return null;
        });
    }};

    private HashMap<String, LogAfterMethod> defineAfterMap = new HashMap<String, LogAfterMethod>() {{
        put("default", (className, methodName, aClass, error, beginIntervalMs) -> {
            Logger logger = LoggerFactory.getLogger(aClass);
            long endIntervalMs = System.currentTimeMillis(); //获取结束时间
            long intervalMs = endIntervalMs - beginIntervalMs;
            String hour = formatterHour.format(intervalMs);
            String minute = formatterMinute.format(intervalMs);
            String second = formatterSecond.format(intervalMs);
            boolean flag = ObjectUtil.isNotEmpty(error);
            if (flag) {
                logger.error(PrintUtil.RED.getColorStr("错误信息: " + error.getMessage()), error);
            }
            logger.info(PrintUtil.RED.getColorStr(className + "类的方法:" + methodName + "执行时间： " + hour + "小时,"
                    + minute + "分钟,"
                    + second + "秒,共" + intervalMs + "毫秒"
            ));
            logger.info(PrintUtil.BLUE.getColorStr("=======================================================  方法执行" + (flag ? "失败" : "成功") + "  ======================================================="));
            System.out.println();
            return null;
        });
    }};

    public interface LogBeforeMethod {
        String apply(String className, String methodName, Class<?> aClass);
    }

    public interface LogAfterMethod {
        String apply(String className, String methodName, Class<?> aClass, Exception error, Long beginIntervalMs);
    }

    public LogBeforeMethod getBeforeMethod(String key) {
        return defineBeforeMap.getOrDefault(key, defineBeforeMap.get("default"));
    }

    public LogAfterMethod getAfterMethod(String key) {
        return defineAfterMap.getOrDefault(key, defineAfterMap.get("default"));
    }

    public void addBeforeMethod(String key, LogBeforeMethod logBeforeMethod) {
        defineBeforeMap.put(key, logBeforeMethod);
    }

    public void addAfterMethod(String key, LogAfterMethod logAfterMethod) {
        defineAfterMap.put(key, logAfterMethod);
    }
}
