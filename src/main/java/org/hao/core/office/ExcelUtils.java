package org.hao.core.office;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.style.StyleUtil;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel工具类，提供Excel文件的各种操作功能，包括合并单元格、设置样式、数据导出等功能
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/20 16:26
 */
public class ExcelUtils {

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
}
