package com.alex.yygh.serviceorder.controller;

import com.alex.yygh.common.result.Result;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-24-20:07
 */

@Api(tags = "订单接口")
@RestController
@RequestMapping("/admin/order/orderInfo")

public class OrderController {

    @Autowired
    private OrderInfoService orderInfoService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,
            @ApiParam(name = "orderCountQueryVo", value = "查询对象", required = false) OrderQueryVo orderQueryVo) {
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        System.out.println("查询条件对象 orderQueryVo "+orderQueryVo );
        IPage<OrderInfo> pageModel = orderInfoService.selectPage(pageParam, orderQueryVo);
        return Result.ok(pageModel);
    }


    @ApiOperation(value = "获取订单状态")
    @GetMapping("getStatusList")
    public Result getStatusList() {
        System.out.println("orderController 获取订单状态");
        return Result.ok(OrderStatusEnum.getStatusList());
    }


    @ApiOperation(value = "根据订单Id获取订单详情")
    @GetMapping("show/{orderId}")
    public Result getOrderInfo(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable Long orderId) {
        System.out.println("根据订单Id获取订单详情 orderId ="+orderId+" ---"+orderId.getClass().getName());
        System.out.println("测试！！！！！！！！！！！！！！！！！！");

        Map<String,Object> result= orderInfoService.show(orderId);
        return Result.ok(result);
    }




}
