package com.alex.yygh.servicecmnclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication

public class ServiceCmnClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCmnClientApplication.class, args);
    }

}
