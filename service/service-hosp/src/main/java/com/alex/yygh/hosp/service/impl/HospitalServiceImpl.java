package com.alex.yygh.hosp.service.impl;

import com.alex.yygh.enums.DictEnum;
import com.alex.yygh.hosp.repository.HospitalRepository;
import com.alex.yygh.hosp.service.HospitalService;
import com.alex.yygh.model.hosp.BookingRule;
import com.alex.yygh.model.hosp.Hospital;
import com.alex.yygh.servicecmnclient.dict.DictFeignClient;
import com.alex.yygh.vo.hosp.HospitalQueryVo;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-14-13:19
 */

@Service
@Slf4j
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public void save(Map<String, Object> paramMap) {

        //1、把map转换为String
        String maptoString = JSONObject.toJSONString(paramMap);
        log.info(maptoString);

        //json字符串转换成hospital对象
        Hospital hospital = JSONObject.parseObject(maptoString, Hospital.class);
      //  System.out.println(hospital.toString()+"====");

        //判断是否存在数据
        Hospital target = hospitalRepository.getHospitalByHoscode(hospital.getHoscode());
        if(null!=target){
            //存在，则修改

            hospital.setStatus(target.getStatus());
            hospital.setCreateTime(target.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setId(target.getId());
            hospital.setIsDeleted(0);
            System.out.println("医院id ="+hospital.getId());
            hospitalRepository.save(hospital);

        }else {
            //不存在则添加
            //0：未上线 1：已上线
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);

            hospitalRepository.save(hospital);
        }

    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

        //0为首页
        PageRequest pageable = PageRequest.of(page - 1, limit, sort);

        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        //创建匹配器
        //串匹配方式：模糊查询  忽略大小写
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        //创建实例
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);

        pages.getContent().stream().forEach(hospitalitem->{
            this.packHospital(hospitalitem);
        });

        return pages;

    }

    /**
     * 封装数据 调用远程接口，根据数据字典 处理映射
     * @param hospital
     * @return
     */
    private Hospital packHospital(Hospital hospital) {
        String hostypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(),hospital.getHostype());
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());

        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());
        return hospital;
    }

    //显示详情
    @Override
    public Map<String, Object> show(String id) {

        HashMap<String, Object> result = new HashMap<>();

        //此处调用远程接口(cmn) 对地区等编号进行映射
        Hospital hospital = this.packHospital(this.getById(id));
        result.put("hospital",hospital);

        result.put("bookingRule",hospital.getBookingRule());

        //不需要重复返回
        hospital.setBookingRule(null);
        System.out.println("show in HospitalService"+result);
        return result;

    }
    //更新医院上线状态
    @Override
    public void updateStatus(String id) {
        Hospital hospital = this.getById(id);
        Integer status = hospital.getStatus();
        if(status==0) {
            hospital.setStatus(1);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
            System.out.println("状态更新为 = "+hospital.getStatus());
        }else{
            hospital.setStatus(0);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
            System.out.println("状态更新为 = "+hospital.getStatus());
        }
    }



    private Hospital getById(String id) {
        Optional<Hospital> byId = hospitalRepository.findById(id);
        Hospital hospital = byId.get();
        return hospital;
    }


    //根据医院名，模糊查询
    @Override
    public List<Hospital> findByHosname(String hosname) {
        return hospitalRepository.findHospitalByHosnameLike(hosname);
    }



    //根据医院的hoscode 获取该医院的详细信息

    public Map<String,Object> item(String hoscode){
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        HashMap<String, Object> result = new HashMap<>();

        BookingRule bookingRule = hospital.getBookingRule();
        Hospital hospital1 = this.packHospital(hospital);

        //hospital1 里额外封装了两个数据 (hostypeString, fullAddress)
        hospital1.setBookingRule(null);
        result.put("hospital",hospital1);
        result.put("bookingRule",bookingRule);

        return result;
    }

}
