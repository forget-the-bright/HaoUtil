package org.hao.core.compiler;

import javax.tools.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompilerUtil {
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

    public static Class<?> compileAndLoadClass(String className, String javaCode) throws Exception {
        return compileAndLoadClass(className, javaCode, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 编译并加载 Java 源代码为 Class 对象。
     *
     * @param className         完整的类名（包括包名）
     * @param javaCode          Java 源代码字符串
     * @param parentClassLoader 父类加载器
     * @return 编译后的 Class 对象
     * @throws Exception 如果编译失败或加载失败
     */
    public static Class<?> compileAndLoadClass(String className, String javaCode, ClassLoader parentClassLoader) throws Exception {
        // 获取系统自带的 Java 编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("无法获取 Java 编译器，请确保使用的是 JDK 而不是 JRE");
        }

        // 创建内存中的 Java 文件对象
        InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(compiler.getStandardFileManager(null, null, null));
        List<JavaFileObject> compilationUnits = Arrays.asList(new JavaSourceFromString(className, javaCode));

        // 执行编译任务
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                null,
                null,
                null,
                compilationUnits);
        boolean success = task.call();
        if (!success) {
            throw new RuntimeException("Compilation failed");
        }

        // 创建类加载器并加载编译后的类
        if (parentClassLoader == null) {
            parentClassLoader = Thread.currentThread().getContextClassLoader();
        }
        InMemoryClassLoader classLoader = new InMemoryClassLoader(parentClassLoader);
        for (Map.Entry<String, InMemoryJavaFileManager.ByteCodeJavaFileObject> entry : fileManager.getCompiledClasses().entrySet()) {
            classLoader.addClassBytes(entry.getKey(), entry.getValue().getByteCode());
        }
        Class<?> aClass = classLoader.loadClass(className);
        classCache.put(className, aClass);
        return aClass;
    }

    /**
     * 根据类名从缓存中创建实例。
     *
     * @param className 类名
     * @return 类实例
     * @throws Exception 如果类未找到或实例化失败
     */
    public Object createInstance(String className) throws Exception {
        Class<?> clazz = classCache.get(className);
        if (clazz == null) {
            throw new IllegalArgumentException("Class not found: " + className);
        }
        return clazz.getDeclaredConstructor().newInstance();
    }
}