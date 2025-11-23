package com.oem.evpart.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TestRedisService {

    @Cacheable("test_key")
    public String testCache(String input) {
        System.out.println("Cache MISS for " + input);
        return input.toUpperCase();
    }
}
