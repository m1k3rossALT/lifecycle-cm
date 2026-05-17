package main.java.com.ecm.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Library Service application entry point.
 *
 * <p>Owns: document metadata, lifecycle state machine, access control enforcement,
 * workflow task management, retention engine, PHI audit logging, and all REST
 * endpoints consumed by the two frontend applications.</p>
 *
 * <p>Not responsible for: binary content storage — all content is delegated to
 * ecm-resource-manager via ResourceManagerClient.</p>
 *
 * <p>{@code @EnableScheduling} is required for the RetentionEngine nightly job
 * and the WorkflowSlaScanner 15-minute job added in Phase 3 and Phase 4.</p>
 */
@SpringBootApplication
@EnableScheduling
public class LibraryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryServiceApplication.class, args);
    }
}
