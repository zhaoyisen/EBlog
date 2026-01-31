package com.eblog.auth;

import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import com.eblog.user.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/password-reset")
public class PasswordResetController {
  private final PasswordResetService passwordResetService;

  public PasswordResetController(PasswordResetService passwordResetService) {
    this.passwordResetService = passwordResetService;
  }

  @PostMapping("/request")
  public ApiResponse<Object> requestReset(HttpServletRequest request, @RequestBody RequestResetRequest body) {
    if (body == null || body.email == null || body.email.trim().isEmpty()) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    String ip = clientIp(request);
    String ua = request.getHeader("User-Agent");
    passwordResetService.requestReset(body.email, ip, ua);
    return ApiResponse.ok(null);
  }

  @PostMapping("/confirm")
  public ApiResponse<Object> confirm(@RequestBody ConfirmResetRequest body) {
    if (body == null || isBlank(body.email) || isBlank(body.token) || isBlank(body.newPassword)) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    ErrorCode error = passwordResetService.resetPassword(body.email, body.token, body.newPassword);
    if (error != null) {
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(null);
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private static String clientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.trim().isEmpty()) {
      int comma = xff.indexOf(',');
      return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
    }
    return request.getRemoteAddr();
  }

  public static class RequestResetRequest {
    public String email;
  }

  public static class ConfirmResetRequest {
    public String email;
    public String token;
    public String newPassword;
  }
}
