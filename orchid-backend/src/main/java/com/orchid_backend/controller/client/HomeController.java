package com.orchid_backend.controller.client;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HomeController {
    @Value("${spring.datasource.url}")
    private String testVar;

    @GetMapping("/")
    public String getMethodName() {
        return testVar;
    }

}
