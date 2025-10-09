package com.orchid_backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration

public class DotEnvConfig { // Không có tác dụng
    @Value("${ENV_PATH}")
    private String EnvPath;

    @PostConstruct
    public void loadEnv() {
        System.out.println(">>> Working Directory: " + System.getProperty("user.dir"));

        Dotenv dotenv = Dotenv.configure()
                .directory(EnvPath) // Nếu bạn đang ở thư mục orchidshop
                .filename(".env")
                .load();

        System.out.println(">>> Loaded .env entries: " + dotenv.entries().size());
        for (DotenvEntry entry : dotenv.entries()) {
            System.out.println(">>> " + entry.getKey() + " = " + entry.getValue());
            System.setProperty(entry.getKey(), entry.getValue());
        }
    }
}
