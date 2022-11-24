package com.alex.yygh.serviceorder.service;

import com.alex.yygh.model.hosp.Schedule;
import com.alex.yygh.model.order.OrderInfo;
import com.alex.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-23-18:39
 */
public interface OrderInfoService extends IService<OrderInfo> {

    //保存订单
    Long saveOrder(String scheduleId,Long patientId);

    /**
     * 分页列表
     */
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);


    /**
     * api获取订单详情
     */
    OrderInfo getOrderInfo(Long id); //BigInt

    /**
     * admin 获取订单详情
     */
    Map<String ,Object> show(Long orderId);

}