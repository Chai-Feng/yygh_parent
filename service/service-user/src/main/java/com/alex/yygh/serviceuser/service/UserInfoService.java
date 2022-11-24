package com.alex.yygh.serviceuser.service;

import com.alex.yygh.model.user.UserInfo;
import com.alex.yygh.vo.user.LoginVo;
import com.alex.yygh.vo.user.UserAuthVo;
import com.alex.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;


/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-19-15:15
 */
public interface UserInfoService extends IService<UserInfo> {

    //会员登录
    Map<String,Object> login(LoginVo loginVo);

    /**
     * 根据openid 获取用户信息
     */
    UserInfo getByOpenId(String openid);


    /**
     * 用户认证
     */

    void userAuth(Long userId, UserAuthVo userAuthVo);


    /**
     * 用户列表(条件查询带分页)
     */

    IPage<UserInfo> selectPage (Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);


    /**
     * 锁定用户操作
     */

    void lock(Long userId,Integer status);

    /**
     * 展示用户详情
     * userInfo, patient
     */
    Map<String, Object> show(Long userId);

    /**
     * 认证审批
     */
    void approval (Long userId,Integer authStatus);
}
