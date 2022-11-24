package com.alex.yygh.cmn.listener.testExecl;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-12-19:22
 */
@Data
public class UserData {

    @ExcelProperty(value = "id",index = 0)
    private Integer id;

    @ExcelProperty(value = "姓名",index = 1)
    private String name;
}
