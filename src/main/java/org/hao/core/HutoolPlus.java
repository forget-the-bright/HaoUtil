package org.hao.core;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ReUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.SneakyThrows;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URLEncoder;

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
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
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
/*    public static ExcelWriter merge(ExcelWriter writer, int firstColumn, int lastColumn, Object content) {
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
    }*/

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
/*    public static ExcelWriter merge(ExcelWriter writer, int firstRow, int lastRow, int CurrentColumn, Object content) {
        // 执行合并单元格并填充内容的操作，最后一个参数设为true以启用自动样式调整
        writer.merge(firstRow, lastRow, CurrentColumn, CurrentColumn, content, true);
        // 返回执行操作后的ExcelWriter对象
        return writer;
    }*/

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
     * @param v 待舍入的double类型数值
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
     * @param v 待处理的双精度浮点数
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
     * @param v   待处理的数值字符串
     * @param scale 小数位数或有效数字位数
     * @return    四舍五入后的数值字符串
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
