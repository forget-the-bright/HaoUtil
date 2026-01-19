package org.hao.core.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内存类加载器，用于从内存中加载动态生成的字节码
 */
public class InMemoryClassLoader extends ClassLoader {

    /**
     * 存储类名与对应字节码的映射关系
     */
    public final Map<String, byte[]> classBytes = new HashMap<>();

    /**
     * 构造函数，创建内存类加载器实例
     *
     * @param parent 父类加载器，通常设置为Spring的类加载器
     */
    public InMemoryClassLoader(ClassLoader parent) {
        super(parent); // 设置父类加载器为 Spring 的类加载器
    }

    /**
     * 添加类字节码到内存中
     *
     * @param className 类的全限定名
     * @param bytes     类的字节码数组
     */
    public void addClassBytes(String className, byte[] bytes) {
        classBytes.put(className, bytes);
    }

    /**
     * 查找并加载指定名称的类
     *
     * @param name 类的全限定名
     * @return 加载的Class对象
     * @throws ClassNotFoundException 当类未找到时抛出异常
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classBytes.get(name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name); // 委托给父类加载器
    }

    /**
     * 获取所有已加载的类
     *
     * @return 类名与Class对象的映射关系
     * @throws ClassNotFoundException 当类未找到时抛出异常
     */
    public Map<String, Class<?>> getClasses() throws ClassNotFoundException {
        // 将所有已存储的类字节码转换为Class对象并返回映射关系
        Map<String, Class<?>> collect = classBytes.keySet().stream().collect(Collectors.toMap(name -> name, name -> {
            try {
                return findClass(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }));
        return collect;
    }
}
