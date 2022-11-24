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
import com.alex.yygh.serviceuserclient.user.PatientFeignClient;
import com.alex.yygh.vo.hosp.ScheduleOrderVo;
import com.alex.yygh.vo.msm.MsmVo;
import com.alex.yygh.vo.order.OrderMqVo;
import com.alex.yygh.vo.order.OrderQueryVo;
import com.alex.yygh.vo.order.SignInfoVo;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


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
        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow() ||
            new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()){
            //当前时间不可以预约
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }
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
}
