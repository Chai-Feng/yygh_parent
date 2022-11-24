package com.alex.yygh.hosp.service.impl;

import com.alex.yygh.hosp.repository.DepartmentRepository;
import com.alex.yygh.hosp.service.DepartmentService;
import com.alex.yygh.model.hosp.Department;
import com.alex.yygh.vo.hosp.DepartmentQueryVo;
import com.alex.yygh.vo.hosp.DepartmentVo;
import com.alibaba.fastjson.JSONObject;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-15-15:13
 */

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;


    /*{
    "hoscode": "1000_0",
    "depcode": "200050923",
    "depname": "门诊部核酸检测门诊(东院)",
    "intro": "门诊部核酸检测门诊(东院)",
    "bigcode": "44f162029abb45f9ff0a5f743da0650d",
    "bigname": "体检科"
    }
*/
    @Override
    public void save(Map<String, Object> paramMap) {

        //1、判断科室是否存在 把Map转换成Department 对象
        String paramString = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(paramString, Department.class);

        Department targetDepart = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());

        //在mongodb里找到这个department，执行修改，否则执行新增
        if (null != targetDepart) {
            //copy不为null的值，该方法为自定义方法

            department.setCreateTime(targetDepart.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            department.setId(targetDepart.getId());

            departmentRepository.save(department);
        } else {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0); //新加的默认为1  1表示删除 0未删除
            departmentRepository.save(department);
        }

    }


    /**
     * 查询科室信息
     */

    public Page<Department> selectPage(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo) {

        //按创建事件反序排列，
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //0为第一页

        PageRequest pageable = PageRequest.of(page - 1, limit, sort);

        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);


        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true); //构建对象

        //创建实例
        Example<Department> example = Example.of(department, matcher);
        Page<Department> pages = departmentRepository.findAll(example, pageable);

        return pages;

    }


    //删除科室
    public void remove(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (null != department) {
            //departmentRepository.delete(department);

            departmentRepository.deleteById(department.getId());

        }
    }

    @Override
    public List<DepartmentVo> findDepartmentTree(String hoscode) {

        //用到了条件查询  //包含 List<DepartmentVo> children
        ArrayList<DepartmentVo> result = new ArrayList<>();

        //根据医院编号，查询所有科室信息
        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        Example<Department> example = Example.of(departmentQuery);

        //所有科室的列表
        List<Department> departmentList = departmentRepository.findAll(example);

        //根据大科室的编号 bigcode 分组，获取每个大科室里面下级 子科室
        Map<String, List<Department>> deparmentMap =
                departmentList.stream().collect(Collectors.groupingBy(Department::getBigname));

        for (Map.Entry<String, List<Department>> entry : deparmentMap.entrySet()) {
            //大科室编号
            String bigCode = entry.getKey();
            //大科室编号对应的全局数据
            List<Department> departmentList1 = entry.getValue();

            //封装大科室
            DepartmentVo departmentVo1 = new DepartmentVo();
            departmentVo1.setDepcode(bigCode);
            departmentVo1.setDepname(departmentList1.get(0).getBigname());


            //封装小科室
            ArrayList<DepartmentVo> children = new ArrayList<>();
            for (Department department : departmentList1) {
                DepartmentVo departmentVo2 = new DepartmentVo();
                departmentVo2.setDepcode(department.getDepcode());
                departmentVo2.setDepname(department.getDepname());
                children.add(departmentVo2);
            }

            //把小科室的list 放到大科室list内
            departmentVo1.setChildren(children);

            //放到最终的result中
            result.add(departmentVo1);
        }

        System.out.println("查询部门树 "+result);
        return result;

    }

    //根据hoscode depcode 获取科室名称
    @Override
    public String getBydepcode(String hoscode,String depcode) {
        String depname = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode).getDepname();
        return depname;
    }

    @Override
    public Department getDeptByhoscodeAndDepcode(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        return department;
    }


}
