package org.hao.core.compiler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import org.hao.core.exception.HaoException;
import org.hao.spring.SpringRunUtil;
import org.springframework.boot.system.ApplicationHome;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class CompilerUtil {
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    //获取系统 Java 编译器：通过 ToolProvider.getSystemJavaCompiler() 获取编译器实例
    public static final JavaCompiler SYSTEM_COMPILER = ToolProvider.getSystemJavaCompiler();
    public static final TreeSet<String> classpath = new TreeSet<>();


    public static Class<?> compileAndLoadClass(String className, String javaCode) throws ClassNotFoundException {
        return compileAndLoadClass(className, javaCode, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 编译并加载 Java 源代码为 Class 对象。
     *
     * @param className         完整的类名（包括包名）
     * @param javaCode          Java 源代码字符串
     * @param parentClassLoader 父类加载器
     * @return 编译后的 Class 对象
     * @throws ClassNotFoundException 如果类未找到
     */
    public static Class<?> compileAndLoadClass(String className, String javaCode, ClassLoader parentClassLoader) throws ClassNotFoundException {
        // 获取系统自带的 Java 编译器
        if (SYSTEM_COMPILER == null) {
            throw new RuntimeException("无法获取 Java 编译器，请确保使用的是 JDK 而不是 JRE");
        }

        // 构建内存文件管理器：使用 InMemoryJavaFileManager 管理源码与字节码的内存存储
        InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(SYSTEM_COMPILER.getStandardFileManager(null, null, null));
        List<JavaFileObject> compilationUnits = Arrays.asList(new JavaSourceFromString(className, javaCode));

        // 执行编译任务
        // 构造编译任务：将输入的 Java 源码字符串封装为 JavaFileObject 并设置编译参数
        // 创建一个诊断收集器，用于收集编译过程中的信息
        DiagnosticCollector<? super JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        // 创建一个选项列表，用于配置编译任务的参数
        List<String> options = new ArrayList<>();
        if (classpath.isEmpty()) {
            classpath.clear();
            classpath.addAll(loadClassPath());
        }
        if (CollUtil.isNotEmpty(classpath)) {
            options.add("-cp");
            options.add(StrUtil.join(File.pathSeparator, classpath));
            // System.out.println( options);
        }
        // 获取一个编译任务实例
        // 此处省略了SYSTEM_COMPILER和fileManager的初始化过程
        JavaCompiler.CompilationTask task = SYSTEM_COMPILER.getTask(
                (Writer) null, // 不使用Writer对象
                fileManager, // 文件管理器，负责管理编译过程中的文件
                diagnosticCollector, // 诊断收集器，收集编译信息
                options, // 编译选项
                (Iterable) null, // 不使用类路径入口
                compilationUnits); // 编译单元集合，包含需要编译的Java源文件
        // 尝试执行编译任务
        try {
            // 如果编译失败
            if (!task.call()) {
                // 抛出异常，包含编译失败的详细信息
                // todo 这里可以做一些处理,比如编译失败，如果是jar环境，可以清理掉temp-classpath 重新解压加载，
                //  但是问题是，如果代码就是引用了找不到的库，这里重复加载，就会消耗系统性能， 磁盘io
                throw new HaoException("编译失败: " + getDiagnosticMessages(diagnosticCollector));
            }
        } finally {
            // 确保文件管理器被正确关闭，释放资源
            IoUtil.close(fileManager);
        }
        // 创建类加载器并加载编译后的类
        // 如果未指定父类加载器，则使用当前线程的上下文类加载器
        if (parentClassLoader == null) {
            parentClassLoader = Thread.currentThread().getContextClassLoader();
        }

        // 创建自定义的类加载器实例，用于加载内存中的字节码
        InMemoryClassLoader classLoader = new InMemoryClassLoader(parentClassLoader);

        // 遍历已编译的类集合，将每个类的字节码添加到类加载器中
        for (Map.Entry<String, InMemoryJavaFileManager.ByteCodeJavaFileObject> entry : fileManager.getCompiledClasses().entrySet()) {
            classLoader.addClassBytes(entry.getKey(), entry.getValue().getByteCode());
        }

        // 使用自定义类加载器加载指定名称的类
        Class<?> aClass = classLoader.loadClass(className);

        // 将加载的类缓存起来，以便后续使用
        classCache.put(className, aClass);

        // 返回加载的类
        return aClass;
    }


    /**
     * 根据类名从缓存中创建实例。
     *
     * @param className 类名
     * @return 类实例
     * @throws Exception 如果类未找到或实例化失败
     */
    public static Object createInstance(String className) throws Exception {
        Class<?> clazz = classCache.get(className);
        if (clazz == null) {
            throw new IllegalArgumentException("Class not found: " + className);
        }
        return clazz.getDeclaredConstructor().newInstance();
    }

    public static String getDiagnosticMessages(DiagnosticCollector<?> collector) {
        List<?> diagnostics = collector.getDiagnostics();
        return diagnostics.stream().map(String::valueOf).collect(Collectors.joining(System.lineSeparator()));
    }

    public static TreeSet<String> loadClassPath() {
        if (null == SpringRunUtil.startUpClass) {
            return loadLocalClassPath();
        }
        ApplicationHome home = new ApplicationHome(SpringRunUtil.startUpClass);
        String jarBaseFile = home.getSource().getPath();
        if (jarBaseFile.endsWith(".jar")) {
            try {
                return extractDependencyJarsToTempDir(jarBaseFile);
            } catch (Exception e) {
                throw new HaoException(e);
            }
        } else {
            return loadLocalClassPath();
        }
    }

    private static boolean jarFileIsExtract = false;
    private static String tempDirName;
    private static String tmpdir = System.getProperties().getProperty("java.io.tmpdir");


    public static synchronized TreeSet<String> extractDependencyJarsToTempDir(String jarBaseFile) throws IOException {

        File tempDir = new File(tmpdir + File.separator + "tempCompilerDir");

        tempDirName = tempDir.getAbsolutePath();
        TreeSet<String> classpath = new TreeSet<>();
        if (!jarFileIsExtract) {
            FileUtil.del(tempDir);
            tempDir.mkdir();
        } else {
            if (tempDir.exists() || tempDir.isDirectory()) {
                File[] files = tempDir.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        classpath.add(file.getAbsolutePath());
                    }
                }
                if (CollUtil.isNotEmpty(classpath)) {
                    classpath.add(jarBaseFile);
                    return classpath;
                }
            }
            FileUtil.del(tempDir);
            tempDir.mkdir();
        }


        // 获取当前 jar 文件路径
        File jarFile = new File(URLDecoder.decode(jarBaseFile, "UTF-8"));

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("BOOT-INF/lib/") && entry.getName().endsWith(".jar")) {
                    // 提取每个依赖 jar 到 tempDir
                    File dest = new File(tempDir, entry.getName().replace("BOOT-INF/lib/", ""));
                    try (InputStream is = jar.getInputStream(entry)) {
                        Files.copy(is, dest.toPath());
                    }
                    classpath.add(dest.getAbsolutePath());
                }
            }
        }
        classpath.add(jarFile.getAbsolutePath());
        jarFileIsExtract = true;
        return classpath;
    }

    public static TreeSet<String> loadLocalClassPath() {
        TreeSet<String> classpath = new TreeSet<>();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        List<Object> jars = new ArrayList<>();
        while (true) {
            if (contextClassLoader == null) break;
            Object ucp = ReflectUtil.getFieldValue(contextClassLoader, "ucp");
            List<Object> loaders = (ArrayList) ReflectUtil.getFieldValue(ucp, "loaders");
            if (CollUtil.isNotEmpty(loaders)) {
                jars.addAll(loaders);
            }
            contextClassLoader = contextClassLoader.getParent();
        }
        List<String> baseUrls = jars.stream().map(loader -> {
            String baseUrl = Convert.toStr(ReflectUtil.getFieldValue(loader, "base"));
            if (StrUtil.startWith(baseUrl, "file:/")) {
                return StrUtil.replace(baseUrl, "file:/", "");
            }
            if (StrUtil.startWith(baseUrl, "jar:file:/")) {
                String replace = StrUtil.replace(baseUrl, "jar:file:/", "");
                return StrUtil.replace(replace, ".jar!/", ".jar");
            }
            return baseUrl;
        }).collect(Collectors.toList());
        //jars.stream()
        classpath.addAll(baseUrls);
        return classpath;
    }
}