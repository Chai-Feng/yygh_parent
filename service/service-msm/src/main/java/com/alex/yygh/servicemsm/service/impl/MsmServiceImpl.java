package com.alex.yygh.servicemsm.service.impl;

import com.alex.yygh.servicemsm.config.ConstantPropertiesUtils;
import com.alex.yygh.servicemsm.service.MsmService;

import com.alex.yygh.vo.msm.MsmVo;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;



import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;


import com.aliyuncs.dysmsapi.model.v20170525.*;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-19-18:39
 */
@Service
public class MsmServiceImpl implements MsmService {


    @Override
    public  boolean send(String phone, String code) {

        //判断手机号是否为空
        if(StringUtils.isEmpty(phone)) {
            return false;
        }
        //整合阿里云短信服务
        //设置相关参数


        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", ConstantPropertiesUtils.ACCESS_KEY_ID, ConstantPropertiesUtils.SECRECT);

        System.out.println("----AccessKeyId ---"+ConstantPropertiesUtils.ACCESS_KEY_ID);
        IAcsClient client = new DefaultAcsClient(profile);

        SendSmsRequest request = new SendSmsRequest();
        request.setSignName("阿里云短信测试");
        request.setTemplateCode("SMS_154950909");  //SMS_154950909  //"SMS_259630206
        request.setPhoneNumbers(phone);
        System.out.println("要发送的内容-- "+code);
        request.setTemplateParam("{\"code\":\""+code+"\"}");



        System.out.println("----TemplateParam ---"+request.getTemplateParam());

        try {
            SendSmsResponse response = client.getAcsResponse(request);
            System.out.println(JSONObject.toJSONString(response)+"  response ");
            System.out.println("response code: "+response.getCode()+"  response message :"+response.getMessage());
           if("OK".equalsIgnoreCase(response.getCode())&&"OK".equalsIgnoreCase(response.getMessage())) {
               return true;
           }
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
        return false;
    }


    //发送rabbit消息

    @Override
    public boolean send(MsmVo msmVo) {
        if(!StringUtils.isEmpty(msmVo)){
            String code = (String)msmVo.getParam().get("code");
            System.out.println("监听到RabbitMq code"+code +" phone "+msmVo.getPhone());
            return this.send(msmVo.getPhone(),code);
        }
        return false;
    }
}
