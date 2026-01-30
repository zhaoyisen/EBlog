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
      @Value("${MINIO_ENDPOINT:}") String endpoint,
      @Value("${MINIO_ACCESS_KEY:}") String accessKey,
      @Value("${MINIO_SECRET_KEY:}") String secretKey) {
    return MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build();
  }
}
