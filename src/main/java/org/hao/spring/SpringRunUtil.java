package org.hao.spring;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.hao.core.StrUtil;
import org.hao.core.ip.IPUtils;
import org.hao.core.print.FontSytle;
import org.hao.core.print.PrintUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.Collections;
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

    /**
     * 打印工具数组，包含多种颜色的打印工具
     */
    private static PrintUtil[] printUtils = new PrintUtil[]{
            PrintUtil.RED,
            PrintUtil.GREEN,
            PrintUtil.YELLOW,
            PrintUtil.BLUE,
            PrintUtil.PURPULE,
            PrintUtil.CYAN,
    };

    /**
     * 运行Spring Boot应用并在启动后执行默认逻辑
     *
     * @param primarySource 主配置类
     * @param args          命令行参数
     */
    public static void runAfter(Class<?> primarySource, String[] args) {
        runAfter(primarySource, args, application -> {
        });
    }

    /**
     * 运行Spring Boot应用并在启动后执行自定义逻辑
     *
     * @param primarySource 主配置类
     * @param args          命令行参数
     * @param consumer      启动后的回调函数，接收ConfigurableApplicationContext参数
     */
    public static void runAfter(Class<?> primarySource, String[] args, Consumer<ConfigurableApplicationContext> consumer) {
        startUpClass = primarySource;
        ConfigurableApplicationContext application = SpringApplication.run(primarySource, args);
        consumer.accept(application);
        printRunInfo();
    }

    /**
     * 打印应用程序运行信息，包括应用名称、本地访问地址和外部IP访问地址
     */
    public static void printRunInfo() {
        List<String> allIP = IPUtils.allIP;
        String applicationName = SpringUtil.getApplicationName();
        String port = ObjectUtil.defaultIfEmpty(SpringUtil.getProperty("server.port"), "80");
        port = "80".equals(port) ? "" : StrUtil.formatFast(":{}", port);
        String path = ObjectUtil.defaultIfEmpty(SpringUtil.getProperty("server.servlet.context-path"), "");
        StringBuilder printStr = new StringBuilder();
        printStr.append("\r\n")
                .append("----------------------------------------------------------\r\n")
                .append(StrUtil.formatFast("\tApplication {} is running! Access URLs:\r\n", applicationName))
                .append(StrUtil.formatFast("\tSwagger:      \thttp://localhost{}{}/swagger-ui.html\r\n", port, path))
                .append(StrUtil.formatFast("\tKnif4j:       \thttp://localhost{}{}/doc.html\r\n", port, path))
                .append(StrUtil.formatFast("\tLocal:        \thttp://localhost{}{}/\r\n", port, path));

        // 随机打乱打印工具数组，用于不同IP显示不同颜色
        int allIpSize = allIP.size();
        List<PrintUtil> list = Arrays.asList(printUtils);
        Collections.shuffle(list);
        for (int i = 0; i < allIpSize; i++) {
            String ipStr = allIP.get(i);
            printStr.append(list.get(i % list.size()).getColorStr("\tExternal[{}]: \thttp://{}{}{}\r\n", FontSytle.BOLD, (i + 1), ipStr, port, path));

        }
        printStr.append("----------------------------------------------------------");
        logger.warn(printStr.toString());
    }

}
