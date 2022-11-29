package com.alex.yygh.serviceorder.service.impl;

import com.alex.yygh.common.exception.YyghException;
import com.alex.yygh.common.helper.HttpRequestHelper;
import com.alex.yygh.common.result.ResultCodeEnum;
import com.alex.yygh.enums.OrderStatusEnum;
import com.alex.yygh.hospclient.hosp.HospitalFeignClient;
import com.alex.yygh.model.order.OrderInfo;
import com.alex.yygh.model.user.Patient;
import com.alex.yygh.rabbitutil.constant.MqConst;
import com.alex.yygh.rabbitutil.service.RabbitService;
import com.alex.yygh.serviceorder.mapper.OrderInfoMapper;
import com.alex.yygh.serviceorder.service.OrderInfoService;
import com.alex.yygh.serviceorder.service.WeixinService;
import com.alex.yygh.serviceuserclient.user.PatientFeignClient;
import com.alex.yygh.vo.hosp.ScheduleOrderVo;
import com.alex.yygh.vo.msm.MsmVo;
import com.alex.yygh.vo.order.*;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-23-18:40
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper,OrderInfo> implements OrderInfoService{

    @Autowired
    private HospitalFeignClient hospitalFeignClient;
    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeixinService weixinService;

    //下单
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {

        //通过远程接口，获取订单中需要的内容
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        if(null == scheduleOrderVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
            Patient patient = patientFeignClient.getPatient(patientId);
        if(null==patient){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }


        //当前时间不可以预约(start 挂号开始时间  end 挂号结束时间)
//        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow() ||
//            new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()){
//            //当前时间不可以预约
//            throw new YyghException(ResultCodeEnum.TIME_NO);
//        }
        //获取当前医院的签名
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());
        if(null ==signInfoVo){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //预约次数没了
        if(scheduleOrderVo.getAvailableNumber()<=0){
            throw new YyghException(ResultCodeEnum.NUMBER_NO);
        }
        //封装订单信息
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo);

        String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        this.save(orderInfo);

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",scheduleOrderVo.getHosScheduleId()); //排班id 114
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime",orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        //就诊人
        paramMap.put("name",orderInfo.getPatientName());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());

        //对签名进行加密传输
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        System.out.println("加密后的签名内容为 "+sign);
        paramMap.put("sign",sign);


        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl()+"/order/submitOrder");
        System.out.println("从医院平台返回的数据"+JSONObject.toJSONString(result));
        if(result.getInteger("code")==200){
            //成功返回
            JSONObject data = result.getJSONObject("data"); //返回的是一个json对象

            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = data.getString("hosRecordId");
            //预约序号
            Integer number = data.getInteger("number");;
            //取号时间
            String fetchTime = data.getString("fetchTime");;
            //取号地址
            String fetchAddress = data.getString("fetchAddress");;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);

            this.updateById(orderInfo);

            //获取排班可预约数
            Integer reservedNumber = data.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = data.getInteger("availableNumber");
            //  TODO 发送mq信息更新号源和短信通知
            //更新号源
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setAvailableNumber(availableNumber);
            orderMqVo.setReservedNumber(reservedNumber);

            //短信通知
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(patient.getPhone());
            msmVo.setTemplateCode("SMS_154950909");
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
                put("code","666666");
            }};
            msmVo.setParam(param);

            orderMqVo.setMsmVo(msmVo);
            //发送消息
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);




        }else {
            throw new YyghException(result.getString("message"),ResultCodeEnum.FAIL.getCode());

        }

        return orderInfo.getId();
    }

    //订单管理 订单列表
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {

        //orderQueryVo 获取条件值
        String keyword = orderQueryVo.getKeyword();
        Long patientId = orderQueryVo.getPatientId(); //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        String outTradeNo = orderQueryVo.getOutTradeNo(); //订单编号

        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)) {
            wrapper.like("hosname",keyword);
        }
        if(!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(outTradeNo)){
            wrapper.eq("out_trade_no",outTradeNo);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }
        //调用mapper的方法
        IPage<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        //编号变成对应值封装
        pages.getRecords().stream().forEach(item -> {
            this.packOrderInfo(item);
        });
        return pages;
    }

    /**
     * 获取订单详情
     * @param id
     * @return
     */
    @Override
    public OrderInfo getOrderInfo(Long id) {
        OrderInfo orderInfo = baseMapper.selectById(id);
        return  packOrderInfo(orderInfo);
    }


    private  OrderInfo packOrderInfo(OrderInfo orderInfo){
        orderInfo.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;

    }


    @Override
    public Map<String, Object> show(Long orderId) {
        HashMap<String, Object> result = new HashMap<>();
        OrderInfo orderInfo = this.getOrderInfo(orderId);
        result.put("orderInfo",orderInfo);

        Patient patient = patientFeignClient.getPatient(orderInfo.getPatientId());

        result.put("patient",patient);
        return  result;
    }


    //取消订单
    @Override
    public Boolean cancelOrder(Long orderId) {

        System.out.println("OrderInfoService cancelOrder "+orderId+" "+orderId.getClass().getName());
        OrderInfo orderInfo = this.getById(orderId);
        //当前时间大于退号时间，不能取消预约
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());

//        if(quitTime.isBeforeNow()){
//            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO) ;// 不能取消预约
//        }
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if(null==signInfoVo){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR); //签名异常
        }


        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updateCancelStatus");

        if(result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        } else {
            //退号，订单是否支付
            if(orderInfo.getOrderStatus().intValue()==OrderStatusEnum.PAID.getStatus().intValue()){
                Boolean isRefund = weixinService.refund(orderId);
                if(!isRefund) {
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
            }
            //更改订单状态
           orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus()); //取消预约
            // 发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
            this.updateById(orderInfo);
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(orderInfo.getScheduleId());
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.setTemplateCode("SMS_154950909");
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("code","999999");
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        }
        return true;

    }


    //就医提醒
    @Override
    public void patientTips() {

        //定时任务消息触发此方法 Mq Receiver收到定时任务消息，调用此方法，组包消息体 通知阿里云发短信

        //找到与本日期相同的单号
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<OrderInfo>().eq("reserve_date", new DateTime().toString("yyyy-MM-dd"));

       // OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        List<OrderInfo> list = baseMapper.selectList(queryWrapper);
        if(null==list){
           throw new YyghException("当日无预约",ResultCodeEnum.PARAM_ERROR.getCode());
       }

        //组包信息 phone templateCode param

        for(OrderInfo orderInfo:list){
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.setTemplateCode("SMS_154950909");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+(orderInfo.getReserveTime()==0?"上午":"下午");

                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("code","777777"); //提醒就诊短信内容
            }};
            msmVo.setParam(param);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);
        }
    }

    /**
     * 订单统计
     */
    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> map = new HashMap<>();

        System.out.println("查询条件 "+orderCountQueryVo);
        List<OrderCountVo> orderCountVoList
                = baseMapper.selectOrderCount(orderCountQueryVo);

        System.out.println("Mapperxml 查询结果");
        //日期列表
        List<String> dateList
                =orderCountVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        //统计列表
        List<Integer> countList
                =orderCountVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        map.put("dateList", dateList);
        map.put("countList", countList);
        return map;
    }





}
