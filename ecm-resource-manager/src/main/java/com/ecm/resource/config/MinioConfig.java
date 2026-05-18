package main.java.com.ecm.resource.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Produces the {@link MinioClient} bean used by ContentStoreServiceImpl.
 *
 * <p>The client is a singleton — MinioClient is thread-safe and reuses
 * HTTP connections internally. One bean is sufficient for the whole application.</p>
 *
 * <p>Not responsible for: verifying bucket existence on startup — that is
 * done separately by MinioStartupCheck so failures are logged clearly.</p>
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties props) {
        return MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
    }
}
