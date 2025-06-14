package org.hao.core.compiler;

import java.util.HashMap;
import java.util.Map;

public class InMemoryClassLoader extends ClassLoader {

    private final Map<String, byte[]> classBytes = new HashMap<>();

    public InMemoryClassLoader(ClassLoader parent) {
        super(parent); // 设置父类加载器为 Spring 的类加载器
    }
    public void addClassBytes(String className, byte[] bytes) {
        classBytes.put(className, bytes);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classBytes.get(name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name); // 委托给父类加载器
    }
}
