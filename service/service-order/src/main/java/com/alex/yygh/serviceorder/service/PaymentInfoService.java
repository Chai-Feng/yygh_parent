package com.alex.yygh.serviceorder.service;

import com.alex.yygh.model.order.OrderInfo;
import com.alex.yygh.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-25-13:30
 */
public interface PaymentInfoService extends IService<PaymentInfo> {
    /**
     * 保存交易记录
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */

    void savePaymentInfo(OrderInfo orderInfo,Integer paymentType);

    /**
     * 根据单号 查询订单详情，修改订单支付状态
     * @param outTradeNo
     * @param paymentType
     * @param paramMap
     */
    void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> paramMap);


    /**
     * 获取支付记录
     * @param orderId
     * @param paymentType
     * @return
     */
    PaymentInfo getPaymentInfo(Long orderId,Integer paymentType);
}
