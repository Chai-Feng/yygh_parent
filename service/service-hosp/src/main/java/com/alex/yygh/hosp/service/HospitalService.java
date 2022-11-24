package com.alex.yygh.hosp.service;

import com.alex.yygh.hosp.repository.HospitalRepository;
import com.alex.yygh.model.hosp.Hospital;
import com.alex.yygh.vo.hosp.HospitalQueryVo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-14-13:18
 */
public interface HospitalService {



    /**
     * 上传医院信息
     */
    void save(Map<String,Object> paramMap);

    /**
     * 查询医院
     */
    Hospital getByHoscode(String hoscode);


    /**
     * 分页查询
     * @param page 当前页码
     * @param limit 每页记录数
     * @param hospitalQueryVo 查询条件
     * @return
     */
    Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);


    /**
     * 查看医院详情
     */
    Map<String,Object> show(String id);


    /**
     * 更新医院上线状态
     * @param id
     */
    void updateStatus(String id);


    /**
     * 根据医院名称获取医院列表
     */

    List<Hospital> findByHosname(String hosname);

    /**
     * 预约挂号详情
     * @param hoscode
     * @return
     */
    Map<String,Object> item(String hoscode);
}
