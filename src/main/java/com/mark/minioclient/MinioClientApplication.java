package com.mark.minioclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MinioClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinioClientApplication.class, args);
    }

}
