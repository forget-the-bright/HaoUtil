package org.hao.core.web;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.SneakyThrows;
import org.hao.annotation.FailSafeRule;
import org.hao.core.failsafe.FailSafeHandler;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * TODO
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/20 16:29
 */
public class DownloadUtils {
    //region 下载相关

    /**
     * 设置文件名和响应类型，用于文件下载
     * 此方法主要用于处理文件下载时的HTTP响应头设置，确保文件名能够正确编码，避免中文名乱码问题
     *
     * @param fileName 文件名，可以包含中文字符
     * @param response HTTP响应对象，用于设置响应头和内容类型
     */
    @SneakyThrows
    @FailSafeRule(handler = FailSafeHandler.class)
    public static void setFileName(String fileName, HttpServletResponse response) {
        // 设置Content-Disposition响应头，用于指定文件名，文件名使用UTF-8编码以支持中文
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        // 设置响应的内容类型，指定为Excel格式，同时设置字符集为utf-8
        response.setContentType(getContentTypeForExtension(fileName));
    }

    public static String getContentTypeForExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        String fileExtension = "";
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            fileExtension = fileName.substring(dotIndex + 1);
        }
        switch (fileExtension) {
            case "txt":
                return "text/plain";
            case "html":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "text/javascript";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "svg":
                return "image/svg+xml";
            case "ico":
                return "image/x-icon";
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "ogg":
                return "audio/ogg";
            case "flac":
                return "audio/flac";
            case "aac":
                return "audio/aac";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/avi";
            case "mkv":
                return "video/webm";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/windowsmedia";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation;charset=utf-8";
            case "doc":
                return "application/vnd.ms-word";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordml.document;charset=utf-8";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            case "7z":
                return "application/x-7z-compressed";
            case "tar":
                return "application/x-tar";
            case "gz":
                return "application/x-gz";
            case "exe":
            case "dll":
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 使用ExcelWriter将数据下载到客户端
     * 此方法专注于处理Excel文件下载的流程，采用SneakyThrows注解来处理可能的异常
     * 这种方式避免了直接在方法签名中声明异常，保持了方法的简洁性
     *
     * @param writer   ExcelWriter实例，用于写入Excel数据
     * @param response HttpServletResponse对象，用于获取输出流以向客户端发送数据
     */
    @SneakyThrows
    public static void download(ExcelWriter writer, HttpServletResponse response) {
        // 获取响应的输出流，用于向客户端发送Excel数据
        ServletOutputStream outputStream = response.getOutputStream();
        // 将Excel数据写入输出流中，开启自动闭合工作簿
        writer.flush(outputStream, true);
        // 关闭writer，释放内存
        writer.close();
        // 此处记得关闭输出Servlet流
        IoUtil.close(outputStream);
    }


    /**
     * 使用输入流下载文件
     *
     * @param inputStream 文件输入流，用于读取文件数据
     * @param response    HTTP响应对象，用于向客户端输出文件数据
     *                    <p>
     *                    此方法负责将指定的文件数据通过HTTP响应输出给客户端
     *                    它首先从response对象中获取一个输出流，然后将输入流中的数据复制到输出流中
     *                    最后，确保关闭所有打开的流以释放系统资源
     */
    @SneakyThrows
    public static void download(InputStream inputStream, HttpServletResponse response) {
        // 获取响应的输出流，用于向客户端发送文件数据
        ServletOutputStream outputStream = response.getOutputStream();

        // 将输入流中的数据复制到输出流中，实现文件下载
        // 这里使用了1024字节（1kb）作为缓冲区大小，以平衡内存使用和传输效率
        IoUtil.copy(inputStream, outputStream, 1024);

        // 关闭输入流，释放相关资源
        IoUtil.close(inputStream);

        // 关闭输出Servlet流，确保所有数据都已正确写入并释放资源
        IoUtil.close(outputStream);
    }

    //endregion
}
