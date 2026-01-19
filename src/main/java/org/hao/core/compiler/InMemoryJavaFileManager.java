package org.hao.core.compiler;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * 内存中的Java文件管理器，用于在内存中编译和存储Java类文件，避免写入磁盘
 * 继承ForwardingJavaFileManager以包装现有的JavaFileManager并添加内存编译功能
 */
public class InMemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    /**
     * 存储已编译的类文件对象，键为类名，值为对应的字节码文件对象
     */
    private final Map<String, ByteCodeJavaFileObject> compiledClasses = new HashMap<>();

    /**
     * 构造函数，初始化内存Java文件管理器
     * @param fileManager 被包装的原始Java文件管理器
     */
    public InMemoryJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    /**
     * 获取已编译的类文件映射
     * @return 包含类名到字节码文件对象映射的Map
     */
    public Map<String, ByteCodeJavaFileObject> getCompiledClasses() {
        return compiledClasses;
    }

    /**
     * 列出指定位置、包名和类型的Java文件
     * @param location 文件位置（如CLASS_PATH、SOURCE_PATH等）
     * @param packageName 包名
     * @param kinds 要查找的文件类型集合
     * @param recurse 是否递归搜索子包
     * @return Java文件对象的可迭代集合
     * @throws IOException 当发生IO错误时抛出
     */
    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        Iterable<JavaFileObject> list = super.list(location, packageName, kinds, recurse);
        return list;
    }

    /**
     * 从Class对象获取字节码数组
     * @param clazz 要获取字节码的Class对象
     * @return 类的字节码数组
     * @throws IOException 当读取字节码失败时抛出
     */
    public static byte[] getClassBytes(Class<?> clazz) throws IOException {
        String resourceName = clazz.getName().replace('.', '/') + ".class";
        try (InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + resourceName);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    /**
     * 获取用于输出的Java文件对象，将编译结果存储在内存中
     * @param location 输出位置
     * @param className 类名
     * @param kind 文件类型
     * @param sibling 相关的文件对象（可选）
     * @return 用于写入字节码的Java文件对象
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
        ByteCodeJavaFileObject file = new ByteCodeJavaFileObject(className, kind);
        compiledClasses.put(className, file);
        return file;
    }


    /**
     * 内存中的字节码文件对象，用于在内存中存储编译后的类字节码
     */
    public static class ByteCodeJavaFileObject extends SimpleJavaFileObject {
        /**
         * 存储字节码的字节数组输出流
         */
        private ByteArrayOutputStream bytecode = new ByteArrayOutputStream();

        /**
         * 构造函数，创建内存字节码文件对象
         * @param name 文件名称
         * @param kind 文件类型
         */
        ByteCodeJavaFileObject(String name, JavaFileObject.Kind kind) {
            super(URI.create("bytes:///" + name + kind.extension), kind);
        }

        /**
         * 打开输出流以写入字节码
         * @return 字节码输出流
         */
        @Override
        public OutputStream openOutputStream() {
            return bytecode;
        }

        /**
         * 获取已存储的字节码数组
         * @return 字节码数组
         */
        public byte[] getByteCode() {
            return bytecode.toByteArray();
        }
    }
}
