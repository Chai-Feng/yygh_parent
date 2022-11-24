package com.alex.yygh.hosp.repository;

import com.alex.yygh.model.hosp.Hospital;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-14-13:12
 */

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {

    //根据hoscode查找医院
    //只添加接口，却没有实现，奇葩
    Hospital getHospitalByHoscode(String hoscode);

    List<Hospital> findHospitalByHosnameLike(String hosname);
}
