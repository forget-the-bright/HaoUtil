package org.hao.spring;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.hao.core.ip.IPUtils;
import org.hao.core.print.PrintUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.function.Consumer;

/**
 * Spring Boot 启动工具类，提供启动时执行自定义逻辑及打印服务运行信息的功能。
 *
 * <p>该类封装了对 Spring Boot 应用的启动增强操作，支持在启动后输出访问地址、IP、端口等关键信息。</p>
 *
 * @author wanghao (61778@xxx.com)
 * @since 2024/5/28
 */

public class SpringRunUtil {
    private static Logger logger = LoggerFactory.getLogger(SpringRunUtil.class);
    public static Class<?> startUpClass = null;

    public static void runAfter(Class<?> primarySource, String[] args, Consumer<ConfigurableApplicationContext> consumer) {
        ConfigurableApplicationContext application = SpringApplication.run(primarySource, args);
        consumer.accept(application);
        printRunInfo();
        startUpClass = primarySource;
    }

    public static void runAfter(Class<?> primarySource, String[] args) {
        runAfter(primarySource, args, application -> {
        });
    }

    public static void printRunInfo() {
        List<String> allIP = IPUtils.allIP;
        String applicationName = SpringUtil.getApplicationName();
        String port = ObjectUtil.defaultIfEmpty(SpringUtil.getProperty("server.port"), "80");
        port = "80".equals(port) ? "" : ":" + port;
        String path = ObjectUtil.defaultIfEmpty(SpringUtil.getProperty("server.servlet.context-path"), "");
        String printStr = "\r\n" +
                "----------------------------------------------------------\r\n" +
                "\tApplication " + applicationName + " is running! Access URLs:\r\n" +
                "\tSwagger:      \thttp://localhost" + port + path + "/swagger-ui.html\r\n" +
                "\tKnif4j:       \thttp://localhost" + port + path + "/doc.html\r\n" +
                "\tLocal:        \thttp://localhost" + port + path + "/\r\n";
        int allIpSize = allIP.size();
        PrintUtil[] values = PrintUtil.values();

        for (int i = 0; i < allIpSize; i++) {
            String ipStr = allIP.get(i);
            int index = i + 1;
            String format = StrUtil.format("\tExternal[{}]: \thttp://{}{}{}\r\n", index, ipStr, port, path);
            printStr += RandomUtil.randomEle(values).getColorStr(format);
        }
        printStr += "----------------------------------------------------------";
        logger.warn(printStr);
    }

}
