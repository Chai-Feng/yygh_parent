package com.alex.yygh.serviceorder.service;

import com.alex.yygh.model.order.PaymentInfo;
import com.alex.yygh.model.order.RefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-25-20:00
 */
public interface RefundInfoService extends IService<RefundInfo> {


    /**
     * 保存退款记录
     * @param paymentInfo
     * @return
     */
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}
