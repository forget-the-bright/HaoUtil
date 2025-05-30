package org.hao.core;

import cn.hutool.core.io.FileUtil;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

/**
 * 本地资源工具类，提供从 JAR 包中加载本地库（如 DLL、SO 文件）的功能。
 *
 * <p>该类封装了将资源文件从类路径复制到临时目录并加载本地库的逻辑，
 * 支持跨平台操作，并确保临时文件在使用后正确删除。</p>
 *
 * @author wanghao
 * @since 2021-12-16
 */

public class NativeUtils {
    /**
     * The minimum length a prefix for a file has to have according to {@link File#createTempFile(String, String)}}.
     */
    private static final int MIN_PREFIX_LENGTH = 3;
    public static final String NATIVE_FOLDER_PATH_PREFIX = "nativeutils";

    /**
     * Temporary directory which will contain the DLLs.
     */
    private static File temporaryDir;

    /**
     * Private constructor - this class will never be instanced
     */
    private NativeUtils() {
    }

    /**
     * Loads library from current JAR archive
     * <p>
     * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted after
     * exiting.
     * Method uses String as filename because the pathname is "abstract", not system-dependent.
     *
     * @param path The path of file inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
     * @throws IOException              If temporary file creation or read/write operation fails
     * @throws IllegalArgumentException If source file (param path) does not exist
     * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than three characters
     *                                  (restriction of {@link File#createTempFile(String, String)}).
     * @throws FileNotFoundException    If the file could not be found inside the JAR.
     */
    public static void loadLibraryFromJar(String path) throws IOException {

        if (null == path || !path.startsWith("/")) {
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");
        }

        // Obtain filename from path
        String[] parts = path.split("/");
        String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

        // Check if the filename is okay
        if (filename == null || filename.length() < MIN_PREFIX_LENGTH) {
            throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
        }

        // Prepare temporary file
        if (temporaryDir == null) {
            temporaryDir = createTempDirectory(NATIVE_FOLDER_PATH_PREFIX);
            temporaryDir.deleteOnExit();
        }

        File temp = new File(temporaryDir, filename);

        try (InputStream is = getInputByClassPath(path)) {
            Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            temp.delete();
            throw e;
        } catch (NullPointerException e) {
            temp.delete();
            throw new FileNotFoundException("File " + path + " was not found inside JAR.");
        }

        try {
            System.load(temp.getAbsolutePath());
        } finally {
            if (isPosixCompliant()) {
                // Assume POSIX compliant file system, can be deleted after loading
                temp.delete();
            } else {
                // Assume non-POSIX, and don't delete until last file descriptor closed
                temp.deleteOnExit();
            }
        }
    }

    private static boolean isPosixCompliant() {
        try {
            return FileSystems.getDefault()
                    .supportedFileAttributeViews()
                    .contains("posix");
        } catch (FileSystemNotFoundException
                | ProviderNotFoundException
                | SecurityException e) {
            return false;
        }
    }

    private static File createTempDirectory(String prefix) throws IOException {
        String tempDir = "";
        if (System.getProperties().getProperty("os.name").toLowerCase().contains("windows")) {
            tempDir = System.getProperty("java.io.tmpdir");
        } else {
            String jar_parent = new File(ResourceUtils.getURL("classpath:").getPath()).getParentFile().getParentFile().getParent();
            tempDir = jar_parent.substring(5);
            System.out.println(tempDir);
        }

        File generatedDir = new File(tempDir, prefix + System.nanoTime());
        File mkdir = FileUtil.mkdir(generatedDir);
        if (!mkdir.exists())
            throw new IOException("Failed to create temp directory " + generatedDir.getName());

        return generatedDir;
    }

    /**
     * 根据相对路径获取资源文件的输入流
     *
     * @param path 相对路径
     * @return InputStream
     */
    public static InputStream getInputByClassPath(String path) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
    }


}
