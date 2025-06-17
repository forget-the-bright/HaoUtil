package org.hao.core.compiler;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class InMemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, ByteCodeJavaFileObject> compiledClasses = new HashMap<>();

    public InMemoryJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    public Map<String, ByteCodeJavaFileObject> getCompiledClasses() {
        return compiledClasses;
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        Iterable<JavaFileObject> list = super.list(location, packageName, kinds, recurse);
        return list;
    }

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

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
        ByteCodeJavaFileObject file = new ByteCodeJavaFileObject(className, kind);
        compiledClasses.put(className, file);
        return file;
    }


    // 内存中的字节码文件对象
    public static class ByteCodeJavaFileObject extends SimpleJavaFileObject {
        private ByteArrayOutputStream bytecode = new ByteArrayOutputStream();

        ByteCodeJavaFileObject(String name, JavaFileObject.Kind kind) {
            super(URI.create("bytes:///" + name + kind.extension), kind);
        }

        @Override
        public OutputStream openOutputStream() {
            return bytecode;
        }

        public byte[] getByteCode() {
            return bytecode.toByteArray();
        }
    }
}
