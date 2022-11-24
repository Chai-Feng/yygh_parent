package com.alex.yygh.rabbitutil.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-24-13:53
 */

//消息发送方
@Service
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public boolean sendMessage(String exchange,String routingKey,Object message){
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
        return  true;
    }
}
