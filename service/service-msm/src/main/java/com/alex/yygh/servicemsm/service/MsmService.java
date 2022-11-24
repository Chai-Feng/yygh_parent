package com.alex.yygh.servicemsm.service;

import com.alex.yygh.vo.msm.MsmVo;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-19-18:38
 */
public interface MsmService {

    //发送手机验证码
    boolean send(String phone, String code);

    //方法重写，发送rabbit 消息
    boolean send(MsmVo msmVo);
}
