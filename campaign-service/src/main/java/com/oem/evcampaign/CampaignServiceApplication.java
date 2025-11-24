package com.oem.evcampaign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.oem.evvehicle.client")
@SpringBootApplication
public class CampaignServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampaignServiceApplication.class, args);
    }
}
