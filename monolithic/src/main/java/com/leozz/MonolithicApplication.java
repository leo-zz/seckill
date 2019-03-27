package com.leozz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Author: leo-zz
 * @Date: 2019/3/12 10:14
 */
@SpringBootApplication
@EnableSwagger2
@MapperScan(basePackages = "com.leozz.dao")
public class MonolithicApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonolithicApplication.class, args);
    }
}
