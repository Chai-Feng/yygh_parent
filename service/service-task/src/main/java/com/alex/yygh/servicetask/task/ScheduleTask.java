package com.alex.yygh.servicetask.task;

import com.alex.yygh.rabbitutil.constant.MqConst;
import com.alex.yygh.rabbitutil.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-26-13:37
 */

@Component
@EnableScheduling
public class ScheduleTask {


    @Autowired
    private RabbitService rabbitService;

    /**
     * 每天8点执行提醒
     */
//这写的是每隔30秒巡检一次@Scheduled(cron = "0/30 * * * * ?")
    @Scheduled(cron = "0 * 8 * * ?")
    public void task1(){
        System.out.println("定时任务执行");
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8,"");
    }

}
