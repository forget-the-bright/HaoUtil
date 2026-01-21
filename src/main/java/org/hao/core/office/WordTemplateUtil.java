package org.hao.core.office;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.hao.core.FileUtils;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Word 模板处理工具类
 * 支持多种占位符语法：
 * - 普通文本替换：${key}
 * - 列表循环：#{list.key}
 * - 图片插入：![key]
 * - 条件判断：@{condition?true_value:false_value}
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/21 16:18
 */
public class WordTemplateUtil {

    // 正则表达式
    private static final Pattern TEXT_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Pattern LIST_PLACEHOLDER_PATTERN = Pattern.compile("#\\{([^}]+)\\}");
    private static final Pattern IMG_PLACEHOLDER_PATTERN = Pattern.compile("!\\[([^\\]]+)\\]");
    private static final Pattern CONDITIONAL_PLACEHOLDER_PATTERN = Pattern.compile("@\\{([^}]+)\\}");

    /**
     * 处理 Word 模板
     *
     * @param templatePath 模板路径
     * @param outputPath   输出路径
     * @param params       参数映射
     * @throws IOException 文件操作异常
     */
    public static void processTemplate(String templatePath, String outputPath, Map<String, Object> params) throws IOException {

        try (
                InputStream fis = FileUtils.getInputByClassPath(templatePath);
                XWPFDocument doc = new XWPFDocument(fis);
                FileOutputStream fos = new FileOutputStream(outputPath)
        ) {

            // 处理段落
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                processParagraph(paragraph, params);
            }

            // 处理表格
            for (XWPFTable table : doc.getTables()) {
                processTable(table, params);
            }

            // 处理表格中的段落
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            processParagraph(paragraph, params);
                        }
                    }
                }
            }

            doc.write(fos);
        }
    }

    /**
     * 处理段落
     */
    private static void processParagraph(XWPFParagraph paragraph, Map<String, Object> params) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (CollUtil.isEmpty(runs)) return;

        // 获取完整文本
        StringBuilder fullTextBuilder = new StringBuilder();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null) {
                fullTextBuilder.append(text);
            }
        }
        String fullText = fullTextBuilder.toString();

        if (StrUtil.isBlank(fullText)) return;

        // 处理各种占位符
        String processedText = fullText;
        processedText = processTextPlaceholders(processedText, params);
        processedText = processConditionalPlaceholders(processedText, params);
        processedText = processImagePlaceholders(processedText, params);

        // 清空原有内容
        clearParagraph(paragraph);

        // 添加新内容
        addTextWithStyle(paragraph, processedText, runs.isEmpty() ? null : runs.get(0));
    }

    /**
     * 处理表格
     */
    private static void processTable(XWPFTable table, Map<String, Object> params) {
        List<XWPFTableRow> rows = table.getRows();
        if (CollUtil.isEmpty(rows)) return;

        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);

            // 检查是否是列表模板行
            if (isListTemplateRow(row, params)) {
                processListTemplateRow(table, i, row, params);
            } else {
                // 处理普通行
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        processParagraph(paragraph, params);
                    }
                }
            }
        }
    }

    /**
     * 检查是否是列表模板行
     */
    private static boolean isListTemplateRow(XWPFTableRow row, Map<String, Object> params) {
        for (XWPFTableCell cell : row.getTableCells()) {
            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                String text = getFullParagraphText(paragraph);
                if (ReUtil.contains(LIST_PLACEHOLDER_PATTERN, text)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 处理列表模板行
     */
    private static void processListTemplateRow(XWPFTable table, int templateRowIndex,
                                               XWPFTableRow templateRow, Map<String, Object> params) {
        // 找到列表数据
        List<Object> listData = null;
        String listKey = null;

        for (XWPFTableCell cell : templateRow.getTableCells()) {
            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                String text = getFullParagraphText(paragraph);
                List<String> placeholders = ReUtil.findAllGroup1(LIST_PLACEHOLDER_PATTERN, text);
                if (!placeholders.isEmpty()) {
                    String placeholder = placeholders.get(0);
                    // 解析 list.key 格式
                    if (placeholder.contains(".")) {
                        String[] parts = placeholder.split("\\.", 2);
                        String key = parts[0];
                        if (params.containsKey(key) && params.get(key) instanceof List) {
                            listData = (List<Object>) params.get(key);
                            listKey = key;
                            break;
                        }
                    }
                }
                if (listData != null) break;
            }
            if (listData != null) break;
        }

        if (listData == null || listKey == null) return;

        // 插入数据行
        for (int i = 0; i < listData.size(); i++) {
            Object dataItem = listData.get(i);
            XWPFTableRow newRow = table.insertNewTableRow(templateRowIndex + i + 1);

            // 复制模板行的结构
            copyRowStructure(templateRow, newRow);

            // 替换占位符
            for (int j = 0; j < newRow.getTableCells().size(); j++) {
                XWPFTableCell templateCell = templateRow.getCell(j);
                XWPFTableCell newCell = newRow.getCell(j);

                // 清空新单元格内容
                List<XWPFParagraph> paragraphs = newCell.getParagraphs();
                for (int p = paragraphs.size() - 1; p >= 0;p--) {
                    newCell.removeParagraph(p);
                }

                // 复制段落
                for (XWPFParagraph templatePara : templateCell.getParagraphs()) {
                    XWPFParagraph newPara = newCell.addParagraph();

                    // 获取模板段落的完整文本
                    String templateText = getFullParagraphText(templatePara);

                    // 替换列表项数据
                    String replacedText = processListPlaceholders(templateText, dataItem);

                    // 添加到新段落
                    addTextWithStyle(newPara, replacedText,
                            templatePara.getRuns().isEmpty() ? null : templatePara.getRuns().get(0));
                }
            }
        }

        // 删除模板行
        table.removeRow(templateRowIndex);
    }

    /**
     * 复制行的结构（列数、单元格样式等）
     */
    private static void copyRowStructure(XWPFTableRow sourceRow, XWPFTableRow targetRow) {
        int sourceCellCount = sourceRow.getTableCells().size();
        int targetCellCount = targetRow.getTableCells().size();

        // 确保目标行有足够单元格
        while (targetCellCount < sourceCellCount) {
            targetRow.addNewTableCell();
            targetCellCount++;
        }

        // 复制每个单元格的属性（宽度、边框、对齐等）
        for (int i = 0; i < sourceCellCount; i++) {
            XWPFTableCell sourceCell = sourceRow.getCell(i);
            XWPFTableCell targetCell = targetRow.getCell(i);

            // 获取源单元格的底层 CTTc 对象
            CTTc sourceCtTc = sourceCell.getCTTc();
            CTTc targetCtTc = targetCell.getCTTc();

            // 如果源单元格有 TcPr（单元格属性），则复制
            if (sourceCtTc.isSetTcPr()) {
                // 创建新的 TcPr 并深拷贝内容
                CTTcPr newTcPr = CTTcPr.Factory.newInstance();

                // 将源 TcPr 的 XML 内容复制到新对象
                newTcPr.set(sourceCtTc.getTcPr().copy());

                // 设置到目标单元格
                targetCtTc.setTcPr(newTcPr);
            }
        }
    }


    /**
     * 处理普通文本占位符 ${key}
     */
    private static String processTextPlaceholders(String text, Map<String, Object> params) {
        String result = text;

        List<String> placeholders = ReUtil.findAllGroup1(TEXT_PLACEHOLDER_PATTERN, text);
        for (String placeholder : placeholders) {
            if (params.containsKey(placeholder)) {
                Object value = params.get(placeholder);
                result = result.replace("${" + placeholder + "}",
                        String.valueOf(value == null ? "" : value));
            }
        }

        return result;
    }

    /**
     * 处理列表占位符 #{list.key}
     */
    private static String processListPlaceholders(String text, Object listItem) {
        String result = text;

        List<String> placeholders = ReUtil.findAllGroup1(LIST_PLACEHOLDER_PATTERN, text);
        for (String placeholder : placeholders) {
            // 解析 list.key 格式
            if (placeholder.contains(".")) {
                String[] parts = placeholder.split("\\.", 2);
                String key = parts[1]; // 获取属性名

                if (listItem instanceof Map) {
                    Map<String, Object> mapItem = (Map<String, Object>) listItem;
                    Object value = mapItem.get(key);
                    result = result.replace("#{" + placeholder + "}",
                            String.valueOf(value == null ? "" : value));
                } else {
                    // 如果是普通对象，可以通过反射获取属性值
                    // 这里简化处理，只处理 Map 类型
                    result = result.replace("#{" + placeholder + "}",
                            String.valueOf(listItem));
                }
            }
        }

        return result;
    }

    /**
     * 处理图片占位符 ![key]
     */
    private static String processImagePlaceholders(String text, Map<String, Object> params) {
        String result = text;

        List<String> placeholders = ReUtil.findAllGroup1(IMG_PLACEHOLDER_PATTERN, text);
        for (String placeholder : placeholders) {
            if (params.containsKey(placeholder)) {
                Object value = params.get(placeholder);
                if (value instanceof BufferedImage || value instanceof InputStream) {
                    result = result.replace("!" + "[" + placeholder + "]", "[图片已插入]");
                } else {
                    result = result.replace("!" + "[" + placeholder + "]",
                            String.valueOf(value == null ? "" : value));
                }
            }
        }

        return result;
    }

    /**
     * 处理条件占位符 @{condition?true_value:false_value}
     */
    private static String processConditionalPlaceholders(String text, Map<String, Object> params) {
        String result = text;

        List<String> placeholders = ReUtil.findAllGroup1(CONDITIONAL_PLACEHOLDER_PATTERN, text);
        for (String placeholder : placeholders) {
            // 解析 condition?true_value:false_value 格式
            int colonIndex = placeholder.indexOf('?');
            if (colonIndex > 0) {
                String condition = placeholder.substring(0, colonIndex);
                String[] values = placeholder.substring(colonIndex + 1).split(":", 2);

                if (values.length == 2) {
                    String trueValue = values[0];
                    String falseValue = values[1];

                    boolean conditionResult = false;
                    if (params.containsKey(condition)) {
                        Object value = params.get(condition);
                        conditionResult = value != null && !"".equals(value) && !"false".equalsIgnoreCase(String.valueOf(value));
                    }

                    String replacement = conditionResult ? trueValue : falseValue;
                    result = result.replace("@{" + placeholder + "}", replacement);
                }
            }
        }

        return result;
    }

    /**
     * 获取段落完整文本
     */
    private static String getFullParagraphText(XWPFParagraph paragraph) {
        StringBuilder sb = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null) {
                sb.append(text);
            }
        }
        return sb.toString();
    }

    /**
     * 清空段落内容
     */
    private static void clearParagraph(XWPFParagraph paragraph) {
        while (!paragraph.getRuns().isEmpty()) {
            paragraph.removeRun(0);
        }
    }

    /**
     * 添加文本并保留样式
     */
    private static void addTextWithStyle(XWPFParagraph paragraph, String text, XWPFRun templateRun) {
        if (StrUtil.isBlank(text)) return;

        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                // 添加换行符
                XWPFRun newlineRun = paragraph.createRun();
                newlineRun.addBreak();
            }

            XWPFRun newRun = paragraph.createRun();
            newRun.setText(lines[i]);

            // 复制样式
            if (templateRun != null) {
                copyRunStyle(templateRun, newRun);
            }
        }
    }

    /**
     * 复制运行样式（安全版）
     */
    private static void copyRunStyle(XWPFRun sourceRun, XWPFRun targetRun) {
        if (sourceRun == null || targetRun == null) {
            return;
        }

        // 字体大小
        Integer fontSize = sourceRun.getFontSize();
        if (fontSize != null && fontSize > 0) {
            targetRun.setFontSize(fontSize);
        }

        // 字体族
        String fontFamily = sourceRun.getFontFamily();
        if (fontFamily != null && !fontFamily.trim().isEmpty()) {
            targetRun.setFontFamily(fontFamily);
        }

        // 加粗、斜体、删除线
        targetRun.setBold(sourceRun.isBold());
        targetRun.setItalic(sourceRun.isItalic());
        targetRun.setStrike(sourceRun.isStrikeThrough());

        // 下划线（关键修复点）
        UnderlinePatterns underline = sourceRun.getUnderline();

        if (underline != null) {
            targetRun.setUnderline(underline);
        } // 否则保持默认（无下划线）

        // 文字颜色
        String color = sourceRun.getColor();
        if (color != null && !color.isEmpty() && !"auto".equalsIgnoreCase(color)) {
            targetRun.setColor(color);
        }

        // 字符间距（单位： twentieths of a point）
        Integer charSpacing = sourceRun.getCharacterSpacing();
        if (charSpacing != null && charSpacing != 0) {
            targetRun.setCharacterSpacing(charSpacing);
        }

        // 文字背景色 文字底纹（shading）
        CTRPr sourceRPr = sourceRun.getCTR().getRPr();
        if (sourceRPr != null && sourceRPr.isSetShd()) {
            // 克隆 shading 对象
            CTShd shd = sourceRPr.getShd();
            CTRPr targetRPr = targetRun.getCTR().isSetRPr() ? targetRun.getCTR().getRPr() : targetRun.getCTR().addNewRPr();
            CTShd ctShd = CTShd.Factory.newInstance();
            ctShd.set(shd.copy());
            targetRPr.setShd(ctShd); // 使用 copy() 避免共享引用
        }
    }

    /**
     * 插入图片到文档
     */
    public static void insertImage(XWPFDocument document, XWPFRun run, InputStream imageStream,
                                   int width, int height) throws InvalidFormatException, IOException {
        run.addPicture(imageStream, Document.PICTURE_TYPE_JPEG, "image",
                Units.toEMU(width), Units.toEMU(height));
    }
}