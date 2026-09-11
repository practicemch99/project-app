package com.example.project.migration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class DatabaseMigrationApplication {
    public static void main(String[] args) {
        try (var context = SpringApplication.run(DatabaseMigrationApplication.class, args)) { /* One-shot process. */ }
    }
}
