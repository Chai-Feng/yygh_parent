package com.alex.yygh.hosp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication //(exclude= {DataSourceAutoConfiguration.class}) //在集成mybatis时候，加上，防止找不到数据源  但是加上之后，又会报Unsatisfied dependency
@ComponentScan("com.alex") //设置扫描规则 扫描 swagger
@EnableDiscoveryClient //开启nacos注册

@EnableFeignClients(basePackages = "com.alex") //开启openFeign服务 调用Service-cmn模块
public class ServiceHospApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceHospApplication.class, args);
	}

}
