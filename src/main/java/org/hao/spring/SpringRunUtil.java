package org.hao.spring;

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

    public static void runAfter(Class<?> primarySource, String[] args, Consumer<ConfigurableApplicationContext> consumer) {
        ConfigurableApplicationContext application = SpringApplication.run(primarySource, args);
        consumer.accept(application);
        printRunInfo();
    }

    public static void runAfter(Class<?> primarySource, String[] args) {
        SpringApplication.run(primarySource, args);
        printRunInfo();
    }

    public static void printRunInfo() {
        List<String> allIP = IPUtils.allIP;
        String port = SpringUtil.getProperty("server.port");
        String path = SpringUtil.getProperty("server.servlet.context-path");
        String applicationName = SpringUtil.getApplicationName();
        path = path == null ? "" : path.equals("") ? "" : path;
        String printStr = "\n----------------------------------------------------------\n\t" +
                "Application " + applicationName + " is running! Access URLs:\n\t" +
                "Swagger-ui: \thttp://localhost:" + port + path + "/swagger-ui.html\n\t" +
                "Doc文档: \t\thttp://localhost:" + port + path + "/doc.html\n\t" +
                "Local: \t\t\thttp://localhost:" + port + path + "/\n\t";
        int allIpSize = allIP.size();
        PrintUtil[] values = PrintUtil.values();

        for (int i = 0; i < allIpSize; i++) {
            String ipStr = allIP.get(i);
            int index = i + 1;
            String format = StrUtil.format("External[{}]: \thttp://{}:{}{}\n{}", index, ipStr, port, path, index == allIpSize ? "" : "\t");
            printStr += RandomUtil.randomEle(values).getColorStr(format);
        }
        printStr += "----------------------------------------------------------";
        logger.warn(printStr);
    }

}
