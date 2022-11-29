package com.alex.yygh.serviceorder.mapper;

import com.alex.yygh.model.order.OrderInfo;
import com.alex.yygh.model.user.UserInfo;
import com.alex.yygh.vo.order.OrderCountQueryVo;
import com.alex.yygh.vo.order.OrderCountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-23-18:38
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     *
     */
    List<OrderCountVo> selectOrderCount(@Param("vo")OrderCountQueryVo  orderCountQueryVo);
}
