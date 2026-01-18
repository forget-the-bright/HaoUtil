package org.hao.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Excel 模板渲染列表信息内部类
 */
@Data
public class ExcelTemplateListInfo {
    public final String listName;
    public final List<Map<String, Object>> listData;

    public ExcelTemplateListInfo(String listName, List<Map<String, Object>> listData) {
        this.listName = listName;
        this.listData = listData;
    }
}
