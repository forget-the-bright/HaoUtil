package org.hao;

import cn.hutool.core.date.StopWatch;
import lombok.extern.slf4j.Slf4j;
import org.hao.core.StrUtil;
import org.hao.core.print.PrintUtil;
import org.hao.spring.SpringRunUtil;
import org.junit.jupiter.api.Test;

/**
 * TODO
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/7/8 16:40
 */
@Slf4j
public class TestStringFormat {
    @Test
    public void test3() {
        SpringRunUtil.printRunInfo();
        SpringRunUtil.printRunInfo();
    }
    @Test
    public void test2() {
        // 创建一个计时器
        StopWatch stopWatch = new StopWatch();
// 启动第一个任务
        stopWatch.start("任务1");
        System.out.println(StrUtil.formatFast("姓名：{}，价格：{}", "张三", 99.345));
        System.out.println(StrUtil.formatFast("姓名：{}，价格：{},{} ", "张三", 99, 234.345, 123));
        // 输出：姓名：张三，价格：99.35
        System.out.println(StrUtil.formatFast("用户：{}，ID：{}，余额：{}", "Tom", 1001, 88.234));
        // 输出：用户：Tom，ID：1001，余额：88.23
        System.out.println(StrUtil.formatFast("原始内容：{}，另一个数字：{}", "abc", 123.456));
        // 输出：原始内容：abc，另一个数字：123.5
        stopWatch.stop();
        // 启动第二个任务
        stopWatch.start("任务2");
        System.out.println(StrUtil.format("姓名：{}，价格：{}", "张三", 99.345));
        System.out.println(StrUtil.format("姓名：{}，价格：{},{} ", "张三", 99, 234.345, 123));
        // 输出：姓名：张三，价格：99.35
        System.out.println(StrUtil.format("用户：{}，ID：{}，余额：{}", "Tom", 1001, 88.234));
        // 输出：用户：Tom，ID：1001，余额：88.23
        System.out.println(StrUtil.format("原始内容：{}，另一个数字：{}", "abc", 123.456));
        stopWatch.stop();

        // 打印结果
        System.out.println("总耗时：" + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("任务1耗时：" + stopWatch.getTaskInfo()[0].getTimeNanos() + " ns");
        System.out.println("任务2耗时：" + stopWatch.getTaskInfo()[1].getTimeNanos() + " ns");

        // 可视化输出
        System.out.println(stopWatch.prettyPrint());
    }

    @Test
    public void test() {
        // 创建一个计时器
        //预加载系统资源,随便一个
        StrUtil.formatM("姓名：{}，价格：{:.2f}", "张三", 99.345);
        StopWatch stopWatch = new StopWatch();

        // 启动第一个任务
        stopWatch.start("formatM");
        System.out.println(StrUtil.formatM("姓名：{}，价格：{:.2f}", "张三", 99.345));
        System.out.println(StrUtil.formatM("姓名：{}，价格：{:d},{:f} ", "张三", 99, 234.345, 123));
        // 输出：姓名：张三，价格：99.35
        System.out.println(StrUtil.formatM("用户：{}，ID：{}，余额：{:.2f}", "Tom", 1001, 88.234));
        // 输出：用户：Tom，ID：1001，余额：88.23
        System.out.println(StrUtil.formatM("原始内容：{}，另一个数字：{:.1f}", "abc", 123.456));
        // 输出：原始内容：abc，另一个数字：123.5
        stopWatch.stop();


        // 启动第二个任务
        stopWatch.start("format");
        System.out.println(StrUtil.format("姓名：{}，价格：{:.2f}", "张三", 99.345));
        System.out.println(StrUtil.format("姓名：{}，价格：{:d},{:f}  {:s}", "张三", 99, 234.345, 123));
        // 输出：姓名：张三，价格：99.35
        System.out.println(StrUtil.format("用户：{}，ID：{}，余额：{:.2f}", "Tom", 1001, 88.234));
        // 输出：用户：Tom，ID：1001，余额：88.23
        System.out.println(StrUtil.format("原始内容：{}，另一个数字：{:.1f}", "abc", 123.456));
        stopWatch.stop();

        // 启动第一个任务
        stopWatch.start("formatFast");
        System.out.println(StrUtil.formatFast("姓名：{}，{} 价格：{:.2f}", "张三",true, 99.345));
        System.out.println(StrUtil.formatFast("姓名：{}，价格：{:d},{:f} ", "张三", 99, 234.345, 123));
        // 输出：姓名：张三，价格：99.35
        System.out.println(StrUtil.formatFast("用户：{}，ID：{}，余额：{:.2f}", "Tom", 1001, 88.234));
        // 输出：用户：Tom，ID：1001，余额：88.23
        System.out.println(StrUtil.formatFast("原始内容：{}，另一个数字：{:.1f}", "abc", 123.456));
        // 输出：原始内容：abc，另一个数字：123.5
        stopWatch.stop();

        // 打印结果
        System.out.println("总耗时：" + stopWatch.getTotalTimeMillis() + " ms");
        for (StopWatch.TaskInfo taskInfo : stopWatch.getTaskInfo()) {
            PrintUtil.RED.Println("任务{}耗时：{} ns", taskInfo.getTaskName(), taskInfo.getTimeNanos());
        }
        // 可视化输出
        System.out.println(stopWatch.prettyPrint());
    }
}
