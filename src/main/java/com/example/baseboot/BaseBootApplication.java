package com.example.baseboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动引导类
 * 
 * 技术栈：JDK 17 + Spring Boot 3 + Redisson + MyBatis + MySQL
 */
@SpringBootApplication
@MapperScan("com.example.baseboot.module.**.mapper")
public class BaseBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaseBootApplication.class, args);
    }
}
