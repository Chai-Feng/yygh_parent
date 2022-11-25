package com.alex.yygh.serviceorder.service.impl;

import com.alex.yygh.enums.RefundStatusEnum;
import com.alex.yygh.model.order.PaymentInfo;
import com.alex.yygh.model.order.RefundInfo;
import com.alex.yygh.serviceorder.mapper.xml.RefundInfoMapper;
import com.alex.yygh.serviceorder.service.RefundInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.util.Date;

/**
 * @Title:
 * @Description:
 * 退款
 *
 * @author: Alex
 * @Version:
 * @date 2022-11-25-20:01
 */

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    /**
     * 保存退款： 点击退款按钮，次方法被调用，
     * @param paymentInfo
     * @return
     */
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {

        QueryWrapper<RefundInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",paymentInfo.getOrderId());
        wrapper.eq("payment_type",paymentInfo.getPaymentType());

        RefundInfo refundInfo = baseMapper.selectOne(wrapper);
        if(null!=refundInfo) return refundInfo; //已经提交退款，不需要重复提交

         refundInfo = new RefundInfo();

         refundInfo.setOrderId(paymentInfo.getOrderId());
         refundInfo.setSubject(paymentInfo.getSubject());
         refundInfo.setPaymentType(paymentInfo.getPaymentType());
         refundInfo.setCreateTime(new Date());
         refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
         refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
         refundInfo.setTotalAmount(paymentInfo.getTotalAmount());

         baseMapper.insert(refundInfo);
         return refundInfo;





    }
}
