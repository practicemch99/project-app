package com.example.project.core.presentation.api;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class SetupController {
    private final JdbcTemplate jdbc;
    public SetupController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @GetMapping("/api/v1/setup")
    public Map<String, String> setup() {
        return Map.of("status", "UP", "database",
            jdbc.queryForObject("SELECT description FROM setup_marker WHERE id = 1", String.class));
    }
}
