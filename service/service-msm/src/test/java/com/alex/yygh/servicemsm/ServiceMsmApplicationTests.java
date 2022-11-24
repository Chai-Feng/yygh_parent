package com.alex.yygh.servicemsm;

import com.alex.yygh.servicemsm.service.MsmService;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest

class ServiceMsmApplicationTests {

    @Autowired
    private MsmService msmService;

   @Test
    void contextLoads() {

        boolean send = msmService.send("17849981097", "9527");
        System.out.println(send);
    }

}
