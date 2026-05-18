package main.java.com.ecm.resource.startup;

import com.ecm.resource.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Verifies that the configured MinIO bucket exists and is accessible on startup.
 *
 * <p>A Resource Manager that cannot reach MinIO or find its bucket is
 * non-functional. Failing fast here surfaces the problem immediately as
 * a startup error rather than allowing the service to start and then fail
 * on the first upload request.</p>
 *
 * <p>Implements ApplicationRunner so it runs after the Spring context is
 * fully initialized — the MinioClient bean is guaranteed to be available.</p>
 *
 * <p>Not responsible for: creating the bucket — that is the minio-init
 * Docker sidecar's job. This check only verifies existence.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MinioStartupCheck implements ApplicationRunner {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public void run(ApplicationArguments args) {
        String bucket = minioProperties.getBucketName();
        log.info("Verifying MinIO connectivity — endpoint={} bucket={}",
                minioProperties.getEndpoint(), bucket);

        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );

            if (!exists) {
                // The minio-init sidecar should have created the bucket before
                // this service starts (enforced by depends_on in docker-compose).
                // If it is missing here, the compose startup order was bypassed.
                throw new IllegalStateException(
                        "MinIO bucket '" + bucket + "' does not exist. " +
                        "Verify that the minio-init container ran successfully.");
            }

            log.info("MinIO connectivity verified — bucket '{}' is accessible", bucket);

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot connect to MinIO at " + minioProperties.getEndpoint() +
                    ". Verify MINIO_ENDPOINT, MINIO_ACCESS_KEY, and MINIO_SECRET_KEY.", e);
        }
    }
}
