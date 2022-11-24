package com.alex.yygh.serviceuser.service.impl;

import com.alex.yygh.enums.DictEnum;
import com.alex.yygh.model.user.Patient;
import com.alex.yygh.servicecmnclient.dict.DictFeignClient;
import com.alex.yygh.serviceuser.mapper.PatientMapper;
import com.alex.yygh.serviceuser.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-21-18:13
 */

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {


    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public List<Patient> findAllUserById(Long userId) {
        //根据userId 查询所有就诊人信息
        QueryWrapper<Patient> wrapper = new QueryWrapper<Patient>().eq("user_id", userId);

        List<Patient> patients = baseMapper.selectList(wrapper);

        patients.stream().forEach(item->{
        this.packPatient(item);
        });

        return patients;

    }

    @Override
    public Patient getPatientById(Long id) {

        Patient patient = baseMapper.selectById(id);
        Patient patient1 = this.packPatient(patient);
        System.out.println("通过id 获取就诊信息详情 "+patient1);
        return patient1;
    }

    private Patient packPatient(Patient patient){
        //根据证件类型编码，获取证件类型具体指
        //“证件类型”
        String certificatesTypeString = dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getCertificatesType());
        //联系人证件类型
        String contactsCertificatesTypeString =
                dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());

        //省
        String provinceString = dictFeignClient.getName(patient.getProvinceCode());
        //市
        String cityString = dictFeignClient.getName(patient.getCityCode());
        //区
        String districtString = dictFeignClient.getName(patient.getDistrictCode());


        patient.getParam().put("certificatesTypeString",certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString",contactsCertificatesTypeString);
        patient.getParam().put("provinceString",provinceString);
        patient.getParam().put("cityString",cityString);
        patient.getParam().put("districtString",districtString);

        return  patient;
    }

}
