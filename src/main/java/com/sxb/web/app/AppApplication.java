package com.sxb.web.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.sxb.web")
public class AppApplication {
        
    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}
