package com.alex.yygh.serviceuser.controller;

import com.alex.yygh.common.result.Result;
import com.alex.yygh.model.user.UserInfo;
import com.alex.yygh.serviceuser.service.UserInfoService;
import com.alex.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-21-21:24
 */

@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private UserInfoService userInfoService;

    //用户列表 条件分页查询
    @PostMapping("{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit,  @RequestBody(required = false) UserInfoQueryVo userInfoQueryVo){

        Page<UserInfo> pageParam = new Page<>(page,limit);
        IPage<UserInfo> pageModel = userInfoService.selectPage(pageParam, userInfoQueryVo);
        return Result.ok(pageModel);
    }

    //锁定用户
    @ApiOperation(value = "锁定")
    @GetMapping("lock/{userId}/{status}")
    public Result lock(
            @PathVariable("userId") Long userId,
            @PathVariable("status") Integer status){
        userInfoService.lock(userId, status);
        return Result.ok();
    }

    //展示用户详情 UserInfo 和 patient 就诊人信息
    @GetMapping("show/{userId}")
    public  Result show(@PathVariable Long userId){

        Map<String, Object> result = userInfoService.show(userId);

        return Result.ok(result);
    }

    //用户认证
    @GetMapping("approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId,@PathVariable Integer authStatus) {
        userInfoService.approval(userId,authStatus);
        return Result.ok();
    }


}
