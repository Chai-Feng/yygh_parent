package com.alex.yygh.hosp.service.impl;

import com.alex.yygh.common.exception.YyghException;
import com.alex.yygh.common.result.ResultCodeEnum;
import com.alex.yygh.hosp.repository.ScheduleRepository;
import com.alex.yygh.hosp.service.DepartmentService;
import com.alex.yygh.hosp.service.HospitalService;
import com.alex.yygh.hosp.service.ScheduleService;
import com.alex.yygh.model.hosp.BookingRule;
import com.alex.yygh.model.hosp.Department;
import com.alex.yygh.model.hosp.Hospital;
import com.alex.yygh.model.hosp.Schedule;
import com.alex.yygh.vo.hosp.BookingScheduleRuleVo;
import com.alex.yygh.vo.hosp.ScheduleOrderVo;
import com.alex.yygh.vo.hosp.ScheduleQueryVo;
import com.alibaba.fastjson.JSONObject;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-15-20:19
 */

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    //分页查询使用
    @Autowired
    private MongoTemplate mongoTemplate;

    //获取医院名称使用
    @Autowired
    private HospitalService hospitalService;

    //获取科室名称
    @Autowired
    private DepartmentService departmentService;

    @Override
    public void save(Map<String, Object> paramMap) {

        //传入的
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Schedule.class);
        System.out.println("上传schedule 从request中获得的schecule == " + schedule.toString());
        //本地mongo里的
        Schedule targetSchedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        //System.out.println("mongodb中已存在的schedule == "+targetSchedule.toString());
        if (null != targetSchedule) {
            System.out.println("mongodb中已存在的schedule 的ID = " + targetSchedule.getId());
            String id = targetSchedule.getId();
            Date createTime = targetSchedule.getCreateTime();
            BeanUtils.copyProperties(schedule, targetSchedule, Schedule.class);
            targetSchedule.setId(id);
            targetSchedule.setCreateTime(createTime);
            targetSchedule.setUpdateTime(new Date());
            targetSchedule.setIsDeleted(0);

            System.out.println("mongodb中已存在的schedule 的ID = " + targetSchedule.getId());
            scheduleRepository.save(targetSchedule);
        } else {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);

        }
    }

    @Override
    public Page<Schedule> selectPage(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //0为第一页
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Schedule> example = Example.of(schedule, matcher);
        Page<Schedule> pages = scheduleRepository.findAll(example, pageable);
        return pages;


    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (null != schedule) {
            System.out.println("删除排班 获取id" + schedule.getId());
            scheduleRepository.deleteById(schedule.getId());
        }

    }

    @Override
    public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {

        //scheduleRepository findAll 分页条件查询的 PageRequest.of(Integer page,Integer limit ..) 此处参数是long，不适合
        //此处我们使用MongoTemplate

        //1 根据医院编号 和 科室编号 查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //2 根据工作日workDate期进行分组
        //聚合分组
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria), //匹配条件
                Aggregation.group("workDate").first("workDate").as("workDate")//按照workDate分组
                        //统计号源数量
                        .count().as("docCount").sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                //4 实现分页
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit));

        //调用方法，最终执行
        AggregationResults<BookingScheduleRuleVo> aggregationResults = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregationResults.getMappedResults();

        //分组查询的总记录数
        Aggregation totalAgg = Aggregation.newAggregation(Aggregation.match(criteria), Aggregation.group("workDate"));
        AggregationResults<BookingScheduleRuleVo> totalAggResults =
                mongoTemplate.aggregate(totalAgg,
                        Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResults.getMappedResults().size();

        //把日期对应星期获取
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //设置最终数据，进行返回
        HashMap<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList", bookingScheduleRuleVoList);
        result.put("total", total);

        //获取医院名称
        String hosName = hospitalService.getByHoscode(hoscode).getHosname();

        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hosName);
        result.put("baseMap", baseMap);

        return result;

    }

    /**
     * 根据排班日期获取排班详情
     *
     * @param hoscode
     * @param depcode
     * @param workDate
     * @return
     */
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {

        //把String 转成 Date
        Date worDate1 = new DateTime(workDate).toDate();
        List<Schedule> scheduleList = scheduleRepository.getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, worDate1);

        scheduleList.forEach(item -> {
            this.packageSchedule(item);
        });
        return scheduleList;

    }


    //对查询出来的排班内容进行封装
    private void packageSchedule(Schedule item) {
        //医院名
        //科室名称
        item.getParam().put("hosname", hospitalService.getByHoscode(item.getHoscode()).getHosname());
        item.getParam().put("depname", departmentService.getBydepcode(item.getHoscode(), item.getDepcode()));

        item.getParam().put("dayOfWeek", getDayOfWeek(new DateTime(item.getWorkDate())));
    }


    /**
     * 根据日期获取周几数据
     * 测试远程调用
     *
     * @param dateTime
     * @return
     */
    public String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }


    /**
     * Api端 获取排班可预约日期数据  MonggoDb 分页查询
     *
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public Map<String, Object> getBookingScheduleRule(int page, int limit, String hoscode, String depcode) {

        HashMap<String, Object> result = new HashMap<>();

        //获取预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (null == hospital) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //获取可预约日期分页数据(从当前天数起 cycle天的内容)
        IPage<Date> iPage = this.getListByDate(page, limit, bookingRule);
        //当前页可预约日期
        List<Date> dateList = iPage.getRecords();

        //查出可预约日期内 当前医院，当前科室的排班信息
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")

        );
        AggregationResults<BookingScheduleRuleVo> aggregateResult = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleRuleVoList = aggregateResult.getMappedResults();
        System.out.println("排班分组之后，转换为list --" + scheduleRuleVoList);
        //获取科室剩余预约数

        //合并数据 将统计数据ScheduleVo根据“安排日期”合并到BookingRuleVo
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(scheduleRuleVoList)) {
            scheduleVoMap = scheduleRuleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
            System.out.println("合并数据 将统计数据ScheduleVo根据“安排日期”合并到scheduleVoMap " + scheduleVoMap);
        }
        //获取可预约排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0, len = dateList.size(); i < len; i++) {
            Date date = dateList.get(i);

            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if (null == bookingScheduleRuleVo) { // 说明当天没有排班医生
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if (i == len - 1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if (i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        System.out.println("排班最终封装结果 --" + bookingScheduleRuleVoList);

        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getByHoscode(hoscode).getHosname());
        //科室
        Department department = departmentService.getDeptByhoscodeAndDepcode(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;


    }


    private IPage<Date> getListByDate(int page, int limit, BookingRule bookingRule) {
        //放号时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        System.out.println("getListByDate -- 放号时间,从bookingrule 获取 " + releaseTime);

        //预约周期
        Integer cycle = bookingRule.getCycle();

        //如果当天放号时间已过，则预约周期后一天为即将放号时间，周期加一
        //解释：放号意思停止挂号，当前时间比放号时间早，说明还没停止挂号，还能继续挂号，当前时间比放号时间晚，即当天已经不可挂号
        if (releaseTime.isBeforeNow()) cycle += 1;
        //可预约所有日期，最后一天显示即将放号倒计时
        List<Date> dates = new ArrayList<>();
        //计算当前预约日期
        for (int i = 0; i < cycle; i++) {
            DateTime curDateTime = new DateTime().plusDays(i); //当前天+i天
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dates.add(new DateTime(dateString).toDate());
            System.out.println("当前可预约日期(预约周期为" + cycle + ") " + dateString);
        }
        //日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        List<Date> pageDateList = new ArrayList<>();
        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;

        //如果可显示的数据小于limit 直接显示
        if (end > dates.size()) end = dates.size();
        for (int i = start; i < end; i++) {
            pageDateList.add(dates.get(i));
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, 7, dates.size());
        iPage.setRecords(pageDateList);
        return iPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */

    private DateTime getDateTime(Date date, String timeString) {

        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);

        return dateTime;

    }


    @Override
    public Schedule getById(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();
        this.packageSchedule(schedule); //源对象操作，无返回值
        return schedule;
    }

    //根据排班表id 获取排班详情(订单使用)
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        //查询当前排班id的详细信息，再把值传递给ScheduleOrderVo 打包好后，发给订单处理接口

        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        if (schedule == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //获取预约规则
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if (null == hospital) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }

        BookingRule bookingRule = hospital.getBookingRule();
        if (null == bookingRule) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        String hoscode = hospital.getHoscode();
        String hosname = hospital.getHosname();
        Integer cycle = bookingRule.getCycle(); //预约周期

        String depcode = schedule.getDepcode(); //科室编号
        String deptname = departmentService.getBydepcode(hoscode, depcode);
        String hosScheduleId = schedule.getHosScheduleId();
        Integer availableNumber = schedule.getAvailableNumber();
        String title = schedule.getTitle();
        Integer workTime = schedule.getWorkTime();
        Date workDate = schedule.getWorkDate();
        BigDecimal amount = schedule.getAmount();


        scheduleOrderVo.setHoscode(hoscode);
        scheduleOrderVo.setHosScheduleId(hosScheduleId);
        scheduleOrderVo.setAmount(amount);
        scheduleOrderVo.setDepcode(depcode);
        scheduleOrderVo.setDepname(deptname);
        scheduleOrderVo.setHosname(hosname);
        scheduleOrderVo.setAvailableNumber(availableNumber);
        scheduleOrderVo.setReserveDate(workDate);
        scheduleOrderVo.setReserveTime(workTime);
        scheduleOrderVo.setTitle(title);

        //相关时间的处理

        String stopTime = bookingRule.getStopTime(); //当天停止挂号时间
        String quitTime = bookingRule.getQuitTime(); //退号时间
        String releaseTime = bookingRule.getReleaseTime(); //放号时间
        //退号截止天数 安排日期 -当前日期
        //-1 就诊前一天 0 当天（）
        Integer quitDay = bookingRule.getQuitDay();
        //退号截止日期(年月日，时分秒)
        DateTime quitDateTime = this.getDateTime(new DateTime(workDate).plusDays(quitDay).toDate(), quitTime);

        scheduleOrderVo.setQuitTime(quitDateTime.toDate());

        //预约开始时间（挂号开始时间）//当天几点开始放号
        DateTime startTime = this.getDateTime(new Date(), releaseTime);
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间（挂号结束时间）
        DateTime endTime = this.getDateTime(new DateTime().plusDays(cycle).toDate(), stopTime);
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime currentStopTime = this.getDateTime(new Date(), stopTime);
        scheduleOrderVo.setStopTime(currentStopTime.toDate());
        return scheduleOrderVo;

    }


    //修改排班

    @Override
    public void update(Schedule schedule) {
        //更新时间
        schedule.setUpdateTime(new Date());
        //主键一致就是更新
        scheduleRepository.save(schedule);

    }
}
