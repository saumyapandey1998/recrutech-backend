package com.recrutech.recrutechplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.recrutech.recrutechplatform", "com.recrutech.common"})
public class RecrutechPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecrutechPlatformApplication.class, args);
    }

}
