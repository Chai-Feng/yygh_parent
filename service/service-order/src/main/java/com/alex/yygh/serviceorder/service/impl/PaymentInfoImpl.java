package com.alex.yygh.serviceorder.service.impl;

import com.alex.yygh.common.exception.YyghException;
import com.alex.yygh.common.helper.HttpRequestHelper;
import com.alex.yygh.common.result.ResultCodeEnum;
import com.alex.yygh.enums.OrderStatusEnum;
import com.alex.yygh.enums.PaymentStatusEnum;
import com.alex.yygh.hospclient.hosp.HospitalFeignClient;
import com.alex.yygh.model.order.OrderInfo;
import com.alex.yygh.model.order.PaymentInfo;
import com.alex.yygh.serviceorder.mapper.PaymentIntoMapper;
import com.alex.yygh.serviceorder.service.OrderInfoService;
import com.alex.yygh.serviceorder.service.PaymentInfoService;
import com.alex.yygh.vo.order.SignInfoVo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-25-13:32
 */
@Service
public class PaymentInfoImpl extends ServiceImpl<PaymentIntoMapper, PaymentInfo> implements PaymentInfoService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    /**
     *
     * @param orderInfo
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {

        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderInfo.getId());
        wrapper.eq("payment_type",paymentType);
        Integer count = baseMapper.selectCount(wrapper);
        if(count>0) return;;

        //保存交易记录
        PaymentInfo paymenInfo = new PaymentInfo();
        paymenInfo.setCreateTime(new Date());
        paymenInfo.setOrderId(orderInfo.getId());
        paymenInfo.setPaymentType(paymentType);

        paymenInfo.setOutTradeNo(orderInfo.getOutTradeNo()); //订单流水号
        //支付状态：未支付
        paymenInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());

        //交易内容
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + "|" + orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle();

        paymenInfo.setSubject(subject);
        paymenInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymenInfo);

    }

    //支付成功，修改订单详情
    @Override
    public void paySuccess(String out_trade_no, Integer paymentType, Map<String, String> paramMap) {
        PaymentInfo paymentInfo = this.getPayment(out_trade_no, paymentType);
        if(null==paymentInfo){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        if (paymentInfo.getPaymentStatus() != PaymentStatusEnum.UNPAID.getStatus()) {
            return;
        }
        //修改支付状态
        PaymentInfo paymentInfoUpd = new PaymentInfo();
        paymentInfoUpd.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfoUpd.setTradeNo(paramMap.get("transaction_id"));
        paymentInfoUpd.setCallbackTime(new Date());
        paymentInfoUpd.setCallbackContent(paramMap.toString());
        this.updatePaymentInfo(out_trade_no, paymentInfoUpd);

        //修改订单状态
        OrderInfo orderInfo = orderInfoService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus()); //订单已支付
        orderInfoService.updateById(orderInfo);

        //调用医院接口 通知更新订单状态
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if(null == signInfoVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");

        if(result.getInteger("code")!=200){
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }

    }

    /**
     * 更改支付记录
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    private void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd) {

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<PaymentInfo>().eq("out_trade_no", out_trade_no);
        baseMapper.update(paymentInfoUpd,queryWrapper);
    }


    /**
     * 根据订单号，支付类型 获取支付详情
     * @param out_trade_no
     * @param paymentType
     * @return
     */
    private  PaymentInfo getPayment(String out_trade_no, Integer paymentType){
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<PaymentInfo>().eq("out_trade_no",out_trade_no).eq("payment_type",paymentType);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);
        return paymentInfo;

    }

    /**
     * 根据订单id 和支付类型获取支付详情
     * @param orderId
     * @param paymentType
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }

}
