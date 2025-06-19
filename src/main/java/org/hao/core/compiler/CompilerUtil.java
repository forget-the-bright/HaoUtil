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

/**
 * <p>
 * 编译工具类，提供动态编译 Java 源代码并加载为 Class 对象的功能。
 * 主要用于在运行时将字符串形式的 Java 代码编译为字节码，并通过自定义类加载器加载该类。
 * </p>
 *
 * @author wanghao (helloworldwh@163.com)
 * @since 2025-06-15
 */
public class CompilerUtil {
    /**
     * 用于缓存已加载的类对象，确保类在运行时只被加载一次。
     * Key 是类的全限定名，Value 是对应的 Class 对象。
     */
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

    /**
     * 系统自带的 Java 编译器实例，通过 {@link ToolProvider#getSystemJavaCompiler()} 获取。
     * 如果当前环境不是 JDK（例如使用的是 JRE），则该值可能为 null。
     */
    public static final JavaCompiler SYSTEM_COMPILER = ToolProvider.getSystemJavaCompiler();

    /**
     * 存储编译时所需的类路径信息，保证编译时可以正确引用外部依赖。
     * 使用 TreeSet 是为了保证类路径的唯一性和有序性。
     */
    public static final TreeSet<String> classpath = new TreeSet<>();

    /**
     * 标记依赖的 JAR 文件是否已解压到临时目录，用于避免重复解压。
     * 默认值为 {@code false}，表示尚未解压。
     */
    private static boolean jarFileIsExtract = false;

    /**
     * 存储临时目录的绝对路径名称，用于存放提取出的依赖 JAR 文件。
     * 该目录通常位于系统的临时文件夹下，例如：{@code java.io.tmpdir/tempCompilerDir}。
     */
    private static String tempDirName;

    /**
     * 系统临时文件夹路径，通过 {@link System#getProperties()} 获取。
     * 用于构建存放依赖 JAR 的临时目录。
     */
    private static String tmpdir = System.getProperties().getProperty("java.io.tmpdir");

    /**
     * 使用当前线程上下文类加载器编译并加载指定的 Java 源代码为 Class 对象。
     *
     * @param className 完整的类名（包括包名）
     * @param javaCode  Java 源代码字符串
     * @return 编译后的 Class 对象
     * @throws ClassNotFoundException 如果类未找到
     * @see #compileAndLoadClass(String, String, ClassLoader)
     */
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
            /*
            * 补充了编译时启用 lombok或 其他注解生产库 的内容。java 8 需要系统库添加jdk环境的 tools.jar,在cp中或者jre的lib里面添加都可以
            * 大于java 8 的环境jdk 默认移除了tools.jar 并且jre中默认集成此环境,无需过多配置就可以使用 注解类生产库 功能。
            *
            * -processorpath 指定注解处理器的类路径，用于处理注解类,但是实际测试中有无此配置并没有效果，是否启用注解生成还是看jdk版本和tools.jar.
            * 这里保留了此配置, 但是基本可以忽略掉, 因为没有效果.
            */
            List<String> lombokJar = classpath.stream().filter(q -> q.contains("lombok")).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(lombokJar)){
                options.add("-processorpath");
                options.add(StrUtil.join(File.pathSeparator, lombokJar));
            }
        }
        // 获取一个编译任务实例
        // 此处省略了SYSTEM_COMPILER和fileManager的初始化过程
        JavaCompiler.CompilationTask task = SYSTEM_COMPILER.getTask((Writer) null, // 不使用Writer对象
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
     * 根据指定的类名从缓存中获取对应的 Class 对象，并通过反射创建其实例。
     *
     * @param className 完整类名（含包路径）
     * @param params    构造函数所需的参数列表，用于匹配相应的构造方法
     * @return 返回创建的类实例
     * @throws Exception 如果类未找到、构造方法匹配失败或实例化过程中发生异常
     */
    public static Object createInstance(String className, Object... params) throws Exception {
        Class<?> clazz = classCache.get(className);
        if (clazz == null) {
            throw new IllegalArgumentException("Class not found: " + className);
        }
        return ReflectUtil.newInstance(clazz, params);
    }


    /**
     * 将编译过程中收集到的诊断信息转换为可读的字符串形式。
     *
     * @param collector 用于收集编译诊断信息的对象，不可为 null
     * @return 包含所有诊断信息的字符串，每条信息之间以换行符分隔
     */
    public static String getDiagnosticMessages(DiagnosticCollector<?> collector) {
        List<?> diagnostics = collector.getDiagnostics();
        return diagnostics.stream().map(String::valueOf).collect(Collectors.joining(System.lineSeparator()));
    }


    /**
     * 获取当前应用运行时所需的类路径（classpath），用于动态编译时指定依赖。
     *
     * <p>
     * 如果应用是以 JAR 包形式运行，则会提取其依赖 JAR 到临时目录，并将这些路径加入类路径；
     * 如果是以非 JAR 形式运行（如本地开发环境），则加载本地类路径。
     * </p>
     *
     * @return 包含所有必要类路径的有序集合（TreeSet），保证路径唯一且按字母顺序排列
     * @see #extractDependencyJarsToTempDir(String)
     * @see #loadLocalClassPath()
     */
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

    /**
     * 从指定的 JAR 包中提取依赖的外部 JAR 文件到临时目录，并构建类路径。
     *
     * <p>
     * 此方法主要用于在以 JAR 包方式运行时，将嵌套的依赖 JAR 提取到系统临时目录（如 tempCompilerDir），
     * 确保编译器可以正确加载这些依赖库。该方法是线程安全的（synchronized），避免并发执行导致资源冲突。
     * </p>
     *
     * @param jarBaseFile 当前应用主 JAR 文件的路径
     * @return 包含所有依赖 JAR 路径的有序集合（TreeSet），包含主 JAR 自身
     * @throws IOException 如果提取过程中发生 I/O 错误
     */
    public static synchronized TreeSet<String> extractDependencyJarsToTempDir(String jarBaseFile) throws IOException {
        /**
         * 创建或复用已存在的临时目录用于存放提取出的依赖 JAR 文件。
         *
         * <p>
         * 如果是首次提取（jarFileIsExtract == false），则清空旧目录并重新创建；
         * 如果非首次提取且目录存在，则尝试复用已有文件以提升性能，避免重复 I/O 操作。
         * </p>
         *
         * <p>
         * 该逻辑确保在多线程环境下仅解压一次，并通过 classpath 缓存加速后续调用。
         * </p>
         */
        File tempDir = new File(tmpdir + File.separator + "tempCompilerDir");

        // 存储临时目录的绝对路径供外部访问
        tempDirName = tempDir.getAbsolutePath();

        // 初始化类路径集合，用于保存依赖 JAR 的路径
        TreeSet<String> classpath = new TreeSet<>();

        if (!jarFileIsExtract) {
            // 首次提取：删除可能存在的旧目录并新建
            FileUtil.del(tempDir);
            tempDir.mkdir();
        } else {
            // 非首次提取：尝试复用已存在的依赖 JAR 文件
            if (tempDir.exists() || tempDir.isDirectory()) {
                File[] files = tempDir.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        classpath.add(file.getAbsolutePath());
                    }
                }
                if (CollUtil.isNotEmpty(classpath)) {
                    // 若已有可用依赖路径，直接返回缓存结果
                    classpath.add(jarBaseFile);
                    return classpath;
                }
            }
            // 若目录为空或不存在，清理后重建目录
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

    /**
     * 加载本地类路径，用于在非 JAR 包运行模式下获取所有可用的依赖路径。
     *
     * <p>
     * 该段逻辑通过反射访问类加载器内部结构，获取底层的 URLClassLoader 中的 "ucp" 和 "loaders" 字段，
     * 从中提取所有可用的本地 JAR 或目录路径，并转换为标准文件系统格式。
     * </p>
     *
     *
     * 处理步骤如下：
     * <ol>
     *     <li>遍历当前线程上下文类加载器链</li>
     *     <li>通过反射获取每个类加载器中的 URLClassPath 对象（"ucp"）</li>
     *     <li>从中提取 loaders 列表（即每个类路径条目）</li>
     *     <li>将这些路径标准化为文件系统格式并加入 classpath 集合</li>
     * </ol>
     *
     * 支持的 URL 类型包括：
     * <ul>
     *     <li>{@code file:/}：表示本地文件系统路径</li>
     *     <li>{@code jar:file:/}：表示嵌套在 JAR 包内的资源路径</li>
     * </ul>
     *
     *
     * @return 包含本地类路径的有序集合（TreeSet），保证路径唯一且按字母顺序排列
     */
    public static TreeSet<String> loadLocalClassPath() {
        // 创建类路径集合，使用 TreeSet 保证路径唯一且有序
        TreeSet<String> classpath = new TreeSet<>();

        // 获取当前线程上下文类加载器，用于提取运行时依赖路径
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        // 存储所有找到的类加载器条目（通常是 URLClassLoader 的内部结构）
        List<Object> jars = new ArrayList<>();

        // 遍历类加载器链，包括父类加载器，直到为空为止
        while (true) {
            if (contextClassLoader == null) break;

            // 使用反射获取类加载器中的 ucp（URLClassPath）对象
            Object ucp = ReflectUtil.getFieldValue(contextClassLoader, "ucp");

            // 从 ucp 中获取 loaders 列表（每个元素代表一个类路径条目）
            List<Object> loaders = (ArrayList) ReflectUtil.getFieldValue(ucp, "loaders");

            // 如果有可用的加载器条目，则加入集合
            if (CollUtil.isNotEmpty(loaders)) {
                jars.addAll(loaders);
            }

            // 继续向上查找父类加载器
            contextClassLoader = contextClassLoader.getParent();
        }

        // 将类加载器条目转换为实际路径，并进行标准化处理
        List<String> baseUrls = jars.stream().map(loader -> {
            // 使用反射获取每个 loader 的 base 字段，即原始路径字符串
            String baseUrl = Convert.toStr(ReflectUtil.getFieldValue(loader, "base"));

            // 处理不同协议的路径格式：
            if (StrUtil.startWith(baseUrl, "file:/")) {
                // 去除 file:/ 协议前缀，转换为标准文件路径
                return StrUtil.replace(baseUrl, "file:/", "");
            }
            if (StrUtil.startWith(baseUrl, "jar:file:/")) {
                // 去除 jar:file:/ 前缀，并将 .jar!/ 替换为 .jar，得到可识别的 JAR 路径
                String replace = StrUtil.replace(baseUrl, "jar:file:/", "");
                return StrUtil.replace(replace, ".jar!/", ".jar");
            }
            // 默认返回原始路径（适用于未知协议或已处理过的路径）
            return baseUrl;
        }).collect(Collectors.toList());

        // 将标准化后的路径添加到类路径集合中
        classpath.addAll(baseUrls);

        // 返回最终构建的类路径集合
        return classpath;

    }
}