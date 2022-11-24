package com.alex.yygh.common.utils;

import com.alex.yygh.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-21-15:55
 */

//获取当前用户信息工具类
public class AuthContextHolder {

    //获取当前用户id
    public static Long getUserId(HttpServletRequest request){
        //从header获取token
        String token = request.getHeader("token");
        //jwt 从token 获取userId
        Long userId = JwtHelper.getUserId(token);
        return userId;

    }


    //获取当前用户名
    public static String getUserName(HttpServletRequest request){
        //从header获取token
        String token = request.getHeader("token");
        //jwt 从token 获取userId
        String name = JwtHelper.getUserName(token);
        return name;

    }
}
