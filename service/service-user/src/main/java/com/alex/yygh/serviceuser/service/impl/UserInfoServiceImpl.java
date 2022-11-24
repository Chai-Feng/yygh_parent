package com.alex.yygh.serviceuser.service.impl;

import com.alex.yygh.common.exception.YyghException;
import com.alex.yygh.common.helper.JwtHelper;
import com.alex.yygh.common.result.ResultCodeEnum;
import com.alex.yygh.enums.AuthStatusEnum;
import com.alex.yygh.model.user.Patient;
import com.alex.yygh.model.user.UserInfo;
import com.alex.yygh.model.user.UserLoginRecord;
import com.alex.yygh.serviceuser.mapper.UserInfoMapper;
import com.alex.yygh.serviceuser.mapper.UserLoginRecordMapper;
import com.alex.yygh.serviceuser.service.PatientService;
import com.alex.yygh.serviceuser.service.UserInfoService;
import com.alex.yygh.vo.user.LoginVo;
import com.alex.yygh.vo.user.UserAuthVo;
import com.alex.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-19-15:18
 */

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    //已经自动注入 userInfoMapper --》baseMapper

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PatientService patientService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserLoginRecordMapper userLoginRecordMapper;

    /**
     * 手机登录验证 用户存在则登录，不存在则执行注册
     *
     * @param loginVo
     * @return
     */
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        String phone = loginVo.getPhone(); //界面登录的手机号
        String code = loginVo.getCode(); //界面登录的动态密码

        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code) /*|| StringUtils.isEmpty(loginVo.getOpenid())*/) {
            //手机号和动态密码不能为空
            System.out.println("手机号或动态密码或openid不能为空");
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //  校验验证码

        String mobleCode = stringRedisTemplate.opsForValue().get(phone);
        System.out.println("类型 " + mobleCode.getClass() + "   值 " + mobleCode + "===============");


        if (!code.equals(mobleCode)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }


        //绑定手机号码
        UserInfo userInfo = null;
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.getByOpenId(loginVo.getOpenid());
            if (null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                this.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        } //openid 为null 表示直接用手机登录


        if (null == userInfo) {
            //手机号被使用，则登录，手机号如果没有被使用过就注册
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", phone);

            userInfo = baseMapper.selectOne(wrapper); //获取当前手机号的用户
            if (null == userInfo) {
                //此用户不在数据库中，就新建用户
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1); //0:锁定，1：正常
                //新建用户，稍后认证
                this.save(userInfo); //baseMappper
            }
        }

        //如果在user_Info表中查到，说明不是新用户
        //检查用户状态
        if (userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        //记录登录
        UserLoginRecord userLoginRecord = new UserLoginRecord();
        userLoginRecord.setUserId(userInfo.getId());
        userLoginRecord.setIp(loginVo.getIp());
        userLoginRecordMapper.insert(userLoginRecord);


        //返回页面显示名称
        HashMap<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }

        //使用map，保证用户信息安全，只返回部分数据
        map.put("name", name);
        //登录成功生产token
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }


    //微信登录： 根据openid 获取个人信息
    @Override
    public UserInfo getByOpenId(String openid) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<UserInfo>().eq("openid", openid);
        UserInfo user = baseMapper.selectOne(wrapper);
        return user;
    }

    //用户认证
    //user_Info BigInt

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据id查询userInfo信息
        UserInfo userInfo = baseMapper.selectById(userId);

        //设置认证信息
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());

        //认证中 1
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //信息更新
        baseMapper.updateById(userInfo);

    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //UserInfoQueryVo 获取条件值
        String keyword = userInfoQueryVo.getKeyword(); //获取关键字
        Integer status = userInfoQueryVo.getStatus();  //用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus();//用户认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间
        System.out.println("创建查询条件 keyword ：" + keyword + "\t" + "createdTime范围 " + createTimeBegin + "---" + createTimeEnd);
        //对条件值进行非空判断

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        //正则匹配手机号
        Pattern pattern = Pattern.compile("^(1[3,4,5,7,8])\\d{9}$");
        if (!StringUtils.isEmpty(keyword)) {
            Matcher matcher = pattern.matcher(keyword);
            boolean matcherFlag = matcher.find();
            System.out.println("匹配结果" + matcherFlag + "\t测试匹配 17849981097 " + pattern.matcher("17849981097").find());
            if (matcherFlag) {
                wrapper.eq("phone", keyword);
            } else {
                wrapper.like("name", keyword);
            }
        }


        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);
        }
        if (!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status", authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) { //ge 大于等于
            wrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) { //le 小于等于
            wrapper.le("create_time", createTimeEnd);
        }

        IPage<UserInfo> pages = baseMapper.selectPage(pageParam, wrapper);

        pages.getRecords().stream().forEach(item -> {
            this.packageUserInfo(item);
        });

        System.out.println("模糊查询结果 :" + pages.getRecords());
        return pages;
    }


    //编号变成对应值封装
    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态 0  1
        String statusString = userInfo.getStatus().intValue() == 0 ? "锁定" : "正常";
        userInfo.getParam().put("statusString", statusString);
        return userInfo;
    }


    //锁定用户
    @Override
    public void lock(Long userId, Integer status) {
        if (status.intValue() == 0 || status.intValue() == 1) {
            UserInfo user = this.getById(userId);
            user.setStatus(status);
            this.updateById(user);
        }
    }


    @Override
    public Map<String, Object> show(Long userId) {
        UserInfo user = this.getById(userId);
        UserInfo userInfo = this.packageUserInfo(user);
        List<Patient> patientList = patientService.findAllUserById(userId);

        HashMap<String, Object> result = new HashMap<>();
        result.put("userInfo",userInfo);
        result.put("patientList",patientList);
        return result;
    }


    //用户认证
    @Override
    public void approval(Long userId, Integer authStatus) {

        if(authStatus.intValue()==2 || authStatus.intValue()==-1) {
            UserInfo userInfo = this.getById(userId);
            userInfo.setAuthStatus(authStatus);
            this.updateById(userInfo);
        }

    }
}
