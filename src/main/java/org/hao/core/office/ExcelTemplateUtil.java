package org.hao.core.office;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import org.hao.core.HutoolPlus;
import org.hao.vo.ExcelTemplateListInfo;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel模板导出工具类 - 支持嵌套列表数据导出，修复列表导出覆盖问题
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2026/1/6 13:10
 */
public class ExcelTemplateUtil {
    private static byte[] renderTemplateToBytesBySheetTag(String templatePath, Map<String, Object> data, Object sheetTag) throws IOException {
        // 1. 从 classpath 读取模板（只读）
        InputStream inputByClassPath = HutoolPlus.getInputByClassPath(templatePath);
        ExcelReader reader = ExcelUtil.getReader(inputByClassPath);
        Workbook workbook = reader.getWorkbook();
        Sheet sheet = null;
        if (sheetTag instanceof Number) {
            sheet = workbook.getSheetAt((Integer) sheetTag);
        } else {
            sheet = workbook.getSheet(sheetTag.toString());
        }

        // 先处理所有行，收集需要扩展的列表信息
        processSheet(sheet, data);

        // 3. 写入 ByteArrayOutputStream
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } finally {
            workbook.close();
            reader.close();
        }
    }

    public static byte[] renderTemplateToBytes(String templatePath, Map<String, Object> data, String sheetName) throws IOException {
        return renderTemplateToBytesBySheetTag(templatePath, data, sheetName);
    }

    public static byte[] renderTemplateToBytes(String templatePath, Map<String, Object> data, Integer sheetIndex) throws IOException {
        return renderTemplateToBytesBySheetTag(templatePath, data, sheetIndex);
    }

    /**
     * 从 classpath 读取 Excel 模板，替换变量，返回字节数组（可用于下载）
     *
     * @param templatePath 模板路径，如 "templates/report.xlsx"
     * @param data         变量映射，如 {"name": "张三", "age": "25", "items": [{"item": "商品1", "price": 100}, ...]}
     * @return 替换后的 Excel 字节数组
     */
    public static byte[] renderTemplateToBytes(String templatePath, Map<String, Object> data) throws IOException {
        return renderTemplateToBytesBySheetTag(templatePath, data, 0);
    }

    /**
     * 根据模板和多工作表数据渲染Excel字节数组
     *
     * @param templatePath     模板路径
     * @param data             包含每个工作表名称和对应数据的映射
     * @param templateSheetTag 模板工作表标识（可以是索引或名称）
     * @return 渲染后的Excel字节数组
     * @throws IOException IO异常
     */
    private static byte[] renderTemplateMuiltSheetToBytesBySheetTag(String templatePath, Map<String, Map<String, Object>> data, Object templateSheetTag) throws IOException {
        // 1. 从 classpath 读取模板（只读）
        InputStream inputByClassPath = HutoolPlus.getInputByClassPath(templatePath);
        ExcelReader reader = ExcelUtil.getReader(inputByClassPath);
        Workbook workbook = reader.getWorkbook();
        Sheet sheet = null;
        if (templateSheetTag instanceof Number) {
            sheet = workbook.getSheetAt((Integer) templateSheetTag);
        } else {
            sheet = workbook.getSheet(templateSheetTag.toString());
        }

        // 遍历数据，为每个数据项创建一个新的工作表
        int index = 0;
        for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
            String sheetName = entry.getKey();
            Map<String, Object> value = entry.getValue();

            // 创建新工作表并复制模板内容
            Sheet newSheet = workbook.createSheet(sheetName);
            copySheet(workbook, sheet, newSheet);

            // 处理新工作表中的数据
            processSheet(newSheet, value);

            // 设置工作表顺序
            workbook.setSheetOrder(sheetName, index);
            index++;
        }

        // 设置第一个工作表为活动工作表
        workbook.setActiveSheet(0);

        // 移除原始模板工作表
        int sheetIndex = workbook.getSheetIndex(sheet);
        workbook.removeSheetAt(sheetIndex);

        // 3. 写入 ByteArrayOutputStream
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } finally {
            workbook.close();
            reader.close();
        }
    }

    public static byte[] renderTemplateMuiltSheetToBytes(String templatePath, Map<String, Map<String, Object>> data, String sheetName) throws IOException {
        return renderTemplateMuiltSheetToBytesBySheetTag(templatePath, data, sheetName);
    }

    public static byte[] renderTemplateMuiltSheetToBytes(String templatePath, Map<String, Map<String, Object>> data, Integer sheetIndex) throws IOException {
        return renderTemplateMuiltSheetToBytesBySheetTag(templatePath, data, sheetIndex);
    }

    public static byte[] renderTemplateMuiltSheetToBytes(String templatePath, Map<String, Map<String, Object>> data) throws IOException {
        return renderTemplateMuiltSheetToBytesBySheetTag(templatePath, data, 0);
    }

    /**
     * 处理整个工作表
     */
    private static void processSheet(Sheet sheet, Map<String, Object> data) {
        // 首先遍历所有行，找出包含列表标记的行
        List<Integer> rowsWithListMarkers = new ArrayList<>();
        for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // 检查这一行是否有列表标记
            boolean hasListMarker = false;
            for (Cell cell : row) {
                if (cell == null || cell.getCellType() != CellType.STRING) continue;

                String cellValue = cell.getStringCellValue();
                if (containsListPlaceholder(cellValue)) {
                    hasListMarker = true;
                    break;
                }
            }

            if (hasListMarker) {
                rowsWithListMarkers.add(i);
            }
        }

        // 从后往前处理，避免行索引变化的影响
        for (int i = rowsWithListMarkers.size() - 1; i >= 0; i--) {
            int rowIndex = rowsWithListMarkers.get(i);
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                processListInRow(sheet, row, data, rowIndex);
            }
        }

        // 处理普通占位符
        for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            for (Cell cell : row) {
                if (cell == null || cell.getCellType() != CellType.STRING) continue;

                String cellValue = cell.getStringCellValue();
                if (cellValue == null || (!cellValue.contains("${") && !cellValue.contains("#{"))) continue;

                // 替换所有 ${key} 为 data 中的值
                String replaced = replacePlaceholders(cellValue, data);
                cell.setCellValue(replaced);
            }
        }
    }

    /**
     * 处理列表数据并插入新行，仅对包含列表表达式的列进行扩展
     *
     * @param sheet            工作表
     * @param templateRow      模板行
     * @param data             数据映射
     * @param originalRowIndex 原始行索引
     */
    private static void processListInRow(Sheet sheet, Row templateRow, Map<String, Object> data, int originalRowIndex) {
        // 收集所有列表占位符及其对应的列表数据
        List<Integer> listColumns = new ArrayList<>(); // 记录包含列表标记的列索引
        List<ExcelTemplateListInfo> listInfos = collectListInfos(templateRow, data);

        if (listInfos.isEmpty()) {
            return;
        }

        // 获取最长的列表长度，用于确定需要创建多少行
        int maxListSize = 0;
        for (ExcelTemplateListInfo listInfo : listInfos) {
            maxListSize = Math.max(maxListSize, listInfo.listData.size());
        }

        if (maxListSize == 0) {
            // 如果所有列表都为空，删除模板行
            sheet.removeRow(templateRow);
            return;
        }

        // 找出包含列表标记的列
        for (int i = 0; i < templateRow.getLastCellNum(); i++) {
            Cell cell = templateRow.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String cellValue = cell.getStringCellValue();
                if (containsListPlaceholder(cellValue)) {
                    listColumns.add(i);
                }
            }
        }

        // 保存原始列的模板（仅保存包含列表标记的列）
        Map<Integer, String> cellTemplates = new HashMap<>();
        for (Integer colIndex : listColumns) {
            Cell cell = templateRow.getCell(colIndex);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                cellTemplates.put(colIndex, cell.getStringCellValue());
            }
        }

        // 计算需要插入的行数
        int rowsToAdd = maxListSize - 1; // 原行已经占用了一行，所以只需要新增 maxListSize - 1 行

        if (rowsToAdd > 0) {
            // 只对包含列表标记的列及以下的行进行向下移动
            shiftSpecificColumnsDown(sheet, originalRowIndex, listColumns, rowsToAdd);
        }

        // 在原始行之后创建新行并填充列表数据（仅填充包含列表标记的列）
        for (int i = 1; i < maxListSize; i++) { // 从1开始，因为第0行是原始模板行
            int newRowIdx = originalRowIndex + i;
            Row newRow = sheet.getRow(newRowIdx);
            if (newRow == null) {
                newRow = sheet.createRow(newRowIdx);
            }

            for (Integer colIndex : listColumns) {
                String cellTemplate = cellTemplates.get(colIndex);
                if (cellTemplate != null) {
                    Cell newCell = newRow.getCell(colIndex);
                    if (newCell == null) {
                        newCell = newRow.createCell(colIndex);
                    }

                    // 从模板行复制样式
                    Cell templateCell = templateRow.getCell(colIndex);
                    if (templateCell != null && templateCell.getCellStyle() != null) {
                        newCell.setCellStyle(templateCell.getCellStyle());
                    }

                    // 替换列表项的占位符
                    String cellContent = replaceListPlaceholders(cellTemplate, listInfos, i);
                    newCell.setCellValue(cellContent);
                }
            }
        }

        // 对于原始模板行，更新包含列表标记的列的值
        for (Integer colIndex : listColumns) {
            String cellTemplate = cellTemplates.get(colIndex);
            if (cellTemplate != null) {
                Cell cell = templateRow.getCell(colIndex);
                if (cell == null) {
                    cell = templateRow.createCell(colIndex);
                }

                // 从模板行复制样式
                Cell templateCell = templateRow.getCell(colIndex);
                if (templateCell != null && templateCell.getCellStyle() != null) {
                    cell.setCellStyle(templateCell.getCellStyle());
                }

                // 替换列表项的占位符（使用第一个数据项）
                String cellContent = replaceListPlaceholders(cellTemplate, listInfos, 0);
                cell.setCellValue(cellContent);
            }
        }
    }

    /**
     * 只移动特定列下方的连续单元格，避免移动无关行导致内容重复
     * 仅移动从起始行下方开始的包含指定列的连续行
     *
     * @param sheet          工作表
     * @param startRow       起始行索引（列表表达式所在行）
     * @param columnsToShift 需要移动的列索引列表
     * @param rowsToAdd      需要增加的行数
     */
    private static void shiftSpecificColumnsDown(Sheet sheet, int startRow, List<Integer> columnsToShift, int rowsToAdd) {
        int lastRowNum = sheet.getLastRowNum();

        // 计算需要移动的连续行数：从startRow+1开始，直到遇到一个完全不包含指定列的行或到达最后一行
        int rowsToMove = 0;
        for (int i = startRow + 1; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                break; // 遇到空行则停止
            }

            boolean hasRelevantColumn = false;
            for (Integer colIndex : columnsToShift) {
                if (row.getCell(colIndex) != null) {
                    hasRelevantColumn = true;
                    break;
                }
            }

            if (!hasRelevantColumn) {
                // 如果当前行没有要移动的列，则停止计算
                break;
            }

            rowsToMove++;
        }

        // 从最下面需要移动的行开始向上移动，避免覆盖
        for (int i = startRow + rowsToMove; i >= startRow + 1; i--) {
            Row sourceRow = sheet.getRow(i);
            if (sourceRow != null) {
                // 创建目标行
                Row targetRow = sheet.getRow(i + rowsToAdd);
                if (targetRow == null) {
                    targetRow = sheet.createRow(i + rowsToAdd);
                }

                // 只移动指定的列
                for (Integer colIndex : columnsToShift) {
                    Cell sourceCell = sourceRow.getCell(colIndex);
                    if (sourceCell != null) {
                        Cell targetCell = targetRow.createCell(colIndex);

                        // 复制单元格值
                        switch (sourceCell.getCellType()) {
                            case STRING:
                                targetCell.setCellValue(sourceCell.getStringCellValue());
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(sourceCell)) {
                                    targetCell.setCellValue(sourceCell.getDateCellValue());
                                } else {
                                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                                }
                                break;
                            case BOOLEAN:
                                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                targetCell.setCellFormula(sourceCell.getCellFormula());
                                break;
                            case BLANK:
                                // 留空
                                break;
                            case ERROR:
                                targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                                break;
                            default:
                                break;
                        }

                        // 复制单元格样式
                        if (sourceCell.getCellStyle() != null) {
                            targetCell.setCellStyle(sourceCell.getCellStyle());
                        }

                        // 复制单元格注释（如果存在）
                        if (sourceCell.getCellComment() != null) {
                            targetCell.setCellComment(sourceCell.getCellComment());
                        }

                        // 复制单元格超链接（如果存在）
                        if (sourceCell.getHyperlink() != null) {
                            targetCell.setHyperlink(sourceCell.getHyperlink());
                        }
                    }
                }
            }
        }
    }

    /**
     * 收集行中的列表信息
     */
    private static List<ExcelTemplateListInfo> collectListInfos(Row row, Map<String, Object> data) {
        List<ExcelTemplateListInfo> listInfos = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();

        for (Cell cell : row) {
            if (cell == null || cell.getCellType() != CellType.STRING) continue;

            String cellValue = cell.getStringCellValue();
            if (containsListPlaceholder(cellValue)) {
                Pattern pattern = Pattern.compile("#\\{([^}]+)\\.([^}]+)}");
                Matcher matcher = pattern.matcher(cellValue);

                while (matcher.find()) {
                    String fullMatch = matcher.group(0); // 如 #{users.name}
                    String listName = matcher.group(1);  // 如 users
                    String property = matcher.group(2);  // 如 name

                    if (!processedKeys.contains(listName)) {
                        Object listObj = data.get(listName);

                        if (listObj instanceof List) {
                            List<Map<String, Object>> listData = convertToListOfMaps((List<?>) listObj);
                            listInfos.add(new ExcelTemplateListInfo(listName, listData));
                            processedKeys.add(listName);
                        } else {
                            // 如果不是列表，创建一个包含null元素的列表以保持一致性
                            List<Map<String, Object>> emptyList = Collections.singletonList(null);
                            listInfos.add(new ExcelTemplateListInfo(listName, emptyList));
                            processedKeys.add(listName);
                        }
                    }
                }
            }
        }

        return listInfos;
    }

    /**
     * 将任意对象列表转换为Map列表
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> convertToListOfMaps(List<?> list) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object obj : list) {
            if (obj instanceof Map) {
                result.add((Map<String, Object>) obj);
            } else {
                // 尝试通过反射获取对象属性
                Map<String, Object> map = BeanUtil.beanToMap(obj);
                //Map<String, Object> map = objectToMap(obj);
                result.add(map);
            }
        }

        return result;
    }

    /**
     * 检查字符串是否包含列表占位符
     */
    private static boolean containsListPlaceholder(String text) {
        return text != null && text.contains("#{");
    }

    /**
     * 替换列表项中的占位符
     */
    private static String replaceListPlaceholders(String template, List<ExcelTemplateListInfo> listInfos, int index) {
        String result = template;

        for (ExcelTemplateListInfo listInfo : listInfos) {
            Pattern pattern = Pattern.compile("#\\{" + Pattern.quote(listInfo.listName) + "\\.([^}]+)}");
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String property = matcher.group(1);

                // 获取当前索引位置的对象
                Map<String, Object> obj = index < listInfo.listData.size() ? listInfo.listData.get(index) : null;

                String replacement = "";
                if (obj != null) {
                    // 从Map中获取属性值
                    Map<String, Object> map = obj;
                    Object value = map.get(property);
                    replacement = (value != null) ? value.toString() : "";
                }

                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        // 最后处理普通占位符
        return result;
    }


    /**
     * 替换单元格中的 ${key} 占位符和 #{listName.property} 列表标记
     */
    private static String replacePlaceholders(String template, Map<String, Object> data) {
        String result = template;

        // 替换普通变量
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
        Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = data.get(key);
            String replacement = (value != null) ? value.toString() : "";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        result = sb.toString();

        return result;
    }


    /**
     * 将 sourceSheet 的内容（包括行、单元格、基本样式）复制到 targetSheet
     */
    public static void copySheet(Workbook workbook, Sheet sourceSheet, Sheet targetSheet) {
        // 复制所有行
        for (Row sourceRow : sourceSheet) {
            Row targetRow = targetSheet.createRow(sourceRow.getRowNum());

            // 复制该行所有单元格
            Iterator<Cell> cellIterator = sourceRow.cellIterator();
            while (cellIterator.hasNext()) {
                Cell sourceCell = cellIterator.next();
                Cell targetCell = targetRow.createCell(sourceCell.getColumnIndex());

                // 复制单元格值
                copyCellValue(sourceCell, targetCell);

                // 复制单元格样式（可选，但推荐）
                if (sourceCell.getCellStyle() != null) {
                    // 注意：不能直接赋值 style，需 clone 或复用
                    // 这里简化处理：创建新样式并复制关键属性
                    CellStyle newStyle = workbook.createCellStyle();
                    newStyle.cloneStyleFrom(sourceCell.getCellStyle());
                    targetCell.setCellStyle(newStyle);
                }
            }

            // 可选：复制行高
            targetRow.setHeight(sourceRow.getHeight());
        }

        // 可选：复制列宽
        for (int i = 0; i <= sourceSheet.getLastRowNum(); i++) {
            int colWidth = sourceSheet.getColumnWidth(i);
            targetSheet.setColumnWidth(i, colWidth);
        }
    }

    private static void copyCellValue(Cell sourceCell, Cell targetCell) {
        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    targetCell.setCellValue(sourceCell.getDateCellValue());
                } else {
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                break;
            case BLANK:
                // 留空
                break;
            case ERROR:
                targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                break;
            default:
                break;
        }
    }
}
