package com.alex.yygh.hosp.service.impl;

import com.alex.yygh.common.exception.YyghException;
import com.alex.yygh.common.result.ResultCodeEnum;
import com.alex.yygh.hosp.mapper.HospitalSetMapper;
import com.alex.yygh.hosp.service.HospitalSetService;
import com.alex.yygh.model.hosp.HospitalSet;
import com.alex.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-07-22:45
 */
@Service

public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {

    @Autowired
    private HospitalSetMapper hospitalSetMapper;

    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);

        if(null == hospitalSet) {
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        if(hospitalSet.getStatus().intValue() == 0) {
            throw new YyghException(ResultCodeEnum.HOSPITAL_LOCK);
        }


        return hospitalSet.getSignKey();
    }


    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {

        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);

        if(null == hospitalSet) {
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        String signKey = this.getSignKey(hoscode);

        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setSignKey(signKey);
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        return signInfoVo;
    }
}
