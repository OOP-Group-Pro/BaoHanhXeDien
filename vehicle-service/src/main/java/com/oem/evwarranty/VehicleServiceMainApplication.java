package com.oem.evwarranty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// Thêm (exclude = ...) vào annotation
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class VehicleServiceMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleServiceMainApplication.class, args);
    }
}