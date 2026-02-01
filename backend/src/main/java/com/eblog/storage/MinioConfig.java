package com.eblog.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
  private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

  @Bean
  MinioClient minioClient(
      @Value("${app.minio.endpoint}") String endpoint,
      @Value("${app.minio.access-key}") String accessKey,
      @Value("${app.minio.secret-key}") String secretKey,
      @Value("${app.minio.bucket-public}") String publicBucket) {
    MinioClient client = MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build();

    initBucket(client, publicBucket);

    return client;
  }

  private void initBucket(MinioClient client, String bucket) {
    if (bucket == null || bucket.isEmpty()) {
      return;
    }
    try {
      boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
      if (!exists) {
        log.info("Creating MinIO bucket: {}", bucket);
        client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());

        // Set public read policy
        String policy = "{\n" +
            "  \"Version\": \"2012-10-17\",\n" +
            "  \"Statement\": [\n" +
            "    {\n" +
            "      \"Effect\": \"Allow\",\n" +
            "      \"Principal\": {\"AWS\": [\"*\"]},\n" +
            "      \"Action\": [\"s3:GetBucketLocation\", \"s3:ListBucket\"],\n" +
            "      \"Resource\": [\"arn:aws:s3:::" + bucket + "\"]\n" +
            "    },\n" +
            "    {\n" +
            "      \"Effect\": \"Allow\",\n" +
            "      \"Principal\": {\"AWS\": [\"*\"]},\n" +
            "      \"Action\": [\"s3:GetObject\"],\n" +
            "      \"Resource\": [\"arn:aws:s3:::" + bucket + "/*\"]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        client.setBucketPolicy(
            SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
        log.info("MinIO bucket {} initialized with public read policy", bucket);
      } else {
        log.info("MinIO bucket {} already exists", bucket);
      }
    } catch (Exception e) {
      log.error("Failed to initialize MinIO bucket: {}", bucket, e);
    }
  }
}

