package org.hao.core.math;

import lombok.extern.slf4j.Slf4j;

/**
 * 熔炼炉中铜质量计算工具类。 提供基于拱底截面积模型计算熔池内铜质量的方法， 并支持粗铜排放量相关计算。
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/7/7 下午2:44
 */
@Slf4j
public class CopperMassCalculator {

    // -----------------------------
    // 常量定义（系统固定参数）
    // -----------------------------

    /**
     * 拱底半径（单位：米） 对应图纸尺寸 16000 mm
     */
    private double RADIUS;

    /**
     * 炉体有效长度（单位：米） 对应图纸尺寸 22700 mm
     */
    private double LENGTH;

    /**
     * 铜密度（单位：kg/m³） 熔融态下约为 8920 kg/m³
     */
    private double COPPER_DENSITY;

    /**
     * 冰铜产率（单位：%） 表示原料转化为冰铜的比例
     */
    private double YIELD_PERCENT;


    private CopperMassCalculator(Builder builder) {
        this.RADIUS = builder.radius;
        this.LENGTH = builder.length;
        this.COPPER_DENSITY = builder.copperDensity;
        this.YIELD_PERCENT = builder.yieldPercent;
    }


    // -----------------------------
    // 核心方法定义
    // -----------------------------

    /**
     * 根据给定高度 h 计算拱底截面积 S(h)。
     *
     * <p>公式： S = r² * arccos((r - h)/r) - (r - h) * √(2rh - h²)
     *
     * @param r 拱底半径（单位：米）
     * @param h 铜液面高度（单位：米，0 ≤ h ≤ r）
     * @return 截面积 S（单位：平方米 m²）
     * @throws IllegalArgumentException 如果 h 超出范围
     */
    public static double getCrossSectionArea(double r, double h) {
        if (h > r || h < 0) {
            throw new IllegalArgumentException("高度 h 必须在 [0, r] 范围内");
        }

        double sector = r * r * Math.acos((r - h) / r);
        double triangle = (r - h) * Math.sqrt(2 * r * h - h * h);
        return sector - triangle;
    }

    /**
     * 根据铜液面高度计算熔池中的铜质量 m。 使用系统常量 RADIUS、LENGTH、COPPER_DENSITY。
     *
     * <p>公式： m = ρ * S * L
     *
     * @param h 铜液面高度（单位：米，0 ≤ h ≤ RADIUS）
     * @return 铜质量 m（单位：千克 kg）
     */
    public double getCopperMass(double h) {
        double area = getCrossSectionArea(RADIUS, h);
        return COPPER_DENSITY * area * LENGTH;
    }

    /**
     * 计算排放期间投入的铜量 m3。 使用系统常量 YIELD_PERCENT。
     *
     * <p>公式： m3 = 投料量 × 时间 × 冰铜产率
     *
     * @param feedRate_kg_per_hour 投料量（单位：kg/h）
     * @param time_hours           排放时间（单位：小时）
     * @return m3（单位：kg）
     */
    public double calculateM3(double feedRate_kg_per_hour, double time_hours) {
        return feedRate_kg_per_hour * time_hours * (YIELD_PERCENT / 100.0);
    }

    /**
     * 直接根据输入参数计算粗铜排放量。
     *
     * <p>公式： 排放量 = m1 - m2 + m3
     *
     * @param h1                  初始铜面高度（单位：米）
     * @param h2                  结束铜面高度（单位：米）
     * @param feedRate_t_per_hour 投料量（单位：吨/小时）
     * @param time_hours          排放持续时间（单位：小时）
     * @return 粗铜排放量（单位：kg）
     */
    public double calculateDischarge(
            double h1, double h2, double feedRate_t_per_hour, double time_hours) {
        double m1 = getCopperMass(h1);
        double m2 = getCopperMass(h2);
        double m3 = calculateM3(feedRate_t_per_hour * 1000, time_hours);
        return m1 - m2 + m3;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 CopperMassCalculator 对象
     */
    public static class Builder {
        /**
         * 拱底半径（单位：米） 对应图纸尺寸 16000 mm / 1000 = 16.0m
         */
        private double radius = 16.0;
        /**
         * 炉体有效长度（单位：米） 对应图纸尺寸 22700 mm / 1000 = 22.7m
         */
        private double length = 22.7;
        /**
         * 铜密度（单位：kg/m³） 熔融态下约为 8920 kg/m³
         */
        private double copperDensity = 8920;
        /**
         * 冰铜产率（单位：%） 表示原料转化为冰铜的比例
         */
        private double yieldPercent = 64;

        public Builder setRadius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder setLength(double length) {
            this.length = length;
            return this;
        }

        public Builder setCopperDensity(double copperDensity) {
            this.copperDensity = copperDensity;
            return this;
        }

        public Builder setYieldPercent(double yieldPercent) {
            this.yieldPercent = yieldPercent;
            return this;
        }

        public CopperMassCalculator build() {
            return new CopperMassCalculator(this);
        }
    }
}
