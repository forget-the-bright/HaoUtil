package org.hao.core;

import java.io.InputStream;

/**
 * 文件工具类，提供文件相关的操作方法
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/21 11:36
 */
public class FileUtils {
    /**
     * 根据相对路径获取资源文件的输入流
     *
     * @param path 相对路径
     * @return InputStream 资源文件的输入流，如果找不到则返回null
     */
    public static InputStream getInputByClassPath(String path) {
        // 使用当前线程的上下文类加载器获取资源文件的输入流
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
    }

}
