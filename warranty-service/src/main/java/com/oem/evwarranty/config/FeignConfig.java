package com.oem.evwarranty.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.oem.evvehicle.client.warranty")
public class FeignConfig {
}
