package com.eblog.upload;

import com.eblog.api.common.ErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(MinioClient.class)
public class UploadService {
  private final MinioClient minioClient;
  private final String publicBucket;
  private final String publicBaseUrl;
  private final long maxBytes;

  public UploadService(
      MinioClient minioClient,
      @Value("${app.minio.bucket-public}") String publicBucket,
      @Value("${app.minio.public-base-url}") String publicBaseUrl,
      @Value("${app.upload.max-bytes}") long maxBytes) {
    this.minioClient = minioClient;
    this.publicBucket = publicBucket;
    this.publicBaseUrl = publicBaseUrl;
    this.maxBytes = maxBytes;
  }

  public PresignResult presignPublicUpload(Long userId, String filename, String contentType, Long sizeBytes) {
    if (userId == null || userId <= 0) {
      return PresignResult.error(ErrorCode.UNAUTHORIZED);
    }
    if (isBlank(filename) || isBlank(contentType) || sizeBytes == null || sizeBytes <= 0) {
      return PresignResult.error(ErrorCode.BAD_REQUEST);
    }
    if (sizeBytes > maxBytes) {
      return PresignResult.error(ErrorCode.BAD_REQUEST);
    }
    if (!isAllowedContentType(contentType)) {
      return PresignResult.error(ErrorCode.BAD_REQUEST);
    }

    String safeName = sanitizeFilename(filename);
    String objectKey = buildObjectKey(userId, safeName);
    try {
      String url = minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.PUT)
              .bucket(publicBucket)
              .object(objectKey)
              .expiry(15 * 60)
              .build());

      String base = publicBaseUrl == null ? "" : publicBaseUrl.trim();
      String publicUrl;
      if (!base.isEmpty()) {
        String noSlash = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        publicUrl = noSlash + "/" + publicBucket + "/" + objectKey;
      } else {
        publicUrl = "/" + publicBucket + "/" + objectKey;
      }

      PresignResponse res = new PresignResponse();
      res.bucket = publicBucket;
      res.objectKey = objectKey;
      res.uploadUrl = url;
      res.publicUrl = publicUrl;
      res.contentType = contentType;
      res.maxBytes = maxBytes;
      return PresignResult.success(res);
    } catch (Exception ex) {
      return PresignResult.error(ErrorCode.INTERNAL_ERROR);
    }
  }

  private static boolean isAllowedContentType(String contentType) {
    String ct = contentType.trim().toLowerCase(Locale.ROOT);
    return ct.equals("image/png")
        || ct.equals("image/jpeg")
        || ct.equals("image/gif")
        || ct.equals("image/webp")
        || ct.equals("application/pdf");
  }

  private static String buildObjectKey(Long userId, String safeFilename) {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    String y = String.format("%04d", now.getYear());
    String m = String.format("%02d", now.getMonthValue());
    String id = UUID.randomUUID().toString().replace("-", "");
    return "u/" + userId + "/" + y + "/" + m + "/" + id + "_" + safeFilename;
  }

  private static String sanitizeFilename(String filename) {
    String name = filename.trim();
    name = name.replace("\\", "/");
    int slash = name.lastIndexOf('/');
    if (slash >= 0) {
      name = name.substring(slash + 1);
    }
    name = name.replaceAll("[^A-Za-z0-9._-]", "-");
    name = name.replaceAll("-+", "-");
    if (name.isEmpty()) {
      name = "file";
    }
    if (name.length() > 120) {
      name = name.substring(name.length() - 120);
    }
    return name;
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  public static class PresignResponse {
    public String bucket;
    public String objectKey;
    public String uploadUrl;
    public String publicUrl;
    public String contentType;
    public long maxBytes;
  }

  public static class PresignResult {
    private final ErrorCode error;
    private final PresignResponse data;

    private PresignResult(ErrorCode error, PresignResponse data) {
      this.error = error;
      this.data = data;
    }

    public static PresignResult success(PresignResponse data) {
      return new PresignResult(null, data);
    }

    public static PresignResult error(ErrorCode error) {
      return new PresignResult(error, null);
    }

    public boolean isSuccess() {
      return error == null;
    }

    public ErrorCode getError() {
      return error;
    }

    public PresignResponse getData() {
      return data;
    }
  }
}
