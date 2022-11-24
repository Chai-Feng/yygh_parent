package com.alex.yygh.servergateway.filter;

import com.alex.yygh.common.helper.JwtHelper;
import com.alex.yygh.common.result.Result;
import com.alex.yygh.common.result.ResultCodeEnum;
import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 * 全局Filter，统一处理会员登录与外部不允许访问的服务
 * </p>
 *
 * @author qy
 * @since 2019-11-21
 */

@Component
public class AuthGlobaFilter implements GlobalFilter, Ordered {

   private AntPathMatcher antPathMatcher =new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        System.out.println("== "+path);

        //内部服务接口，不允许外部访问
        if(antPathMatcher.match("/**/inner/**",path)){
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION); //没有权限
        }

        //注意 只有获取token 才能调用getUserId, 否则会报错 MalformedJwtException: JWT strings must contain exactly 2 period characters.
        //在进入admin路径时，不存在token 会报错
        Long userId = null; //api端 检查登录，认证使用token获取
        //api接口，异步请求，校验用户必须登录
        if(antPathMatcher.match("/api/**/schedule/**", path)) {
            userId =this.getUserId(request);
            if(StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                System.out.println("用户没有登录，返回208");
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }

//        if(antPathMatcher.match("/api/**/auth/**", path)) {
//            if(StringUtils.isEmpty(userId)) {
//                ServerHttpResponse response = exchange.getResponse();
//                System.out.println("用户验证");
//                return out(response, ResultCodeEnum.LOGIN_AUTH);
//            }
//        }
//

        return chain.filter(exchange);


    }

    @Override
    public int getOrder() {
        return 0;
    }

    /*
    * 获取当前登录用户id
     * @param request
     * @return
             */
    private Long getUserId(ServerHttpRequest request) {
        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if(null  != tokenList) {
            token = tokenList.get(0);
        }
        if(!StringUtils.isEmpty(token)) {
            return JwtHelper.getUserId(token);
        }
        return null;
    }


    /**
     * api接口鉴权失败返回数据
     * @param response
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }


}
