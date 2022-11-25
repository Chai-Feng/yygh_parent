package com.alex.yygh.serviceorder.service.impl;

import com.alex.yygh.common.utils.ProgressBarUtil;
import com.alex.yygh.enums.PaymentTypeEnum;
import com.alex.yygh.enums.RefundStatusEnum;
import com.alex.yygh.hospclient.hosp.HospitalFeignClient;
import com.alex.yygh.model.order.OrderInfo;
import com.alex.yygh.model.order.PaymentInfo;
import com.alex.yygh.model.order.RefundInfo;
import com.alex.yygh.serviceorder.service.OrderInfoService;
import com.alex.yygh.serviceorder.service.PaymentInfoService;
import com.alex.yygh.serviceorder.service.RefundInfoService;
import com.alex.yygh.serviceorder.service.WeixinService;
import com.alex.yygh.serviceorder.utils.ConstantPropertiesUtils;
import com.alex.yygh.serviceorder.utils.HttpClient;
import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-25-13:56
 */

@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private PaymentInfoService paymentInfoService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RefundInfoService refundInfoService;

    @Override
    public Map createNative(Long orderId) {
        try {
            Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());
            if (null != payMap) return payMap;
            //根据id获取订单信息
            OrderInfo order = orderInfoService.getById(orderId);
            // 保存交易记录
            paymentInfoService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());
            //1、设置参数
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String DateofReserve = new DateTime(order.getReserveDate()).toString("yyyy年MM月dd日");
            System.out.println("就诊日期 格式转换 " + DateofReserve + " 原始值为 " + order.getReserveDate());
            String dayofWeek = hospitalFeignClient.getDayofWeek(new DateTime(order.getReserveDate()));
            System.out.println("就诊日期是周几？？ " + dayofWeek);


            String body = dayofWeek + " " + DateofReserve + "就诊" + order.getDepname();
            System.out.println("body =" + body);
            paramMap.put("body", body);
            paramMap.put("out_trade_no", order.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");
            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //client设置参数
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", order.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));
            if (null != resultMap.get("result_code")) {
                //微信支付二维码2小时过期，可采取2小时未支付取消订单
                redisTemplate.opsForValue().set(orderId.toString(), map, 1000, TimeUnit.MINUTES);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * 根据订单id 取微信第三方查询支付状态
     *
     * @param orderId
     * @param paymentType
     * @return
     */
    @Override
    public Map queryPayStatus(Long orderId, String paymentType) {

        try {
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            //1、封装参数
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //2、设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据，转成Map
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、返回
            //  System.out.println("queryPayStatus --扫码付款后，微信回调 "+resultMap);
            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }


    //微信退款


    @Override
    public Boolean refund(Long orderId) {

        //1、根据orderId 获取 paymentInfo

        PaymentInfo paymentInfoQuery = paymentInfoService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
        try {
            //2、界面点击退款，后台生成退款记录
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfoQuery);
            if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
                return true; //检查当前退款记录的退款状态
            }

            //给微信组包，发送给地址到https://api.mch.weixin.qq.com/secapi/pay/refund

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id", paymentInfoQuery.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no", paymentInfoQuery.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no", "tk" + paymentInfoQuery.getOutTradeNo()); //商户退款单号

            paramMap.put("total_fee", "1");
            paramMap.put("refund_fee", "1");

            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);

            this.xmlDownload(paramXml, "paramxml");

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            httpClient.setXmlParam(paramXml);
            httpClient.setHttps(true);
            ;
            httpClient.setCert(true);
            httpClient.setCertPassword(ConstantPropertiesUtils.PARTNER);
            httpClient.post();
            //3、返回第三方的数据
            //获取微信的返回信息，退款成功 执行退号操作，退款失败，返回false
            String backxml = httpClient.getContent();
            System.out.println("将微信退款返回数据保存至文件中。。。");

            this.xmlDownload(backxml, "backxml");

            Map<String, String> result = WXPayUtil.xmlToMap(backxml);
            if (null != result && WXPayConstants.SUCCESS.equalsIgnoreCase(result.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(result.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(result));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        } catch (Exception e) {

            e.printStackTrace();
        }
        return false;


    }


    //查看发给微信的内容
    private void xmlDownload(String xml, String fileName) {

        FileOutputStream fos = null;
        try {

            String path = "D:\\file\\xml\\" + fileName + "/.xml";
            File file = new File(path);
            byte[] buff = new byte[512];
            buff = xml.getBytes();
            int length = buff.length;
            fos = new FileOutputStream(file);
            fos.write(buff, 0, length);
            ProgressBarUtil.progressBar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                fos.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
