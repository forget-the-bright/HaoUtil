package org.hao;

import lombok.extern.slf4j.Slf4j;
import org.hao.core.ip.IPUtils;
import org.hao.core.math.CopperMassCalculator;
import org.hao.core.math.ProcessVarSineGenerator;
import org.hao.core.print.PrintUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/7/7 18:00
 */
@Slf4j
public class TestMathJob {
    @Test
    public void testSineMath() throws Exception {
        for (int i = 0; i < 100; i++) {
            ProcessVarSineGenerator processVarSineGenerator = new ProcessVarSineGenerator(180);

            double sineValue = processVarSineGenerator
                    .computeSineValue(100, 70, 1, System.currentTimeMillis());

            PrintUtil.RED.Println("max 100 min 70 CurrentSinVal: {}", sineValue);

            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Test
    public void testMath() throws Exception {
        String localIP = IPUtils.getLocalIP();
        // 示例输入参数
        double h1 = 0.510; // 初始铜面高度（米）= 510 mm
        double h2 = 0.622; // 结束铜面高度（米）= 622 mm
        double feedRate_t_per_hour = 100; // 投料量（吨/小时）
        double time_hours = 3; // 时间差（小时）


        CopperMassCalculator copperMassCalculator = CopperMassCalculator.builder()
                .setRadius(16.0) //拱底半径（单位：米） 对应图纸尺寸 16000 mm / 1000 = 16.0m
                .setLength(22.7) //炉体有效长度（单位：米） 对应图纸尺寸 22700 mm / 1000 = 22.7m
                .setCopperDensity(8920) //铜密度（单位：kg/m³） 熔融态下约为 8920 kg/m³
                .setYieldPercent(64) //冰铜产率（单位：%） 表示原料转化为冰铜的比例
                .build();

        // 直接计算粗铜排放量
        double discharge =
                copperMassCalculator.calculateDischarge(h1, h2, feedRate_t_per_hour, time_hours);
        double dischargeTon = discharge / 1000;
        double dischargeRat = dischargeTon / time_hours;

        // 输出结果
        System.out.printf("粗铜排放量kg: %.2f kg\n", discharge);
        System.out.printf("粗铜排放量t: %.2f t\n", dischargeTon);
        System.out.printf("粗铜排放速度t/h: %.2f t/h\n", dischargeRat);
    }
}
