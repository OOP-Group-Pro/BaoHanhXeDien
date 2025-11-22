package com.oem.evuser.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.oem.evvehicle.client.user")
public class FeignConfig {
}
