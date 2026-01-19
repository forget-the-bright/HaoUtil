package org.hao.core.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.net.URI;

/**
 * 从字符串创建Java源文件对象的类
 * 继承自SimpleJavaFileObject，用于在内存中处理Java源代码
 */
public class JavaSourceFromString extends SimpleJavaFileObject {
    private final ByteArrayOutputStream byteCode = new ByteArrayOutputStream();
    private  final String code;
    private  final String className;

    /**
     * 构造函数，创建Java源文件对象
     *
     * @param className 类名，用于构建URI和标识该源文件
     * @param code Java源代码字符串
     */
    public JavaSourceFromString(String className, String code) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
        this.className = className;
    }

    /**
     * 获取字符内容
     *
     * @param ignoreEncodingErrors 是否忽略编码错误
     * @return 源代码字符序列
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }

   /* @Override
    public OutputStream openOutputStream() {
        return byteCode;
    }*/

    /**
     * 获取字节码数组
     *
     * @return 编译后的字节数组
     */
    public byte[] getByteCode() {
        return byteCode.toByteArray();
    }
}
