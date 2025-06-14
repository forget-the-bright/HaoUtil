package org.hao;

import cn.hutool.core.compiler.JavaSourceCompiler;
import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.hao.core.compiler.CompilerUtil;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestCompilerJob {

    @Test
    public  void testHutoolCompiler() throws Exception {
        String className = "com.example.demo.Greeter";
        String javaCode = "package com.example.demo;\n" +
                "\n" +
                "import org.hao.core.print.PrintUtil;\n" +
                "import org.hao.spring.SpringRunUtil;\n" +
                "\n" +
                "public class Greeter {\n" +
                "    public void sayHello(String name) {\n" +
                "        System.out.println(\"Hello, \" + name + \"!\");\n" +
                "        PrintUtil.BLUE.Println(\"name = \" + name);\n" +
                "        SpringRunUtil.printRunInfo();\n" +
                "    }\n" +
                "}";
        ClassLoader classLoader = cn.hutool.core.compiler.CompilerUtil.getCompiler(null)
                .addSource(className, javaCode).compile();
        final Class<?> clazz = classLoader.loadClass(className);
        // 实例化对象c
        Object obj = ReflectUtil.newInstance(clazz);
        clazz.getMethod("sayHello", String.class).invoke(obj, "World");
    }

    /**
     * 测试编译和运行动态生成的 Java 类。
     *
     * @throws Exception 如果编译或运行失败
     */
    @Test
    public void testSayHello() throws Exception {
        String className = "com.example.demo.Greeter";
        String javaCode = "package com.example.demo;\n" +
                "\n" +
                "import org.hao.core.print.PrintUtil;\n" +
                "import org.hao.spring.SpringRunUtil;\n" +
                "\n" +
                "public class Greeter {\n" +
                "    public void sayHello(String name) {\n" +
                "        System.out.println(\"Hello, \" + name + \"!\");\n" +
                "        PrintUtil.BLUE.Println(\"name = \" + name);\n" +
                "        SpringRunUtil.printRunInfo();\n" +
                "    }\n" +
                "}";

        // 使用工具类编译并加载类
        Class<?> clazz = CompilerUtil.compileAndLoadClass(className, javaCode);


        // 创建类实例并调用方法
        Object obj = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("sayHello", String.class).invoke(obj, "World");
    }


}
