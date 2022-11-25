package com.alex.yygh.serviceorder.service;

import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-25-13:56
 */
public interface WeixinService {

    /**
     * 根据订单号下单，生成支付链接(预支付，生成二维码，过程)
     */

    Map createNative(Long orderId);

    /**
     * 根据订单号去微信第三方查询支付状态
     */

    Map queryPayStatus(Long orderId, String paymentType);

    /***
     * 退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);

}
