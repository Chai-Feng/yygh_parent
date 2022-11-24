package com.alex.yygh.serviceoss.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-21-13:08
 */
@Component
public class ConstantOssPropertiesUtils implements InitializingBean {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String secret;

    @Value("${aliyun.oss.bucket}")
    private String bucket;


    public static  String ENDPOINT;
    public static  String ACCESS_KEY_ID;
    public static  String ACCESS_KEY_SECRECT;
    public static  String BUCKET;


    @Override
    public void afterPropertiesSet() throws Exception {

        ENDPOINT=endpoint;
        ACCESS_KEY_ID=accessKeyId;
        ACCESS_KEY_SECRECT=secret;
        BUCKET=bucket;
    }
}
