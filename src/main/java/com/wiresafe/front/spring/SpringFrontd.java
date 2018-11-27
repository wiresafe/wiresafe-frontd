package com.wiresafe.front.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringFrontd {

    public static void main(String[] args) {
        SpringApplication.run(SpringFrontd.class, args);
    }

}
