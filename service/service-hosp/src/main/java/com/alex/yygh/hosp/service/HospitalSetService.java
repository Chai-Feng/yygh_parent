package com.alex.yygh.hosp.service;

import com.alex.yygh.model.hosp.Hospital;
import com.alex.yygh.model.hosp.HospitalSet;
import com.alex.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-07-22:44
 */
public interface HospitalSetService extends IService<HospitalSet> {
    //通过hoscode 查询医院set 目的为了验证上传医院时 签名验证
    String getSignKey(String hoscode);


    //获取医院签名信息
    SignInfoVo getSignInfoVo(String hoscode);
}
