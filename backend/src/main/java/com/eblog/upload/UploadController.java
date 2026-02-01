package com.eblog.upload;

import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {
  private final UploadService uploadService;

  public UploadController(UploadService uploadService) {
    this.uploadService = uploadService;
  }

  @PostMapping("/presign")
  public ApiResponse<UploadService.PresignResponse> presign(@RequestBody PresignRequest body) {
    if (body == null || body.sizeBytes == null) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    Long userId = currentUserId();
    UploadService.PresignResult result = uploadService.presignPublicUpload(
        userId, body.filename, body.contentType, body.sizeBytes);
    if (!result.isSuccess()) {
      ErrorCode error = result.getError();
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(result.getData());
  }

  private static Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) {
      return null;
    }
    try {
      return Long.valueOf(String.valueOf(auth.getPrincipal()));
    } catch (Exception ex) {
      return null;
    }
  }

  public static class PresignRequest {
    public String filename;
    public String contentType;
    public Long sizeBytes;
  }
}
