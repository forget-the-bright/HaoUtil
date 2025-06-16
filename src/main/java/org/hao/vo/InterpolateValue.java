package org.hao.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * TODO
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

    public InterpolateValue(String time, String value) {
        this.time = time;
        this.value = value;
    }
}
