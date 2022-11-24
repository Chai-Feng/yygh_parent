package com.alex.yygh.serviceuser.controller;

import com.alex.yygh.common.result.Result;
import com.alex.yygh.common.utils.AuthContextHolder;
import com.alex.yygh.model.user.UserInfo;
import com.alex.yygh.serviceuser.service.UserInfoService;
import com.alex.yygh.serviceuser.utils.Iputil;
import com.alex.yygh.vo.user.LoginVo;
import com.alex.yygh.vo.user.UserAuthVo;
import com.alibaba.nacos.client.utils.IPUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-19-15:22
 */

@Api(tags = "用户登录接口")
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {


    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation(value = "会员登录")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo, HttpServletRequest httpRequest){

        //Ip创建，值得研究
        loginVo.setIp(Iputil.getIpAddr(httpRequest));
        System.out.println("创建IP "+Iputil.getIpAddr(httpRequest));
        System.out.println("检查登录时 openid "+loginVo);
        Map<String, Object> info = userInfoService.login(loginVo);
        return Result.ok(info);


    }


    //用户认证
    @ApiOperation(value="用户验证")
    @PostMapping("auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo,HttpServletRequest request){

        Long userId = AuthContextHolder.getUserId(request);
        userInfoService.userAuth(userId,userAuthVo);
        System.out.println("用户验证 "+userId);
        return Result.ok();
    }


    //获取用户id
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        System.out.println("获取用户信息 "+userInfo);
        return Result.ok(userInfo);
    }

}
