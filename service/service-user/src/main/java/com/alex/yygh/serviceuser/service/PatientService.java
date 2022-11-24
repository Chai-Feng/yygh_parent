package com.alex.yygh.serviceuser.service;

import com.alex.yygh.model.user.Patient;
import com.alex.yygh.serviceuser.mapper.PatientMapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-21-18:11
 */
public interface PatientService extends IService<Patient> {

    //获取就诊人列表
    //一个用户，可能有多个就诊信息
    List<Patient> findAllUserById(Long userId);

    //根据id 获取就诊人信息
    Patient getPatientById(Long id);
}
