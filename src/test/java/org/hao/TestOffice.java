package org.hao;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hao.core.Maps;
import org.hao.core.StrUtil;
import org.hao.core.math.ProcessVarSineGenerator;
import org.hao.core.office.ExcelTemplateUtil;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/15 16:37
 */
@Slf4j
public class TestOffice {
    public Map<String, Object> getDatas() {
        ProcessVarSineGenerator processVarSineGenerator = new ProcessVarSineGenerator(3);
        Map<String, Object> datas = Maps.asMap(
                Maps.put("name", "姓名"),
                Maps.put("age", "年龄"),
                Maps.put("sex", "性别"),
                Maps.put("express", "函数表达式"),
                Maps.put("list", ListUtil.of(
                        Maps.asMap(
                                Maps.put("name", "张三"),
                                Maps.put("age", processVarSineGenerator.computeSineValue(10, 5, 0, System.currentTimeMillis()+ RandomUtil.randomInt())),
                                Maps.put("sex", "男"),
                                Maps.put("express", "=(B2+0)*10")
                        ),
                        Maps.asMap(
                                Maps.put("name", "李四"),
                                Maps.put("age", processVarSineGenerator.computeSineValue(19, 10, 0, System.currentTimeMillis()+ RandomUtil.randomInt())),
                                Maps.put("sex", "女"),
                                Maps.put("express", "=(B3+0)*10")
                        ),
                        Maps.asMap(
                                Maps.put("name", "王五"),
                                Maps.put("age", processVarSineGenerator.computeSineValue(29, 19, 0, System.currentTimeMillis()+ RandomUtil.randomInt())),
                                Maps.put("sex", "男"),
                                Maps.put("express", "=(B4+0)*10")
                        )
                ))
        );
        return datas;
    }

    @Test
    @SneakyThrows
    public void testExcelTemplateMutil() {
        String currentUser = System.getProperty("user.name");
        Map<String, Object> data1s = getDatas();
        Map<String, Object> data2s = getDatas();
        data1s.put("list2",data2s.get("list"));
        data2s.put("list2",data1s.get("list"));
       // data1s.remove("list");
        //data2s.remove("list");
       // data1s.remove("list2");
       // data2s.remove("list2");
        Map<String, Map<String, Object>> mutilDatas = Maps.asMap(LinkedHashMap.class,
                Maps.put("data1", data1s),
                Maps.put("data2", data2s));
        byte[] bytes = ExcelTemplateUtil.renderTemplateMuiltSheetToBytes("template/testTemplate.xlsx", mutilDatas);
        String filePath = StrUtil.format("C:\\Users\\{}\\Desktop\\exportExcelMutil_{}.xlsx",
                currentUser,
                DateUtil.format(new Date(), "yyyyMMddHHmmss")); // 支持相对或绝对路径

        // 一行代码写入文件
        FileUtil.writeBytes(bytes, filePath);

        System.out.println("文件已成功导出到: " + filePath);
    }

    @Test
    @SneakyThrows
    public void testExcelTemplate() {
        String currentUser = System.getProperty("user.name");
        Map<String, Object> datas = getDatas();
        byte[] bytes = ExcelTemplateUtil.renderTemplateToBytes("template/testTemplate.xlsx", datas);
        String filePath = StrUtil.format("C:\\Users\\{}\\Desktop\\exportExcel_{}.xlsx",
                currentUser,
                DateUtil.format(new Date(), "yyyyMMddHHmmss")); // 支持相对或绝对路径

        // 一行代码写入文件
        FileUtil.writeBytes(bytes, filePath);

        System.out.println("文件已成功导出到: " + filePath);
    }
}
