package org.hao;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.hao.annotation.LogDefine;
import org.hao.core.compiler.CompilerUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;

@Slf4j
public class TestCompilerJob {

    @Test
    public void testHutoolCompiler() throws Exception {
        long start = System.currentTimeMillis();
        String className = "com.example.demo.Greeter";
        String javaCode = "package com.example.demo;\n" +
                "\n" +
                "import org.hao.core.print.PrintUtil;\n" +
                "import org.hao.spring.SpringRunUtil;\n" +
                "import org.hao.annotation.LogDefine;\n" +
                "\n" +
                "public class Greeter {\n" +
                "    @LogDefine(\"123\")        " +
                "    public void sayHello(String name) {\n" +
                "        System.out.println(\"Hello, \" + name + \"!\");\n" +
                "        PrintUtil.BLUE.Println(\"name = \" + name);\n" +
                "        SpringRunUtil.printRunInfo();\n" +
                "    }\n" +
                "}";
        ClassLoader classLoader = cn.hutool.core.compiler.CompilerUtil.getCompiler(null)
                .addSource(className, javaCode).compile();
        final Class<?> clazz = classLoader.loadClass(className);
        long end = System.currentTimeMillis();
        log.info("testHutoolCompiler 编译耗时：{}ms", end - start);
        Method sayHello = clazz.getMethod("sayHello", String.class);
        LogDefine annotation = sayHello.getAnnotation(LogDefine.class);
        // 实例化对象c
        Object obj = ReflectUtil.newInstance(clazz);
        sayHello.invoke(obj, "World");
    }

    /**
     * 测试编译和运行动态生成的 Java 类。
     *
     * @throws Exception 如果编译或运行失败
     */
    @Test
    public void testHaoCompliler() throws Exception {
        long start = System.currentTimeMillis();
        String className = "com.example.demo.Greeter";
        String javaCode = "package com.example.demo;\n" +
                "\n" +
                "import org.hao.core.print.PrintUtil;\n" +
                "import org.hao.spring.SpringRunUtil;\n" +
                "import org.hao.annotation.LogDefine;\n" +
                "\n" +
                "public class Greeter {\n" +
                "    @LogDefine(\"123\")        " +
                "    public void sayHello(String name) {\n" +
                "        System.out.println(\"Hello, \" + name + \"!\");\n" +
                "        PrintUtil.BLUE.Println(\"name = \" + name);\n" +
                "        SpringRunUtil.printRunInfo();\n" +
                "    }\n" +
                "}";
        String currentWorkingDirectory = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentWorkingDirectory);
        // 使用工具类编译并加载类
        Class<?> clazz = CompilerUtil.compileAndLoadClass(className, javaCode);
        long end = System.currentTimeMillis();
        log.info("testHaoCompliler 编译耗时：{}ms", end - start);
        Method sayHello = clazz.getMethod("sayHello", String.class);
        LogDefine annotation = sayHello.getAnnotation(LogDefine.class);

        // 创建类实例并调用方法
        Object obj = clazz.getDeclaredConstructor().newInstance();
        sayHello.invoke(obj, "World");
    }

    @Test
    public void extractJar() throws IOException {
        CompilerUtil.extractDependencyJarsToTempDir("D:\\Project\\铜陵\\tl-back-enfi\\enfi-module-system\\enfi-system-start\\target\\enfi-system-start-3.7.0.jar");
        CompilerUtil.extractDependencyJarsToTempDir("D:\\Project\\铜陵\\tl-back-enfi\\enfi-module-system\\enfi-system-start\\target\\enfi-system-start-3.7.0.jar");
    }
}
