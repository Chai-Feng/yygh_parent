package com.alex.yygh.serviceorder.controller;

import com.alex.yygh.common.result.Result;
import com.alex.yygh.common.utils.AuthContextHolder;
import com.alex.yygh.enums.OrderStatusEnum;
import com.alex.yygh.model.order.OrderInfo;
import com.alex.yygh.serviceorder.service.OrderInfoService;
import com.alex.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-23-18:56
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("api/order/orderInfo")
public class OrderApiController {

    @Autowired
    private OrderInfoService orderInfoService;

    @ApiOperation(value = "创建订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result submitOrder(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable String scheduleId,
            @ApiParam(name = "patientId", value = "就诊人id", required = true)
            @PathVariable Long patientId) {
        return Result.ok(orderInfoService.saveOrder(scheduleId, patientId));
    }

    //订单列表（条件查询带分页）
    @GetMapping("auth/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       OrderQueryVo orderQueryVo, HttpServletRequest request) {
        //设置当前用户id
        //从token中获取用户ID
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));
        Page<OrderInfo> pageParam = new Page<>(page,limit);
        IPage<OrderInfo> pageModel =
                orderInfoService.selectPage(pageParam,orderQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("auth/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }



    @ApiOperation(value = "根据订单Id获取订单详情")
    @GetMapping("auth/getOrders/{orderId}")
    public Result getOrders(@PathVariable Long orderId) {
        System.out.println("根据订单Id获取订单详情 orderId ="+orderId);
        OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
        return Result.ok(orderInfo);
    }


}
