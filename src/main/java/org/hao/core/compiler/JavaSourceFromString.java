package org.hao.core.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class JavaSourceFromString extends SimpleJavaFileObject {
    private final ByteArrayOutputStream byteCode = new ByteArrayOutputStream();
    private  final String code;
    private  final String className;

    public JavaSourceFromString(String className, String code) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
        this.className = className;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }

   /* @Override
    public OutputStream openOutputStream() {
        return byteCode;
    }*/

    public byte[] getByteCode() {
        return byteCode.toByteArray();
    }
}
