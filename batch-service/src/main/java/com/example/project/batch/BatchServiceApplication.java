package com.example.project.batch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class BatchServiceApplication {
    public static void main(String[] args) {
        try (var context = SpringApplication.run(BatchServiceApplication.class, args)) { /* One-shot process. */ }
    }
}
