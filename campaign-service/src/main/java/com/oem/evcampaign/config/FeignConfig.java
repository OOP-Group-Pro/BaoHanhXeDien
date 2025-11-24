package com.oem.evcampaign.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.oem.evvehicle.client")
public class FeignConfig {
}
