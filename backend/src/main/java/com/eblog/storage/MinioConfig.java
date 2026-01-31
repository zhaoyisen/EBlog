package com.eblog.storage;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

  @Bean
  @ConditionalOnExpression("!'${MINIO_ENDPOINT:}'.isEmpty() && !'${MINIO_ACCESS_KEY:}'.isEmpty() && !'${MINIO_SECRET_KEY:}'.isEmpty()")
  MinioClient minioClient(
      @Value("${app.minio.endpoint}") String endpoint,
      @Value("${app.minio.access-key}") String accessKey,
      @Value("${app.minio.secret-key}") String secretKey) {
    return MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build();
  }
}
