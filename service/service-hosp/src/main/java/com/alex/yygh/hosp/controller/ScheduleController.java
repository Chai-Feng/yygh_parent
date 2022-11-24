package com.alex.yygh.hosp.controller;

import com.alex.yygh.common.result.Result;
import com.alex.yygh.hosp.service.ScheduleService;
import com.alex.yygh.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-17-13:58
 */
 //  /admin/hosp/schedule/getScheduleRule/${page}/${limit}/${hoscode}/${depcode}
@Api(tags = "医院排班信息")
@RestController
@RequestMapping("/admin/hosp/schedule")
//@CrossOrigin
public class ScheduleController {


    @Autowired
    private ScheduleService scheduleService ;

    //根据医院编号 和 科室编号 ，查询排班规则数据
    @ApiOperation(value ="查询排班规则数据")
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable long page,
                                  @PathVariable long limit,
                                  @PathVariable String hoscode,
                                  @PathVariable String depcode
    ){
        Map<String, Object> scheduleRule = scheduleService.getScheduleRule(page, limit, hoscode, depcode);
        System.out.println("排班查询结果 == "+scheduleRule);
        return Result.ok(scheduleRule);
    }


    //根据排班日期获取排班详情
    ///admin/hosp/schedule/getScheduleDetail/{hoscode}/{depcode}/{workDate}
    @ApiOperation(value ="根据排班日期获取排班详情")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail(@PathVariable String hoscode,
                                    @PathVariable String depcode,
                                    @PathVariable String workDate ){

        List<Schedule> scheduleList =scheduleService.getScheduleDetail(hoscode,depcode,workDate);
        System.out.println("根据日期获取具体排班详情 = "+scheduleList);
        return Result.ok(scheduleList);

    }

}
