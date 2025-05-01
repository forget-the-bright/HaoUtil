package org.hao.core;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.style.StyleUtil;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.*;

/**
 * Description TODO
 * Author wanghao(helloworlwh @ 163.com)
 * Date 2024/11/25 下午2:15
 */
public class HutoolPlus {

    //region 下载相关

    /**
     * 设置文件名和响应类型，用于文件下载
     * 此方法主要用于处理文件下载时的HTTP响应头设置，确保文件名能够正确编码，避免中文名乱码问题
     *
     * @param fileName 文件名，可以包含中文字符
     * @param response HTTP响应对象，用于设置响应头和内容类型
     */
    @SneakyThrows
    public static void setFileName(String fileName, HttpServletResponse response) {
        // 设置Content-Disposition响应头，用于指定文件名，文件名使用UTF-8编码以支持中文
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        // 设置响应的内容类型，指定为Excel格式，同时设置字符集为utf-8
        response.setContentType(getContentTypeForExtension(fileName));
    }

    private static String getContentTypeForExtension(String fileName) {
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


    //region excel相关

    /**
     * 合并单元格并写入内容
     * 该方法用于合并指定列范围的单元格，并写入指定内容
     * 如果内容不为空，则在设置内容后跳转到下一行
     *
     * @param writer      ExcelWriter对象，用于操作Excel文件
     * @param firstColumn 起始列索引，从0开始
     * @param lastColumn  结束列索引，从0开始
     * @param content     要写入的单元格内容，可以是任何对象类型
     * @return 返回操作后的ExcelWriter对象
     */
    public static ExcelWriter merge(ExcelWriter writer, int firstColumn, int lastColumn, Object content) {
        // 获取当前行索引
        final int rowIndex = writer.getCurrentRow();
        // 执行合并单元格并写入内容的操作，参数true表示需要创建单元格
        writer.merge(rowIndex, rowIndex, firstColumn, lastColumn, content, true);
        // 设置内容后跳到下一行
        if (null != content) {
            writer.passCurrentRow();
        }
        // 返回操作后的ExcelWriter对象
        return writer;
    }

    /**
     * 合并单元格并填充内容
     * 该方法用于在ExcelWriter对象中合并指定行范围内的单元格，并填充指定内容
     * 主要解决了在Excel中合并单元格并写入数据的需求
     *
     * @param writer        ExcelWriter对象，用于操作Excel文件
     * @param firstRow      要合并的单元格区域的起始行号
     * @param lastRow       要合并的单元格区域的结束行号
     * @param CurrentColumn 要合并的单元格所在的列号
     * @param content       要填充到合并单元格中的内容
     * @return 返回执行合并和填充操作后的ExcelWriter对象
     */
    public static ExcelWriter merge(ExcelWriter writer, int firstRow, int lastRow, int CurrentColumn, Object content) {
        // 执行合并单元格并填充内容的操作，最后一个参数设为true以启用自动样式调整
        writer.merge(firstRow, lastRow, CurrentColumn, CurrentColumn, content, true);
        // 返回执行操作后的ExcelWriter对象
        return writer;
    }

    /**
     * 为指定行设置样式
     *
     * @param writer ExcelWriter对象，用于操作Excel文件
     * @param style  CellStyle对象，表示要应用的单元格样式
     * @param rows   可变参数，表示需要设置样式的行号
     *               <p>
     *               此方法遍历给定的行号，并将指定的样式应用到每一行
     *               如果没有提供行号（即rows数组长度为0），则不执行任何操作
     */
    public static void setStyleForRow(ExcelWriter writer, CellStyle style, int... rows) {
        // 检查是否有行号提供，如果没有，则不进行任何操作
        if (rows.length == 0) {
            return;
        }
        // 遍历每个提供的行号，并为每一行应用指定的样式
        Arrays.stream(rows).forEach(row -> {
            writer.getSheet().getRow(row).setRowStyle(style);
        });
    }

    /**
     * 设置Excel表格中指定区域的单元格样式
     *
     * @param writer ExcelWriter对象，用于访问和操作Excel文件
     * @param style CellStyle对象，定义了要应用到单元格的样式
     * @param beginRow 起始行号，指定样式应用的开始行
     * @param endRow 结束行号，指定样式应用的结束行
     * @param beginColumn 起始列号，指定样式应用的开始列
     * @param endColumn 结束列号，指定样式应用的结束列
     * @throws RuntimeException 如果结束行小于起始行或结束列小于起始列，则抛出运行时异常
     */
    public static void setStyle(ExcelWriter writer, CellStyle style, int beginRow, int endRow, int beginColumn, int endColumn) {
        // 获取ExcelWriter对象关联的Sheet对象
        Sheet sheet = writer.getSheet();

        // 检查结束行是否小于起始行，如果是，则抛出异常
        if (endRow < beginRow) {
            throw new RuntimeException("endRow must be greater than beginRow");
        }

        // 检查结束列是否小于起始列，如果是，则抛出异常
        if (endColumn < beginColumn) {
            throw new RuntimeException("endColumn must be greater than beginColumn");
        }

        // 遍历指定行范围内的每一行
        for (int i = beginRow; i <= endRow; i++) {
            // 获取当前行的Row对象
            Row row = sheet.getRow(i);

            // 遍历指定列范围内的每一列
            for (int j = beginColumn; j <= endColumn; j++) {
                // 获取当前单元格的Cell对象
                Cell cell = row.getCell(j);

                // 将指定的样式应用到当前单元格
                cell.setCellStyle(style);
            }
        }
    }

    /**
     * 根据ExcelWriter对象获取InputStream输入流
     * 此方法用于将ExcelWriter对象中的工作簿转换为InputStream输入流，便于进一步处理或传输
     *
     * @param writer ExcelWriter对象，用于写入Excel数据
     * @return InputStream输入流，包含Excel数据
     * @throws IOException 当创建工作簿的输入流时发生I/O错误
     */
    public static InputStream getInputStreamByExcelWriter(ExcelWriter writer) throws IOException {
        // 获取ExcelWriter对象中的Workbook工作簿
        Workbook workbook = writer.getWorkbook();
        // 通过Workbook工作簿获取InputStream输入流
        InputStream is = getInputStreamByWorkbook(workbook);
        // 返回包含Excel数据的输入流
        return is;
    }


    /**
     * 通过Workbook对象获取InputStream
     * 该方法主要用于将Workbook对象转换为InputStream，以便于进一步的操作，如文件下载等
     *
     * @param workbook Excel工作簿对象，包含了Excel文件的全部信息
     * @return InputStream 输入流，可以通过它来读取Excel文件的内容
     */
    private static InputStream getInputStreamByWorkbook(Workbook workbook) {
        InputStream is = null;
        try {
            // 创建一个字节输出流，用于暂存Workbook对象写入的字节数据
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // 将Workbook对象写入到字节输出流中
            workbook.write(bos);
            // 从字节输出流中获取字节数组
            byte[] barray = bos.toByteArray();
            // 使用获取到的字节数组创建一个字节输入流
            is = new ByteArrayInputStream(barray);

        } catch (IOException e) {
            // 打印异常信息，此处简单处理异常，实际使用中可根据需要进行更详细的异常处理
            e.printStackTrace();
        }
        // 返回字节输入流
        return is;
    }

    /**
     * 根据导出数据和表头信息获取ExcelWriter对象
     * 该方法主要用于简化Excel导出过程，通过传入数据和表头信息，自动完成Excel表的格式设置和数据写入
     *
     * @param hashMaps    一个包含多行数据的列表，每行数据为一个Map，键为字段名，值为字段值
     * @param tableHeader 一个LinkedHashMap，用于定义Excel表的表头，键为字段名，值为表头显示文本
     * @param title       标题
     * @return 返回一个ExcelWriter对象，用于进一步操作Excel文件，如输出到文件或流
     */
    public static ExcelWriter getExcelWriterByExportExcelVO(List<? extends Map<String, Object>> hashMaps, LinkedHashMap<String, String> tableHeader, String title) {
        // Step.3 AutoPoi 导出Excel
        // 通过工具类创建writer，默认创建xls格式
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //自定义标题别名
        tableHeader.forEach((k, v) -> {
            writer.addHeaderAlias(k, v);
        });
        // 合并单元格后的标题行，使用默认标题样式

        writer.merge(tableHeader.size() - 1, title);
        Font font = writer.createFont();
        font.setBold(true);
        font.setColor(Font.COLOR_NORMAL);
        font.setItalic(true);
        //第二个参数表示是否忽略头部样式
        writer.getHeadCellStyle().setFont(font);
        // 一次性写出内容，使用默认样式，强制输出标题
        writer.write(hashMaps, true);
        setSizeColumn(writer.getSheet(), tableHeader.size() - 1);
        StyleUtil.setAlign(writer.getCellStyle(), HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        /*        writer.getStyleSet().setAlign(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);*/
        //out为OutputStream，需要写出到的目标流
        //response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        return writer;
    }

    /**
     * 根据导出数据和表头信息获取ExcelWriter对象
     * 此方法用于将给定的数据和表头信息转换为一个ExcelWriter对象，以便进一步操作，如写入到Excel文件中
     * 它调用了另一个重载方法getExcelWriterByExportExcelVO，固定了导出标题为"导出标题"
     *
     * @param hashMaps    包含导出数据的列表，每个元素是一个Map，键为列名，值为单元格数据
     * @param tableHeader 表头信息，键为列名，值为在Excel表头中显示的名称
     * @return ExcelWriter对象，用于后续的Excel文件写入操作
     */
    public static ExcelWriter getExcelWriterByExportExcelVO(List<? extends Map<String, Object>> hashMaps, LinkedHashMap<String, String> tableHeader) {
        return getExcelWriterByExportExcelVO(hashMaps, tableHeader, "导出标题");
    }


    /**
     * 自适应宽度(中文支持)
     *
     * 根据sheet中各列的字符串长度，自动调整列宽，以确保中文和英文都能完整显示
     * 此方法旨在解决当单元格内容长度不一时，如何合理设置列宽的问题
     *
     * @param sheet Excel工作表中的Sheet对象，用于操作Excel表格中的数据
     * @param size  需要调整列宽的列数，因为for循环从0开始，size值为 列数-1
     */
    public static void setSizeColumn(Sheet sheet, int size) {
        //遍历所有列，调整每列的宽度
        for (int columnNum = 0; columnNum <= size; columnNum++) {
            //获取当前列的初始宽度，并将其转换为单位为字符的宽度
            int columnWidth = sheet.getColumnWidth(columnNum) / 256;
            //遍历当前列的所有行，寻找最长的字符串长度
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row currentRow;
                //当前行未被使用过
                if (sheet.getRow(rowNum) == null) {
                    //创建新行
                    currentRow = sheet.createRow(rowNum);
                } else {
                    //使用已有行
                    currentRow = sheet.getRow(rowNum);
                }

                //检查当前单元格是否已存在
                if (currentRow.getCell(columnNum) != null) {
                    Cell currentCell = currentRow.getCell(columnNum);
                    //根据单元格类型调整列宽
                    if (currentCell.getCellType() == CellType.STRING) {
                        //计算字符串长度，确保中文和英文都能正确计算长度
                        int length = currentCell.getStringCellValue().getBytes().length;
                        //如果当前列宽小于字符串长度，则更新列宽为字符串长度
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    } else {
                        //对于非字符串类型，适当增加列宽以留出额外空间
                        columnWidth = columnWidth + 5;
                    }
                }
            }
            //如果计算出的列宽大于60个字符宽度，则设置为60个字符宽度，防止列宽过大
            if (columnWidth > 60) {
                sheet.setColumnWidth(columnNum, 60 * 256);//columnWidth * 256
            } else {
                //否则，将列宽设置为计算出的宽度
                sheet.setColumnWidth(columnNum, columnWidth * 256);//columnWidth * 256
            }

        }
    }
    //endregion

    /**
     * 根据相对路径获取资源文件的输入流
     *
     * @param path 相对路径
     * @return InputStream
     */
    public static InputStream getInputByClassPath(String path) {
        // 使用当前线程的上下文类加载器获取资源文件的输入流
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
    }
    //region 数字相关

    /**
     * 对double类型数值进行四舍五入操作
     * 此方法旨在处理需要对浮点数进行精确舍入的场景，由于double类型的精度问题，
     * 使用BigDecimal来确保舍入操作的精确性
     *
     * @param v     待舍入的double类型数值
     * @param scale 舍入后的规模（小数点后的位数）
     * @return 舍入后的BigDecimal对象
     */
    public static BigDecimal round(double v, int scale) {
        // 当数值大于等于1.0时，直接使用setScale方法进行四舍五入
        if (v >= 1.0) {
            return new BigDecimal(v).setScale(scale, RoundingMode.HALF_UP);
        }
        // 当数值小于1.0时，使用round方法结合MathContext进行四舍五入，以保持有效数字的精度
        return new BigDecimal(v).round(new MathContext(scale, RoundingMode.HALF_UP));
    }


    /**
     * 对指定数值进行四舍五入操作
     *
     * @param v     待四舍五入的数值，以字符串形式表示，以支持精确计算
     * @param scale 小数点后的位数，表示需要保留的小数位
     * @return 四舍五入后的结果，以BigDecimal形式返回
     */
    public static BigDecimal round(String v, int scale) {
        // 创建BigDecimal对象，用于进行精确计算
        BigDecimal bigDecimal = new BigDecimal(v);
        // 判断数值是否大于等于1.0，以决定使用setScale还是round方法
        if (bigDecimal.doubleValue() >= 1.0) {
            // 如果大于等于1.0，使用setScale方法设置小数点后的位数，并进行四舍五入
            return bigDecimal.setScale(scale, RoundingMode.HALF_UP);
        }  // 如果小于1.0，使用round方法进行四舍五入，并指定精度和四舍五入模式
        return bigDecimal.round(new MathContext(scale, RoundingMode.HALF_UP));
    }


    /**
     * 将双精度浮点数四舍五入到指定小数位数，并以字符串形式返回
     * 此方法旨在处理不同情况下的双精度浮点数，包括大于等于1、等于0、不是数字（NaN）以及其他情况
     * 对于大于等于1的情况，直接使用setScale方法进行四舍五入
     * 对于等于0或不是数字的情况，直接返回"0.0"
     * 对于其他情况，使用MathContext进行四舍五入
     *
     * @param v     待处理的双精度浮点数
     * @param scale 小数点后的位数
     * @return 四舍五入后的字符串表示的数字
     */
    public static String roundStr(Double v, int scale) {
        // 保留有效数字两位，四舍五入
        if (v >= 1.0) {
            // 对于大于等于1的数字，直接设置小数点后的位数并四舍五入
            return new BigDecimal(v.toString()).setScale(scale, RoundingMode.HALF_UP).toString();
        } else if (v == 0.0) {
            // 对于等于0的情况，直接返回"0.0"
            return "0.0";
        } else if (Double.isNaN(v)) {
            // 对于不是数字的情况，也返回"0.0"
            return "0.0";
        }
        // 对于其他情况，使用MathContext进行四舍五入
        return new BigDecimal(v.toString()).round(new MathContext(scale, RoundingMode.HALF_UP)).toPlainString();
    }


    /**
     * 将给定的数值字符串四舍五入到指定的小数位数
     * 如果数值大于等于1，则直接四舍五入并返回结果
     * 如果数值等于0，则返回"0.0"，以确保至少有一位小数
     * 如果数值小于1且不等于0，则使用指定的有效数字位数进行四舍五入
     *
     * @param v     待处理的数值字符串
     * @param scale 小数位数或有效数字位数
     * @return 四舍五入后的数值字符串
     */
    public static String roundStr(String v, int scale) {
        // 创建BigDecimal对象以进行精确计算
        BigDecimal bigDecimal = new BigDecimal(v);
        // 当数值大于等于1时，直接四舍五入到指定小数位数
        if (bigDecimal.doubleValue() >= 1.0) {
            return bigDecimal.setScale(scale, RoundingMode.HALF_UP).toString();
        } else if (bigDecimal.doubleValue() == 0.0) {
            // 当数值等于0时，确保返回的字符串至少有一位小数
            return "0.0";
        }
        // 当数值小于1且不等于0时，按有效数字位数进行四舍五入
        return bigDecimal.round(new MathContext(scale, RoundingMode.HALF_UP)).toPlainString();
    }


    /**
     * 将double类型数值转换为字符串表示，特别处理特定数值情况
     *
     * @param v 待转换的double类型数值
     * @return 转换后的字符串表示，对于特殊值有特定处理
     */
    public static String doubleStr(double v) {
        // 当输入值为0.0时，直接返回"0.0"，避免因浮点数精度问题导致的不必要转换
        if (v == 0.0) {
            return "0.0";
            // 当输入值为非数字（NaN）时，返回"0.0"，统一处理无效数值输入
        } else if (Double.isNaN(v)) {
            return "0.0";
        }
        // 对于有效输入，使用BigDecimal进行转换和四舍五入，然后移除不必要的尾随零
        // 这样做可以得到一个精确且格式化的字符串表示
        return removeTrailingZeros(new BigDecimal(v).setScale(10, RoundingMode.HALF_UP).toString());
    }


    /**
     * 将字符串表示的数字转换为double类型，并格式化为字符串
     * 此方法主要用于处理财务计算等场景，需要高精度和正确舍入
     *
     * @param v 输入的字符串表示的数字
     * @return 格式化后的字符串表示的数字
     */
    public static String doubleStr(String v) {
        // 创建BigDecimal对象，并设置小数点后10位，使用四舍五入模式
        BigDecimal bigDecimal = new BigDecimal(v).setScale(10, RoundingMode.HALF_UP);

        // 如果值为0.0，返回"0.0"字符串，保证返回结果的一致性
        if (bigDecimal.doubleValue() == 0.0) {
            return "0.0";
        }

        // 移除BigDecimal.toString()产生的尾随零，简化输出格式
        return removeTrailingZeros(bigDecimal.toString());
    }


    /**
     * 移除字符串末尾的零
     * 该方法用于处理字符串形式的数值，移除其末尾的零，直到遇到非零字符或小数点
     * 如果字符串以小数点结尾，也会移除小数点
     *
     * @param value 待处理的字符串数值
     * @return 移除末尾零和可能的小数点后的字符串数值
     */
    public static String removeTrailingZeros(String value) {
        // 检查字符串中是否包含小数点，以确定是否需要进行处理
        if (value.contains(".")) {
            // 循环移除字符串末尾的零
            while (value.endsWith("0")) {
                value = value.substring(0, value.length() - 1);
            }
            // 如果字符串以小数点结尾，则移除小数点
            if (value.endsWith(".")) {
                value = value.substring(0, value.length() - 1);
            }
        }
        // 返回处理后的字符串
        return value;
    }
    //endregion


    //region 时间相关

    /**
     * 判断字符串是否符合日期的月份格式
     *
     * @param time 待验证的字符串
     * @return 如果字符串符合yyyy-MM的格式，返回true；否则返回false
     */
    public static Boolean isDateMonthStr(String time) {
        // 使用正则表达式匹配年份和月份的格式
        // ^\d{4}-(0[1-9]|1[0-2])$ 表示以4位数字开始，后跟一个短横线，然后是01到12之间的两位数字
        return ReUtil.isMatch("^\\d{4}-(0[1-9]|1[0-2])$", time);
    }


    /**
     * 检验字符串是否为有效的日期时间格式
     * 该方法主要用于验证输入的字符串是否符合"yyyy-MM-dd"的日期格式
     * 这里的有效性检查指的是字符串是否严格按照指定的格式排列，并不涉及日期的实际存在性（例如2月30日）
     *
     * @param time 待验证的字符串
     * @return 如果字符串符合"yyyy-MM-dd"格式，则返回true；否则返回false
     */
    public static boolean isDateTimeStr(String time) {
        // 使用正则表达式匹配日期时间格式，格式为"yyyy-MM-dd"
        // 其中，^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$ 表示：
        // ^ 表示字符串的开始
        // \d{4} 表示四位数字的年份
        // (0[1-9]|1[0-2]) 表示月份，01到09或者10到12
        // (0[1-9]|[1-2][0-9]|3[0-1]) 表示日期，01到09、10到29或者30到31
        // $ 表示字符串的结束
        // 这个正则表达式用于验证字符串是否符合"yyyy-MM-dd"的格式
        return ReUtil.isMatch("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$", time);
    }


    /**
     * 检查字符串是否符合日期年份的格式
     * 该方法用于验证给定的字符串是否表示一个四位数的年份
     *
     * @param time 待验证的字符串
     * @return 如果字符串符合年份格式 yyyy（四位数字），则返回true；否则返回false
     */
    public static Boolean isDateYearStr(String time) {
        // 使用正则表达式匹配四位数字的年份格式
        return ReUtil.isMatch("^\\d{4}$", time);
    }


    public static DateField convertDateField(String queryType) {
        if (StrUtil.equals(queryType, "month")) {
            return DateField.MONTH;
        }
        if (StrUtil.equals(queryType, "day")) {
            return DateField.DAY_OF_YEAR;
        }
        if (StrUtil.equals(queryType, "hour")) {
            return DateField.HOUR;
        }
        if (StrUtil.equals(queryType, "minute")) {
            return DateField.MINUTE;
        }
        if (StrUtil.equals(queryType, "second")) {
            return DateField.SECOND;
        }
        if (StrUtil.equals(queryType, "week")) {
            return DateField.WEEK_OF_YEAR;
        }
        return null;
    }

    public static DateTime beginOf(Date date, String dateField) {
        return beginOf(date, convertDateField(dateField));
    }

    public static DateTime beginOf(Date date, DateField dateField) {
        if (dateField == DateField.YEAR) {
            return cn.hutool.core.date.DateUtil.beginOfYear(date);
        }
        if (dateField == DateField.MONTH) {
            return cn.hutool.core.date.DateUtil.beginOfMonth(date);
        }
        if (dateField == DateField.DAY_OF_MONTH ||
                dateField == DateField.DAY_OF_WEEK ||
                dateField == DateField.DAY_OF_WEEK_IN_MONTH ||
                dateField == DateField.DAY_OF_YEAR) {
            return cn.hutool.core.date.DateUtil.beginOfDay(date);
        }
        if (dateField == DateField.WEEK_OF_MONTH ||
                dateField == DateField.WEEK_OF_YEAR) {
            return cn.hutool.core.date.DateUtil.beginOfWeek(date);
        }
        if (dateField == DateField.HOUR || dateField == DateField.HOUR_OF_DAY) {
            return cn.hutool.core.date.DateUtil.beginOfHour(date);
        }
        if (dateField == DateField.MINUTE) {
            return cn.hutool.core.date.DateUtil.beginOfMinute(date);
        }
        if (dateField == DateField.SECOND) {
            return cn.hutool.core.date.DateUtil.beginOfSecond(date);
        }
        return new DateTime(date);
    }

    public static DateTime endOf(Date date, String dateField) {
        return endOf(date, convertDateField(dateField));
    }

    public static DateTime endOf(Date date, DateField dateField) {
        if (dateField == DateField.YEAR) {
            return cn.hutool.core.date.DateUtil.endOfYear(date);
        }
        if (dateField == DateField.MONTH) {
            return cn.hutool.core.date.DateUtil.endOfMonth(date);
        }
        if (dateField == DateField.DAY_OF_MONTH ||
                dateField == DateField.DAY_OF_WEEK ||
                dateField == DateField.DAY_OF_WEEK_IN_MONTH ||
                dateField == DateField.DAY_OF_YEAR) {
            return cn.hutool.core.date.DateUtil.endOfDay(date);
        }
        if (dateField == DateField.WEEK_OF_MONTH ||
                dateField == DateField.WEEK_OF_YEAR) {
            return cn.hutool.core.date.DateUtil.endOfWeek(date);
        }
        if (dateField == DateField.HOUR || dateField == DateField.HOUR_OF_DAY) {
            return cn.hutool.core.date.DateUtil.endOfHour(date);
        }
        if (dateField == DateField.MINUTE) {
            return cn.hutool.core.date.DateUtil.endOfMinute(date);
        }
        if (dateField == DateField.SECOND) {
            return DateUtil.endOfSecond(date);
        }
        return new DateTime(date);
    }
    //endregion

    /**
     * 根据格式化字符串和参数抛出RuntimeException
     * 此方法用于简化异常的抛出过程，通过接受一个格式化字符串和一组参数，
     * 使用StrFormatter.format方法格式化字符串，然后抛出包含格式化信息的RuntimeException
     *
     * @param strPattern 格式化字符串的模式这通常是一个包含占位符的字符串，
     *                   用于说明如何插入参数以构建最终的字符串
     * @param argArray   一组参数，用于替换格式化字符串中的占位符这些参数
     *                   应按照它们在格式化字符串中出现的顺序提供
     *                   <p>
     *                   注意：这个方法的设计允许在抛出异常时提供更灵活和丰富的错误信息，
     *                   它抽象了异常信息的构建过程，使得调用者可以以一种更简洁和可读的方式
     *                   提供异常的上下文信息
     */
    public static void throwRuntimEx(String strPattern, Object... argArray) {
        throw new RuntimeException(StrFormatter.format(strPattern, argArray));
    }

}
