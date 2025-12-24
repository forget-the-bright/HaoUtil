package org.hao.core.math;

import cn.hutool.core.util.NumberUtil;

/**
 * 正弦波过程变量生成器类
 * 用于生成具有正弦波特性的过程变量数据，通常用于模拟或测试场景
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2025/12/24 11:55
 */
public class ProcessVarSineGenerator {

    private double DEFAULT_PERIOD_SECONDS;

    public ProcessVarSineGenerator(double periodSeconds) {
        this.DEFAULT_PERIOD_SECONDS = periodSeconds;
    }

    public ProcessVarSineGenerator() {
        this.DEFAULT_PERIOD_SECONDS = 30 * 60; // 1800s 30 * 60
    }

    /*
     * 生成正弦曲线值
     * 根据给定的参数计算某一时刻在正弦波上的值。
     * 公式: Yn = ((Amax - Amin) / 2) * sin((2π/T) * (t - n*60)) + ((Amin + Amax) / 2)
     *
     * @param Amax 正弦波的最大幅值
     * @param Amin 正弦波的最小幅值
     * @param n    相位偏移索引，用于控制不同变量之间的时间偏移（每个单位代表60秒）
     * @param t    当前时间戳（单位：秒）
     * @return 计算得到的当前时间点对应的正弦波数值，保留两位小数
     */
    public double computeSineValue(double Amax, double Amin, long n, long t) {
        // 振幅中间值
        double Acenter = (Amin + Amax) / 2;
        // 振幅值
        double An = (Amax - Amin) / 2;
        // 周期时间 s
        double T = DEFAULT_PERIOD_SECONDS;
        // 角频率 ω = 2π / T
        double omega = (2 * Math.PI) / T;

        // 每个变量延迟 n * 60 秒启动（绝对时间偏移） 相位偏移
        long phaseOffsetSeconds = n * 60;

        // 得到当前时间正弦曲线值 Yn = An * sin ( (2π/T) * (t-n*60) ) + Acenter
        double Yn = An * Math.sin(omega * (t - phaseOffsetSeconds)) + Acenter;
        // PrintUtil.RED.Println("omega {} An {} Yn {}", omega, An, Yn);
        // 可选：限制在合理范围（防浮点误差）
        double clampedY = Math.max(Acenter - An, Math.min(Acenter + An, Yn));
        return NumberUtil.round(clampedY, 2).doubleValue();
    }

}
