package com.oem.evvehicle.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.oem.evvehicle.client.vehicle")
public class FeignConfig {
}
