package com.alex.yygh.serviceorder.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-25-13:14
 */
@Component
public class ConstantPropertiesUtils implements InitializingBean {

    @Value("${weixin.pay.appid}")
    private String appid;

    @Value("${weixin.pay.partner}")
    private String partner;

    @Value("${weixin.pay.partnerkey}")
    private String partnerkey;

    @Value("${weixin.cert}")
    private String cert;

    public static String APPID;
    public static String PARTNER;
    public static String PARTNERKEY;

    public static String CERT;

    @Override
    public void afterPropertiesSet() throws Exception {
        APPID = appid;
        PARTNER = partner;
        PARTNERKEY = partnerkey;
        CERT=cert;
    }
}
