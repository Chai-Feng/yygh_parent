package com.alex.yygh.hosp.repository;

import com.alex.yygh.model.hosp.Department;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-15-15:03
 */

@Repository
public interface DepartmentRepository  extends MongoRepository<Department, String> {

    Department getDepartmentByHoscodeAndDepcode(String hoscode,String depcode);


    List<Department> getDepartmentsByHoscode(String hoscode);


}
