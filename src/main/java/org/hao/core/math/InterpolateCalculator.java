package org.hao.core.math;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import org.hao.core.exception.HaoException;
import org.hao.vo.InterpolateValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 插值计算器
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/6/2 下午1:46
 */
public class InterpolateCalculator {


    // 主方法：线性插值 + 校验
    public static List<InterpolateValue> interpolate(
            Date startTime,
            Date endTime,
            Double sValue,
            Double eValue,
            Integer step,
            TimeUnit unit) throws IllegalArgumentException {

        List<InterpolateValue> result = new ArrayList<>();
        HaoException.throwByFlag(!(
                        unit.equals(TimeUnit.SECONDS) ||
                                unit.equals(TimeUnit.MINUTES) ||
                                unit.equals(TimeUnit.HOURS) ||
                                unit.equals(TimeUnit.DAYS)),
                "时间间隔范围目前只能在 秒,分,时,天 四个范围内选择");

        // 1. 参数非空校验
        HaoException.throwByFlag(ObjectUtil.isEmpty(startTime), "起始时间不能为空");
        HaoException.throwByFlag(ObjectUtil.isEmpty(endTime), "结束时间不能为空");
        HaoException.throwByFlag(ObjectUtil.isEmpty(sValue), "起始值不能为空");
        HaoException.throwByFlag(ObjectUtil.isEmpty(eValue), "结束值不能为空");
        HaoException.throwByFlag(ObjectUtil.isEmpty(step), "步长不能为空");
        HaoException.throwByFlag(ObjectUtil.isEmpty(unit), "时间单位不能为空");

        // 2. 时间格式校验与转换
        if (startTime.after(endTime)) {
            throw new IllegalArgumentException("起始时间不能晚于结束时间");
        }

        // 3. 数值校验
        BigDecimal startValue = NumberUtil.toBigDecimal(sValue);
        BigDecimal endValue = NumberUtil.toBigDecimal(eValue);

        // 4. 计算总秒数
        long totalSeconds = betweenSecond(startTime, endTime);
        if (totalSeconds <= 0) {
            throw new IllegalArgumentException("时间差必须大于0秒");
        }
        // 6. 计算斜率
        BigDecimal subtractValue = endValue.subtract(startValue);// 结束-开始值 求出差值
        BigDecimal slope = subtractValue.divide(BigDecimal.valueOf(totalSeconds), 10, BigDecimal.ROUND_HALF_UP); //差值除以总秒数,得出每秒增量=斜率

        // 6. 步长转为秒
        long stepInSeconds;
        switch (unit) {
            case SECONDS:
                stepInSeconds = step;
                break;
            case MINUTES:
                stepInSeconds = step * 60L;
                break;
            case HOURS:
                stepInSeconds = step * 3600L;
                break;
            case DAYS:
                stepInSeconds = step * 86400L;
                break;
            default:
                stepInSeconds = step;
                break;
        }
        // 6.1 步长不能超过总秒数,超过以总秒数当作步长
        HaoException.throwByFlag(stepInSeconds > totalSeconds, "步长不能超过总秒数,目前超过: [ {} ] 秒", stepInSeconds - totalSeconds);

        // 7. 遍历插值
        Date current = startTime;
        while (!current.after(endTime)) {
            long elapsedSeconds = betweenSecond(startTime, current);
            BigDecimal value = startValue.add(lapse(elapsedSeconds, slope));
            result.add(new InterpolateValue(DateUtil.format(current, "yyyy-MM-dd HH:mm:ss"), NumberUtil.round(value, 2).toPlainString()));
            // 增加步长
            switch (unit) {
                case SECONDS:
                    current = DateUtil.offsetSecond(current, step);
                    break;
                case MINUTES:
                    current = DateUtil.offsetMinute(current, step);
                    break;
                case HOURS:
                    current = DateUtil.offsetHour(current, step);
                    break;
                case DAYS:
                    current = DateUtil.offsetDay(current, step);
                default:
                    current = DateUtil.offsetSecond(current, step);
                    break;
            }
        }
        InterpolateValue interpolateValue = result.get(result.size() - 1);
        DateTime endInterpolateValueTime = DateUtil.parse(interpolateValue.getTime());
        if (endInterpolateValueTime.before(endTime)) {
            long elapsedSeconds = betweenSecond(startTime, endTime);
            BigDecimal value = startValue.add(lapse(elapsedSeconds, slope));
            result.add(new InterpolateValue(DateUtil.format(endTime, "yyyy-MM-dd HH:mm:ss"), NumberUtil.round(value, 2).toPlainString()));
        }
        return result;
    }

    private static BigDecimal lapse(long seconds, BigDecimal slope) {
        return slope.multiply(BigDecimal.valueOf(seconds));
    }

    /**
     * 计算两个时间之间的秒数差
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 秒数差（绝对值）
     */
    public static long betweenSecond(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }

        // 确保开始时间不晚于结束时间
        if (start.after(end)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        long diffMillis = end.getTime() - start.getTime();
        return diffMillis / 1000; // 毫秒转秒
    }
}
