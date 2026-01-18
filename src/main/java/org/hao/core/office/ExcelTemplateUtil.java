package org.hao.core.office;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;

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
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/6 13:10
 */
public class ExcelTemplateUtil {
    private static byte[] renderTemplateToBytesBySheetTag(String templatePath, Map<String, Object> data, Object sheetTag) throws IOException {
        // 1. 从 classpath 读取模板（只读）
        InputStream inputByClassPath = getInputByClassPath(templatePath);
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
     * @param templatePath    模板路径
     * @param data           包含每个工作表名称和对应数据的映射
     * @param templateSheetTag 模板工作表标识（可以是索引或名称）
     * @return 渲染后的Excel字节数组
     * @throws IOException IO异常
     */
    private static byte[] renderTemplateMuiltSheetToBytesBySheetTag(String templatePath, Map<String, Map<String, Object>> data, Object templateSheetTag) throws IOException {
        // 1. 从 classpath 读取模板（只读）
        InputStream inputByClassPath = getInputByClassPath(templatePath);
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
        List<ListInfo> listInfos = collectListInfos(templateRow, data);

        if (listInfos.isEmpty()) {
            // 如果没有列表标记，只做普通替换
            for (Cell cell : templateRow) {
                if (cell == null || cell.getCellType() != CellType.STRING) continue;

                String cellValue = cell.getStringCellValue();
                cell.setCellValue(replacePlaceholders(cellValue, data));
            }
            return;
        }

        // 获取最长的列表长度，用于确定需要创建多少行
        int maxListSize = 0;
        for (ListInfo listInfo : listInfos) {
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
     * @param sheet         工作表
     * @param startRow      起始行索引（列表表达式所在行）
     * @param columnsToShift 需要移动的列索引列表
     * @param rowsToAdd     需要增加的行数
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
     * 向下移动指定范围内的行
     */
    private static void shiftRowsDown(Sheet sheet, int startRow, int rowsToAdd) {
        int lastRowNum = sheet.getLastRowNum();

        // 从最后一行开始向上移动，避免覆盖
        for (int i = lastRowNum; i >= startRow; i--) {
            Row sourceRow = sheet.getRow(i);
            if (sourceRow != null) {
                Row newRow = sheet.createRow(i + rowsToAdd);

                // 复制单元格内容
                for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
                    Cell sourceCell = sourceRow.getCell(j);
                    if (sourceCell != null) {
                        Cell newCell = newRow.createCell(j);

                        // 复制单元格值
                        switch (sourceCell.getCellType()) {
                            case STRING:
                                newCell.setCellValue(sourceCell.getStringCellValue());
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(sourceCell)) {
                                    newCell.setCellValue(sourceCell.getDateCellValue());
                                } else {
                                    newCell.setCellValue(sourceCell.getNumericCellValue());
                                }
                                break;
                            case BOOLEAN:
                                newCell.setCellValue(sourceCell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                newCell.setCellFormula(sourceCell.getCellFormula());
                                break;
                            case BLANK:
                                // 留空
                                break;
                            case ERROR:
                                newCell.setCellErrorValue(sourceCell.getErrorCellValue());
                                break;
                            default:
                                break;
                        }

                        // 复制单元格样式
                        if (sourceCell.getCellStyle() != null) {
                            newCell.setCellStyle(sourceCell.getCellStyle());
                        }

                        // 复制单元格注释（如果存在）
                        if (sourceCell.getCellComment() != null) {
                            newCell.setCellComment(sourceCell.getCellComment());
                        }

                        // 复制单元格超链接（如果存在）
                        if (sourceCell.getHyperlink() != null) {
                            newCell.setHyperlink(sourceCell.getHyperlink());
                        }
                    }
                }

                // 复制行高
                newRow.setHeight(sourceRow.getHeight());

                // 删除原来的行
                sheet.removeRow(sourceRow);
            }
        }
    }

    /**
     * 收集行中的列表信息
     */
    private static List<ListInfo> collectListInfos(Row row, Map<String, Object> data) {
        List<ListInfo> listInfos = new ArrayList<>();
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
                            listInfos.add(new ListInfo(listName, listData));
                            processedKeys.add(listName);
                        } else {
                            // 如果不是列表，创建一个包含null元素的列表以保持一致性
                            List<Map<String, Object>> emptyList = Collections.singletonList(null);
                            listInfos.add(new ListInfo(listName, emptyList));
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
                Map<String, Object> map = objectToMap(obj);
                result.add(map);
            }
        }

        return result;
    }

    /**
     * 将对象转换为Map
     */
    private static Map<String, Object> objectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();

        if (obj == null) {
            return map;
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                map.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                // 忽略无法访问的字段
            }
        }

        return map;
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
    private static String replaceListPlaceholders(String template, List<ListInfo> listInfos, int index) {
        String result = template;

        for (ListInfo listInfo : listInfos) {
            Pattern pattern = Pattern.compile("#\\{" + Pattern.quote(listInfo.listName) + "\\.([^}]+)}");
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String property = matcher.group(1);

                // 获取当前索引位置的对象
                Object obj = index < listInfo.listData.size() ? listInfo.listData.get(index) : null;

                String replacement = "";
                if (obj != null) {
                    // 从Map中获取属性值
                    if (obj instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) obj;
                        Object value = map.get(property);
                        replacement = (value != null) ? value.toString() : "";
                    } else {
                        // 尝试通过反射获取属性值
                        Object value = getPropertyValue(obj, property);
                        replacement = (value != null) ? value.toString() : "";
                    }
                }

                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        // 最后处理普通占位符
        return replaceSimplePlaceholders(result, new HashMap<>()); // 传入空的数据map，因为此时不应有普通占位符
    }

    /**
     * 通过反射获取对象属性值
     */
    private static Object getPropertyValue(Object obj, String propertyName) {
        if (obj == null) return null;

        Class<?> clazz = obj.getClass();
        try {
            Field field = clazz.getDeclaredField(propertyName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 尝试获取getter方法
            try {
                String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
                java.lang.reflect.Method method = clazz.getMethod(getterName);
                return method.invoke(obj);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * 替换简单的占位符（不涉及列表）
     */
    private static String replaceSimplePlaceholders(String template, Map<String, Object> data) {
        // 不做任何替换，因为列表数据处理完后不应该再有普通占位符
        // 如果确实有，可以保留原逻辑
        return template;
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

        // 替换列表标记（如果没有实际列表数据，则清空）
        Pattern listPattern = Pattern.compile("#\\{([^}]+)\\.([^}]+)}");
        Matcher listMatcher = listPattern.matcher(result);
        sb = new StringBuffer();

        while (listMatcher.find()) {
            String listKey = listMatcher.group(1).trim();
            String property = listMatcher.group(2).trim();

            Object listObj = data.get(listKey);
            String replacement = "";

            if (listObj instanceof List) {
                List<?> list = (List<?>) listObj;
                if (!list.isEmpty()) {
                    Object firstItem = list.get(0);
                    if (firstItem instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) firstItem;
                        Object value = map.get(property);
                        replacement = (value != null) ? value.toString() : "";
                    } else {
                        Object value = getPropertyValue(firstItem, property);
                        replacement = (value != null) ? value.toString() : "";
                    }
                }
            }

            listMatcher.appendReplacement(sb, replacement);
        }
        listMatcher.appendTail(sb);

        return sb.toString();
    }

    public static InputStream getInputByClassPath(String path) {
        // 使用当前线程的上下文类加载器获取资源文件的输入流
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
    }

    @SneakyThrows
    public static void download(HttpServletResponse response, byte[] excelBytes, String fileName) {
        // 设置Content-Disposition响应头，用于指定文件名，文件名使用UTF-8编码以支持中文
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        // 设置响应的内容类型，指定为Excel格式，同时设置字符集为utf-8
        response.setContentType(getContentTypeForExtension(fileName));
        response.setContentLength(excelBytes.length);
        // 获取响应的输出流，用于向客户端发送Excel数据
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(excelBytes);
        outputStream.flush(); // 确保数据全部写出
        IoUtil.close(outputStream);
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
     * 列表信息内部类
     */
    private static class ListInfo {
        final String listName;
        final List<Map<String, Object>> listData;

        ListInfo(String listName, List<Map<String, Object>> listData) {
            this.listName = listName;
            this.listData = listData;
        }
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
