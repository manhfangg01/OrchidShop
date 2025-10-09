package com.orchid_backend.controller.client;

import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HomeController {
    @Value("${spring.jpa.show-sql}")
    private String testVar;

    @GetMapping("/")
    public String getMethodName() {
        return testVar;
    }

    @GetMapping("/testPlainText")
    public void getMethodName(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("=== SYSTEM LOGS ===");
        }
    }

    @GetMapping("/testRequest")
    public String getSession(HttpServletRequest request) throws IOException {
        return request.getSession().getAttributeNames().toString();

    }

}
