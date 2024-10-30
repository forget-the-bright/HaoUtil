package org.hao.spring;

import cn.hutool.extra.spring.SpringUtil;
import org.hao.core.ip.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.function.Consumer;

/**
 * Description TODO
 * Author 61778_wanghao
 * Date 2024/5/28 18:20
 */
public class SpringRunUtil {
    private static Logger logger = LoggerFactory.getLogger(SpringRunUtil.class);
    public  static  void runAfter(Class<?> primarySource, String[] args, Consumer<ConfigurableApplicationContext> consumer){
        ConfigurableApplicationContext application = SpringApplication.run(primarySource, args);
        consumer.accept(application);
        printRunInfo();
    }

    public  static  void runAfter(Class<?> primarySource, String[] args){
        SpringApplication.run(primarySource, args);
        printRunInfo();
    }
    private static void printRunInfo(){
        String ip = IPUtils.getLocalIP();
        String port = SpringUtil.getProperty("server.port");
        String path = SpringUtil.getProperty("server.servlet.context-path");
        String applicationName = SpringUtil.getApplicationName();
        path = path == null ? "" : path.equals("") ? "" : path;
        logger.warn("\n----------------------------------------------------------\n\t" +
                "Application " + applicationName + " is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "Swagger-ui: \thttp://" + ip + ":" + port + path + "/swagger-ui.html\n\t" +
                "Doc文档: \thttp://" + ip + ":" + port + path + "/doc.html\n" +
                "----------------------------------------------------------");
    }

}
