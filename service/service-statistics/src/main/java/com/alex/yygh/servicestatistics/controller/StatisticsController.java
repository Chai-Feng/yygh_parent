package com.alex.yygh.servicestatistics.controller;

import com.alex.yygh.common.result.Result;
import com.alex.yygh.orderclient.order.OrderFeignClient;
import com.alex.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-26-16:54
 */
@Api(tags = "统计管理接口")
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @ApiOperation(value = "获取订单统计数据")
    @GetMapping("getCountMap")
    public Result getCountMap(@ApiParam(name = "orderCountQueryVo", value = "查询对象", required = false) OrderCountQueryVo orderCountQueryVo) {
        System.out.println("获取订单统计数据 --controller"+orderCountQueryVo);
        return Result.ok(orderFeignClient.getCountMap(orderCountQueryVo));

    }
}