package com.alex.yygh.hosp.controller.api;

import com.alex.yygh.common.exception.YyghException;
import com.alex.yygh.common.helper.HttpRequestHelper;
import com.alex.yygh.common.result.Result;
import com.alex.yygh.common.result.ResultCodeEnum;
import com.alex.yygh.common.utils.MD5;

import com.alex.yygh.hosp.service.DepartmentService;
import com.alex.yygh.hosp.service.HospitalService;
import com.alex.yygh.hosp.service.HospitalSetService;

import com.alex.yygh.hosp.service.ScheduleService;
import com.alex.yygh.model.hosp.Department;
import com.alex.yygh.model.hosp.Schedule;
import com.alex.yygh.vo.hosp.DepartmentQueryVo;

import com.alex.yygh.vo.hosp.ScheduleQueryVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-14-13:21
 */


@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
//@CrossOrigin
public class ApiController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalSetService hospitalSetService;
    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();

        //把Map的格式进行转换
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //签名校验
        //1、获取医院系统传递过来的签名，签名被HttpHelper MD5加密
        String hospSign = (String) paramMap.get("sign");

        //2、根据传递过来的参数的 医院编码，查询数据库，看签名是否一样
        String hoscode = (String) paramMap.get("hoscode");

        //3、把数据库查出来的签名进行MD5加密
        String signKey = hospitalSetService.getSignKey(hoscode); //能查到说明存在
        System.out.println(signKey);
        //String MD5SignKey = MD5.encrypt(signKey);

        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoDataString = (String) paramMap.get("logoData");
        if (!StringUtils.isEmpty(logoDataString)) {
            String logoData = logoDataString.replaceAll(" ", "+");
            paramMap.put("logoData", logoData);
            System.out.println("检测+是否存在 ===  api " + logoData);

        }

        //签名校验

        if (!HttpRequestHelper.isSignEquals(paramMap, signKey)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        } else {
            hospitalService.save(paramMap);
        }

        return Result.ok();
    }

    //查询医院接口
    @ApiOperation(value = "获取医院信息")
    @PostMapping("hospital/show")
    public Result hospital(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //   String hosSign = (String) paramMap.get("sign");
        //进行参数校验
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        //String signKeyMD5 = HttpRequestHelper.getSign(paramMap,signKey);

        if (!HttpRequestHelper.isSignEquals(paramMap, signKey)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //  System.out.println(hospitalService.getByHoscode(hoscode).getLogoData());
        return Result.ok(hospitalService.getByHoscode(hoscode));

    }


    //上传科室
    @ApiOperation(value = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        String hoscode = (String) paramMap.get("hoscode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        departmentService.save(paramMap);

        return Result.ok();

    }


    @ApiOperation(value = "获取分页列表")
    @PostMapping("department/list")
    public Result department(HttpServletRequest request) {

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");

        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String) paramMap.get("limit"));

        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setDepcode(depcode);
        departmentQueryVo.setHoscode(hoscode);
        Page<Department> pageModel = departmentService.selectPage(page, limit, departmentQueryVo);

        return Result.ok(pageModel);

    }


    //删除科室
    @ApiOperation(value = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验 略
        String hoscode = (String) paramMap.get("hoscode");
        //必填
        String depcode = (String) paramMap.get("depcode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        departmentService.remove(hoscode, depcode);
        return Result.ok();

    }


    //上传医生排班
    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        System.out.println("上传schedule 从request中获得的paramMap == "+paramMap);
        String hoscode = (String) paramMap.get("hoscode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        scheduleService.save(paramMap);
        return Result.ok();

    }

    //分页查询排班
    @ApiOperation(value = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result schedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验
        String hoscode = (String) paramMap.get("hoscode");
        //非必填
        String depcode = (String) paramMap.get("depcode");
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String) paramMap.get("limit"));

        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        Page<Schedule> pageModel = scheduleService.selectPage(page, limit, scheduleQueryVo);

        return Result.ok(pageModel);
    }

    @ApiOperation(value = "删除科室")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验 略
        String hoscode = (String)paramMap.get("hoscode");
        //必填
        String hosScheduleId = (String)paramMap.get("hosScheduleId");
        if(StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if(!HttpRequestHelper.isSignEquals(paramMap, hospitalSetService.getSignKey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        scheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();
    }


}
