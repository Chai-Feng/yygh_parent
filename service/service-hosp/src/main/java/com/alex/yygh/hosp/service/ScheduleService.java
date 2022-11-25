package com.alex.yygh.hosp.service;

import com.alex.yygh.model.hosp.Schedule;
import com.alex.yygh.vo.hosp.ScheduleOrderVo;
import com.alex.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-15-20:17
 */
public interface ScheduleService {

    //上传医生排班

    void save(Map<String,Object> paramMap);

    //分页查询医生排班

    Page<Schedule> selectPage(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo);


    /**
     * 删除科室
     * @param hoscode
     * @param hosScheduleId
     */
    void remove(String hoscode, String hosScheduleId);


    //根据医院编号(hoscode) 科室编号(depcode)，查询医生排班信息
    /**
     * 显示内容 职称、号源时间，可预约数，剩余预约数，擅长技能
     */


        Map<String,Object> getScheduleRule(long page,long limit,String hoscode,String depcode);

    /**
     *
     * 根据排班日期获取排班详情
     */
    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);


    /**
     * 获取排班可预约日期数据,api端调用
     * @param: hoscode
     * @param:depcode
     * @param:page,limit
     */
    Map<String,Object> getBookingScheduleRule(int page, int limit, String hoscode, String depcode);


    /**
     * 根据id获取排班
     */
    Schedule getById(String id);


    /**
     * 根据排班id 获取预约下单数据
     */
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);


    /**
     * 修改排班
     */
    void update(Schedule schedule);

    /**
     * 获取周几
     * @param dateTime
     * @return
     */
    String getDayOfWeek(DateTime dateTime);
}
