package com.alex.yygh.serviceuser.controller;

import com.alex.yygh.common.result.Result;
import com.alex.yygh.common.utils.AuthContextHolder;
import com.alex.yygh.model.user.Patient;
import com.alex.yygh.serviceuser.service.PatientService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-21-18:50
 */

@RestController
@RequestMapping("api/user/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;


    //获取就诊人列表
    @GetMapping("auth/findAll")
    public Result findAll(HttpServletRequest request){

        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> patients = patientService.findAllUserById(userId);

        return Result.ok(patients);
    }


    //添加就诊人
    @PostMapping("auth/save")
    public Result savePatient(@RequestBody Patient patient,HttpServletRequest request){

        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();

    }

    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public Result deletePatient(@PathVariable Long id){
        patientService.removeById(id);
        return Result.ok();
    }

    //修改就诊人信息
    @PutMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient){
        patientService.updateById(patient);
        return Result.ok();
    }

    //根据id 查询就诊人信息

    @GetMapping("auth/get/{id}")
    public Result getPatient(@PathVariable Long id){
        Patient patientById = patientService.getPatientById(id);

        return Result.ok(patientById);
    }


    @ApiOperation(value = "获取就诊人")
    @GetMapping("inner/get/{id}")

    public Patient getPatientOrder(
            @ApiParam(name = "id", value = "就诊人id", required = true)
            @PathVariable("id") Long id) {
        //记得要封装一下(调用pack )
        return patientService.getPatientById(id);
    }

}
