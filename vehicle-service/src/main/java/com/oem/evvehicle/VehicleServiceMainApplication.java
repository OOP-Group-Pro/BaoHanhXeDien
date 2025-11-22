package com.oem.evvehicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class VehicleServiceMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleServiceMainApplication.class, args);
    }
}