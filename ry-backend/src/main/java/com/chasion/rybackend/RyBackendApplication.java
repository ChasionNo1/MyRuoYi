package com.chasion.rybackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.chasion.rybackend.mappers")
public class RyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RyBackendApplication.class, args);
    }

}
