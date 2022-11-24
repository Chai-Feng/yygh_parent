package com.alex.yygh.serviceuser.controller;

import com.alex.yygh.common.exception.YyghException;
import com.alex.yygh.common.helper.JwtHelper;
import com.alex.yygh.common.result.Result;
import com.alex.yygh.common.result.ResultCodeEnum;
import com.alex.yygh.model.user.UserInfo;
import com.alex.yygh.serviceuser.config.ConstantPropertiesUtil;
import com.alex.yygh.serviceuser.service.UserInfoService;
import com.alex.yygh.serviceuser.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信登录
 * @date 2022-11-20-17:52
 */

@Controller
@RequestMapping("/api/ucenter/wx")
@Slf4j

public class WeixinApiController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取微信登陆参数  二维码展示的，还未扫描登录
     */
    //http://localhost/api/ucenter/wx/getLoginParam

    @GetMapping("getLoginParam")
    @ResponseBody
    public Result genQrConnection(HttpSession session) throws UnsupportedEncodingException {


        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis()+"");//System.currentTimeMillis()+""
        System.out.println("获取微信登录配置参数");
        return Result.ok(map);

    }

    /*
    微信登录回调
    http://localhost:8160/api/ucenter/wx/callback?code=011a760w33bdCZ2d8m2w3T3NLr1a760T&state=1668948736109
    @param :code
    @param :state
     */
    //从前端获取到的

    @RequestMapping(value = "callback")
    public String callBack(String code,String state){
        System.out.println("微信授权服务器回调。。。。。。");
        System.out.println("state = " + state);
        System.out.println("code = " + code);
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(code)) {
            log.error("非法回调请求");
            throw new YyghException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }
    //使用code和appid以及appscrect换取access_token
        StringBuffer baseAccessTokenUrl=  new StringBuffer().append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");




        String accessTokenUrl =String.format(baseAccessTokenUrl.toString(),ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);

        String result = null;
        try {
            result = HttpClientUtils.get(accessTokenUrl);
        } catch (Exception e) {
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        System.out.println("使用code换取的access_token结果 = " + result);

        JSONObject resultJson = JSONObject.parseObject(result);
        if(resultJson.getString("errcode") != null){
            log.error("获取access_token失败：" + resultJson.getString("errcode") + resultJson.getString("errmsg"));
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        String accessToken = resultJson.getString("access_token");
        String openId = resultJson.getString("openid");
        log.info(accessToken);
        log.info(openId);

        //根据access_token获取微信用户的基本信息
        //先根据openid进行数据库查询
        UserInfo userInfo = userInfoService.getByOpenId(openId);
        // 如果没有查到用户信息,那么调用微信个人信息获取的接口
         if(null == userInfo){
        //如果查询到个人信息，那么直接进行登录,没有查到，新建
        //使用access_token换取受保护的资源：微信的个人信息
        String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                "?access_token=%s" +
                "&openid=%s";
        String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openId);
        String resultUserInfo = null;
        try {
            resultUserInfo = HttpClientUtils.get(userInfoUrl);
        } catch (Exception e) {
            throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
        }
        System.out.println("使用access_token获取用户信息的结果 = " + resultUserInfo);

        JSONObject resultUserInfoJson = JSONObject.parseObject(resultUserInfo);
        if(resultUserInfoJson.getString("errcode") != null){
            log.error("获取用户信息失败：" + resultUserInfoJson.getString("errcode") + resultUserInfoJson.getString("errmsg"));
            throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
        }

        //解析用户信息
        String nickname = resultUserInfoJson.getString("nickname");
       // String headimgurl = resultUserInfoJson.getString("headimgurl");

         userInfo = new UserInfo();
        //存入新用户的openid
        userInfo.setOpenid(openId);
        userInfo.setNickName(nickname);
        userInfo.setStatus(1);
        userInfoService.save(userInfo);
         }

        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //没绑定手机，手机号为null 要进行绑定手机的操作，前端对openid判断，跳转到绑定手机，
        if(StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", userInfo.getOpenid());
        } else {
            //绑定了手机，openid=null 前端判断后，直接登录
            map.put("openid", "");
        }
        System.out.println("封装callBack map openid"+map.get("openid")+" phone "+userInfo.getPhone());
        //新建一个token
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return "redirect:" + ConstantPropertiesUtil.YYGH_BASE_URL + "/weixin/callback?token="+map.get("token")+"&openid="+map.get("openid")+"&name="+URLEncoder.encode((String)map.get("name"));
    }







}
