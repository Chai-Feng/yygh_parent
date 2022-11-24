package com.alex.yygh.vo.hosp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 条件查询 把需要的条件，封装成一个类
 */
@Data
public class HospitalSetQueryVo {

    @ApiModelProperty(value = "医院名称")
    private String hosname;

    @ApiModelProperty(value = "医院编号")
    private String hoscode;
}
