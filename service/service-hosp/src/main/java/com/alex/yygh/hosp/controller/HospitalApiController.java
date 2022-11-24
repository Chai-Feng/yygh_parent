package com.alex.yygh.hosp.controller;

import com.alex.yygh.common.result.Result;
import com.alex.yygh.hosp.service.DepartmentService;
import com.alex.yygh.hosp.service.HospitalService;
import com.alex.yygh.hosp.service.HospitalSetService;
import com.alex.yygh.hosp.service.ScheduleService;
import com.alex.yygh.model.hosp.Hospital;
import com.alex.yygh.vo.hosp.DepartmentVo;
import com.alex.yygh.vo.hosp.HospitalQueryVo;
import com.alex.yygh.vo.hosp.ScheduleOrderVo;
import com.alex.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description:
 * 要显示的医院信息包括：医院名 logo 等级 放号时间
 * @author: Alex
 * @Version:
 * @date 2022-11-18-15:08
 */

@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    //预约挂号页面会展示全部的科室信息
    @Autowired
    private DepartmentService departmentService;


    @Autowired
    private ScheduleService scheduleService;


    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(
            @PathVariable Integer page,
            @PathVariable Integer limit,
            HospitalQueryVo hospitalQueryVo) {
        //显示上线的医院 status =1
        Page<Hospital> hospitals = hospitalService.selectPage(page, limit, hospitalQueryVo);
        return Result.ok(hospitals);
    }


    //根据医院名称获取医院列表 模糊查询
    @ApiOperation(value = "根据医院名称获取医院列表")
    @GetMapping("findByHosname/{hosname}")
    public Result findByHosname(@PathVariable String hosname){
        List<Hospital> hospitals = hospitalService.findByHosname(hosname);

        return Result.ok(hospitals);
    }



    //预约挂号页面分为两个部分： 预约规则、挂号规则 bookingRule  ，医院名称 hosname 医院等级 hostype 医院logo
    //我们以一个Map 来接收来自多个实体对象的数据(BookingRule,hospital)



    @ApiOperation(value = "预约挂号 展示挂号规则，预约规则")
    @GetMapping("booking/{hoscode}")
    public  Result indexBookingRule(@PathVariable String hoscode){
        //返回一个Map<String ,Object> 包含hospital 和bookingRule 两个对象的内容
        Map<String, Object> item = hospitalService.item(hoscode);
        return Result.ok(item);
    }


    //部门详情
    @ApiOperation(value = "预约挂号 展示部门树")
    @GetMapping("department/{hoscode}")
    public Result indexDepartment(@PathVariable String hoscode){

        List<DepartmentVo> departmentTree = departmentService.findDepartmentTree(hoscode);

        return Result.ok(departmentTree);
    }



    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(

            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Integer page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Integer limit,
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable String depcode) {


        return Result.ok(scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode));
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable String depcode,
            @ApiParam(name = "workDate", value = "排班日期", required = true)
            @PathVariable String workDate) {


        return Result.ok(scheduleService.getScheduleDetail(hoscode, depcode, workDate));
    }
    @ApiOperation(value = "根据排班id获取排班数据")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable String scheduleId) {
        return Result.ok(scheduleService.getById(scheduleId));
    }


    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }


    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable("hoscode") String hoscode) {
        return hospitalSetService.getSignInfoVo(hoscode);
    }



}