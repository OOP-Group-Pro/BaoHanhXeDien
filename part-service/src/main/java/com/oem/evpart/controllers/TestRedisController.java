package com.oem.evpart.controllers;

import com.oem.evpart.services.TestRedisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRedisController {

    private final TestRedisService testRedisService;

    public TestRedisController(TestRedisService testRedisService) {
        this.testRedisService = testRedisService;
    }

    @GetMapping("/test-cache")
    public String testCache(@RequestParam String input) {
        return testRedisService.testCache(input);
    }
}
