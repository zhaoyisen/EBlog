package com.eblog.auth;

import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import com.eblog.user.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/email-code")
@ConditionalOnBean({EmailCodeMapper.class, UserMapper.class})
public class EmailCodeController {

  private final EmailCodeService emailCodeService;
  private final LoginRateLimiter rateLimiter;

  public EmailCodeController(EmailCodeService emailCodeService, LoginRateLimiter rateLimiter) {
    this.emailCodeService = emailCodeService;
    this.rateLimiter = rateLimiter;
  }

  @PostMapping("/send-register")
  public ApiResponse<Object> sendRegister(HttpServletRequest request, @RequestBody SendCodeRequest body) {
    String key = "email-code:" + request.getRemoteAddr() + ":" + (body.email == null ? "" : body.email);
    if (!rateLimiter.tryConsume(key)) {
      return ApiResponse.fail(ErrorCode.RATE_LIMITED.getCode(), ErrorCode.RATE_LIMITED.getMessage());
    }

    // Always return success to avoid email enumeration.
    if (body.email != null && body.email.contains("@")) {
      try {
        emailCodeService.sendRegisterCode(body.email.trim().toLowerCase());
      } catch (Exception ex) {
        // swallow errors; still return ok (deliverability is best-effort)
      }
    }
    return ApiResponse.ok(null);
  }

  public static class SendCodeRequest {
    public String email;
  }
}
