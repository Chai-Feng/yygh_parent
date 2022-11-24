package com.alex.yygh.hosp.controller;

import com.alex.yygh.common.exception.YyghException;
import com.alex.yygh.common.result.Result;
import com.alex.yygh.common.result.ResultCodeEnum;
import com.alex.yygh.common.utils.MD5;
import com.alex.yygh.hosp.service.HospitalSetService;
import com.alex.yygh.model.hosp.HospitalSet;
import com.alex.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.sql.Wrapper;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-08-10:53
 */
@Api(tags = "医院设置管理") //显示在swagger ui中
@Slf4j
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
//@CrossOrigin //允许跨域访问  Access-Control-Allow-Origin'
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    //查找医院所有记录
    @ApiOperation(value = "获取全部医院信息")
    @GetMapping("findAll")
    public Result findAllHospitalSet() {
        List<HospitalSet> list = hospitalSetService.list();

        return Result.ok(list); //默认返回一个json数据
    }


    //逻辑删除医院记录
    @ApiOperation(value = "根据id 删除指定医院")
    @DeleteMapping("/delete/{id}")
    public Result removeHospitalSet(@PathVariable("id") Long id) {

        boolean result = hospitalSetService.removeById(id);

        if (result == true) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //3、条件查询 带分页 （医院名称，医院编号）
    @ApiOperation(value = "按条件查询医院")
    @PostMapping("findPage/{current}/{limit}")
    public Result findPageHospSet(@PathVariable("current") long current,
                                  @PathVariable long limit,
                                  @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
//@RequestBody 通过json数据封装到对象中传参数，required为false 表示此参数可用为空，同时注意，加上这个注解
//请求类型是Post
        //创建Page对象
        Page<HospitalSet> page = new Page<>(current, limit);
        //调用方法实现分页查询
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();

        String hoscode = hospitalSetQueryVo.getHoscode();
        String hosname = hospitalSetQueryVo.getHosname();

        if (!StringUtils.isEmpty(hosname)) {
            wrapper.like("hosname", hosname); //模糊查询

        }
        if (!StringUtils.isEmpty(hoscode)) {
            wrapper.eq("hoscode", hoscode);
        }


        //调用方法实现分页查询
        Page<HospitalSet> hosp = hospitalSetService.page(page, wrapper);

        return Result.ok(hosp);
    }


    //4、添加医院设置
    @ApiOperation(value = "添加医院设置")
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet) {
        //设置状态 1使用 0 不能使用
        hospitalSet.setStatus(1);
        //签名密钥
        Random random = new Random();
        String signKey = MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000));
        hospitalSet.setSignKey(signKey);

        //调用service
        boolean save = hospitalSetService.save(hospitalSet);
        if (save) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //根据id获取医院设置
    @ApiOperation(value = "根据id获取医院设置")
    @GetMapping("getHospitalSet/{id}")
    public Result getHospitalSet(@PathVariable long id) {

        //int i =10/0; //模拟异常

//        try {
//            int i =10/0;
//        }catch (Exception e){
//            throw  new YyghException(ResultCodeEnum.SERVICE_ERROR);
//        }
        HospitalSet hospitalSet = hospitalSetService.getById(id);

        return Result.ok(hospitalSet);
    }

    //修改医院设置
    @ApiOperation(value = "修改医院设置")
    @PostMapping("updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet) {

        boolean flag = hospitalSetService.updateById(hospitalSet);
        if (flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //批量删除医院设置
    @ApiOperation(value = "批量删除医院设置")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idlist) {

        boolean flag = hospitalSetService.removeByIds(idlist);
        if (flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }

    }


    //8、医院设置锁定和解锁
    @ApiOperation(value = "锁定")
    @PutMapping("lock/{id}/{status}")
    public Result lock(@ApiParam(name = "id", value = "医院设置id", required = true)
                       @PathVariable Long id,
                       @ApiParam(name = "status", value = "锁定状态(0：锁定，1：解锁)", required = true)
                       @PathVariable Integer status) {

        HospitalSet hosp = hospitalSetService.getById(id);
        hosp.setStatus(status);
        hospitalSetService.updateById(hosp);

        return Result.ok();
    }


    //9、发送签名密钥
    @PutMapping("sendKey/{id}")
    public Result sendHospitalKey(
            @ApiParam(name = "id", value = "医院设置id", required = true)
            @PathVariable Long id
    ){
        HospitalSet hosp = hospitalSetService.getById(id);
        String signKey = hosp.getSignKey();
        String hoscode = hosp.getHoscode();
        //TODO 发送短信接口

        return Result.ok();

    }


}
