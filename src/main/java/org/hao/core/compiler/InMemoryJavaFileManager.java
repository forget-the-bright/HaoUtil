package org.hao.core.compiler;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
