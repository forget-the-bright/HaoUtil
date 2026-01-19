package org.hao.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 插值计算实体类
 * 用于封装时间点和对应的插值数据
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/6/2 下午2:44
 */
@Data
@ApiModel(value = "InterpolateValue对象", description = "计算插值实体")
public class InterpolateValue {
    @ApiModelProperty(value = "插值时间")
    private final String time;
    @ApiModelProperty(value = "计算插值")
    private final String value;

    /**
     * 构造函数
     *
     * @param time 插值时间，表示进行插值计算的时间点
     * @param value 计算插值，表示在指定时间点的插值结果
     */
    public InterpolateValue(String time, String value) {
        this.time = time;
        this.value = value;
    }
}

