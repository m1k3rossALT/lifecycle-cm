package main.java.com.ecm.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Resource Manager application entry point.
 *
 * <p>Owns: binary content storage and retrieval in MinIO, SHA-256 checksum
 * computation and verification, and the content_object metadata table in
 * its dedicated PostgreSQL database.</p>
 *
 * <p>Not responsible for: any business logic, document lifecycle, access
 * control, or retention policy. This service is intentionally thin. It does
 * what the Library Service tells it to do with content.</p>
 *
 * <p>On startup, verifies that the configured MinIO bucket exists and is
 * accessible. The service will not start if MinIO is unreachable — this is
 * intentional; a Resource Manager that cannot write content is useless.</p>
 */
@SpringBootApplication
public class ResourceManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceManagerApplication.class, args);
    }
}
