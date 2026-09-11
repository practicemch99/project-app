package com.example.project.batch.job;
import com.example.project.batch.client.core.CoreClient;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
@Component
public class SetupCheckRunner implements CommandLineRunner {
    private final CoreClient core;
    public SetupCheckRunner(CoreClient core) { this.core = core; }
    @Override public void run(String... args) {
        LoggerFactory.getLogger(getClass()).info("Core setup check: {}", core.setup());
    }
}
