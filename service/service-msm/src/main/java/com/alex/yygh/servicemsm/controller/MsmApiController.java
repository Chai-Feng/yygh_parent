package com.alex.yygh.servicemsm.controller;

import com.alex.yygh.common.result.Result;
import com.alex.yygh.servicemsm.service.MsmService;
import com.alex.yygh.servicemsm.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-19-19:24
 */

@RestController
@RequestMapping("/api/msm")
public class MsmApiController {

    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    //发送手机验证码
    @GetMapping("send/{phone}")
    public Result sendCode(@PathVariable String phone) {
        //从redis获取验证码，如果获取获取到，返回ok
        // key 手机号  value 验证码
        String code = redisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(code)) {
            //return Result.ok();
        }
        //如果从redis获取不到，
        // 生成验证码，
        code = RandomUtil.getSixBitRandom();
        //调用service方法，通过整合短信服务进行发送
        boolean isSend = msmService.send(phone,code);

        //    测试点
//        redisTemplate.opsForValue().set(phone,code,2, TimeUnit.MINUTES);
//        System.out.println("从redis上获取验证码 ="+redisTemplate.opsForValue().get(phone));
//        return Result.ok();

        if(isSend) {
            //生成验证码放到redis里面，设置有效时间
            redisTemplate.opsForValue().set(phone,code,2, TimeUnit.MINUTES);
            System.out.println("从redis上获取验证码 ="+redisTemplate.opsForValue().get(phone));
            return Result.ok();
        } else {
            return Result.fail().message("发送短信失败");
        }
    }

}
