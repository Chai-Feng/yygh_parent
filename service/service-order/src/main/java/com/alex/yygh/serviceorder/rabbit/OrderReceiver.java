package com.alex.yygh.serviceorder.rabbit;

import com.alex.yygh.rabbitutil.constant.MqConst;
import com.alex.yygh.serviceorder.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-26-13:52
 */
@Component
public class OrderReceiver {

    @Autowired
    private OrderInfoService orderInfoService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_8,durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_8}
    ))
    public void patientTipsTask(Message message, Channel channel) throws IOException {
        //监听定时任务 再组包发给阿里云短信服务监听
        orderInfoService.patientTips();
    }

}
