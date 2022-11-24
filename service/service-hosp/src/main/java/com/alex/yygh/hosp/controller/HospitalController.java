package com.alex.yygh.hosp.controller;

import com.alex.yygh.common.result.Result;
import com.alex.yygh.hosp.service.HospitalService;
import com.alex.yygh.model.hosp.Hospital;
import com.alex.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-16-10:55
 */
@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/admin/hosp/hospiatl")
//CrossOrigin
public class HospitalController {


    @Autowired
    private HospitalService hospitalService;

    //条件查询带分页
    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(@ApiParam(name = "page", value = "当前页码", required = true)
                            @PathVariable("page")Integer page,
                        @ApiParam(name = "limit", value = "每页记录数", required = true)
                        @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo
                        ){
        System.out.println("index 被调用了");
        Page<Hospital> pages = hospitalService.selectPage(page, limit, hospitalQueryVo);

        return Result.ok(pages);

    }


    @ApiOperation(value = "获取医院详情")
    @GetMapping("show/{id}")
    public Result show(
            @ApiParam(name = "id", value = "医院id", required = true)
            @PathVariable String id) {

        System.out.println("show 被调用了"+id);
        return Result.ok(hospitalService.show(id));
    }

    //医院状态更新
    @ApiOperation(value = "医院状态更新")
    @PostMapping("updateStatus/{id}")
     public Result updateStatus(@PathVariable String id){

        hospitalService.updateStatus(id);
        return Result.ok();
    }


}
