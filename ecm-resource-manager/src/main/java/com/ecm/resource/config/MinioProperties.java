package main.java.com.ecm.resource;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed binding for the {@code ecm.minio.*} configuration block.
 *
 * <p>All values are sourced from environment variables via application.yml.
 * No defaults for credentials — the application will fail to start if
 * MINIO_ACCESS_KEY or MINIO_SECRET_KEY are absent, which is the correct
 * behaviour for missing required secrets.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ecm.minio")
public class MinioProperties {

    /** MinIO API endpoint URL, e.g. http://minio:9000 */
    private String endpoint;

    private String accessKey;

    private String secretKey;

    /** Name of the bucket where all content objects are stored. */
    private String bucketName;
}
