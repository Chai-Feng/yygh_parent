package com.alex.yygh.hosp.service;


import com.alex.yygh.model.hosp.Department;
import com.alex.yygh.vo.hosp.DepartmentQueryVo;
import com.alex.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;


import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-15-15:11
 */
public interface DepartmentService {


    /**
     * 上传或修改科室
     */
     void save(Map<String,Object> paramMap);

    /**
     * 查询科室
     *
     */
    Page<Department> selectPage(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo);

    /**
     * 删除科室
     */
    void remove(String hoscode, String depcode);


    //查看科室信息 大小科室以树形展示
    List<DepartmentVo> findDepartmentTree(String hoscode);


    /**
     * 根据depcode hoscode 获取部门名称
     */
    String getBydepcode(String hoscode,String depcode);

    /**
     * 根据depcode hoscode 获取部门名称
     */
    Department getDeptByhoscodeAndDepcode(String hoscode,String depcode);
}
