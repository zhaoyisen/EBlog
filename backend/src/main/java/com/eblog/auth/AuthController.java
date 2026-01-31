package com.eblog.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import com.eblog.user.UserEntity;
import com.eblog.user.UserMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final RefreshTokenMapper refreshTokenMapper;
  private final LoginRateLimiter loginRateLimiter;
  private final AuthService authService;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private final long refreshTtlSeconds;
  private final boolean cookieSecure;

  public AuthController(
      JwtService jwtService,
      UserMapper userMapper,
      RefreshTokenMapper refreshTokenMapper,
      LoginRateLimiter loginRateLimiter,
      AuthService authService,
      @Value("${app.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
      @Value("${app.cookie.secure}") boolean cookieSecure) {
    this.jwtService = jwtService;
    this.userMapper = userMapper;
    this.refreshTokenMapper = refreshTokenMapper;
    this.loginRateLimiter = loginRateLimiter;
    this.authService = authService;
    this.refreshTtlSeconds = refreshTtlSeconds;
    this.cookieSecure = cookieSecure;
  }

  @GetMapping("/csrf")
  public ApiResponse<CsrfToken> csrf(CsrfToken token) {
    return ApiResponse.ok(token);
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(
      HttpServletRequest request, HttpServletResponse response, @RequestBody LoginRequest body) {
    String ip = clientIp(request);
    String rateKey = ip + ":" + (body.email == null ? "" : body.email.trim().toLowerCase());
    if (!loginRateLimiter.tryConsume(rateKey)) {
      return ApiResponse.fail(ErrorCode.RATE_LIMITED.getCode(), ErrorCode.RATE_LIMITED.getMessage());
    }

    UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getEmail, body.email));
    if (user == null || user.getPasswordHash() == null || !passwordEncoder.matches(body.password, user.getPasswordHash())) {
      return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }

    String accessToken = jwtService.createAccessToken(user.getId().longValue(), user.getRole());

    String refreshToken = TokenGenerator.randomToken();
    String refreshHash = RefreshTokenHasher.sha256Hex(refreshToken);
    RefreshTokenEntity entity = new RefreshTokenEntity();
    entity.setUserId(user.getId());
    entity.setTokenHash(refreshHash);
    entity.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
    entity.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(refreshTtlSeconds));
    refreshTokenMapper.insert(entity);

    setRefreshCookie(response, refreshToken, (int) refreshTtlSeconds);

    LoginResponse res = new LoginResponse();
    res.accessToken = accessToken;
    return ApiResponse.ok(res);
  }

  @PostMapping("/register")
  public ApiResponse<RegisterResponse> register(HttpServletRequest request, @RequestBody RegisterRequest body) {
    if (body == null || isBlank(body.email) || isBlank(body.password)
        || isBlank(body.inviteCode) || isBlank(body.emailCode)) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }

    String ip = clientIp(request);
    AuthService.RegisterResult result = authService.register(
        body.email, body.password, body.inviteCode, body.emailCode, ip);
    if (!result.isSuccess()) {
      ErrorCode error = result.getError();
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }

    RegisterResponse res = new RegisterResponse();
    res.userId = result.getUserId();
    return ApiResponse.ok(res);
  }

  @PostMapping("/refresh")
  public ApiResponse<LoginResponse> refresh(
      HttpServletResponse response, @CookieValue(name = "refresh_token", required = false) String refreshToken) {
    if (refreshToken == null || refreshToken.trim().isEmpty()) {
      return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }

    String oldHash = RefreshTokenHasher.sha256Hex(refreshToken);
    RefreshTokenEntity oldEntity = refreshTokenMapper.selectOne(
        new LambdaQueryWrapper<RefreshTokenEntity>().eq(RefreshTokenEntity::getTokenHash, oldHash));
    if (oldEntity == null || oldEntity.getRevokedAt() != null || oldEntity.getExpiresAt() == null
        || oldEntity.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
      return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }

    UserEntity user = userMapper.selectById(oldEntity.getUserId());
    if (user == null) {
      return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }

    // rotate refresh token
    String newRefreshToken = TokenGenerator.randomToken();
    String newHash = RefreshTokenHasher.sha256Hex(newRefreshToken);
    RefreshTokenEntity newEntity = new RefreshTokenEntity();
    newEntity.setUserId(user.getId());
    newEntity.setTokenHash(newHash);
    newEntity.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
    newEntity.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(refreshTtlSeconds));
    refreshTokenMapper.insert(newEntity);

    oldEntity.setRevokedAt(LocalDateTime.now(ZoneOffset.UTC));
    oldEntity.setReplacedByHash(newHash);
    refreshTokenMapper.updateById(oldEntity);

    setRefreshCookie(response, newRefreshToken, (int) refreshTtlSeconds);

    LoginResponse res = new LoginResponse();
    res.accessToken = jwtService.createAccessToken(user.getId().longValue(), user.getRole());
    return ApiResponse.ok(res);
  }

  @PostMapping("/logout")
  public ApiResponse<Object> logout(HttpServletResponse response, @CookieValue(name = "refresh_token", required = false) String refreshToken) {
    if (refreshToken != null && !refreshToken.trim().isEmpty()) {
      String hash = RefreshTokenHasher.sha256Hex(refreshToken);
      RefreshTokenEntity entity = refreshTokenMapper.selectOne(
          new LambdaQueryWrapper<RefreshTokenEntity>().eq(RefreshTokenEntity::getTokenHash, hash));
      if (entity != null && entity.getRevokedAt() == null) {
        entity.setRevokedAt(LocalDateTime.now(ZoneOffset.UTC));
        refreshTokenMapper.updateById(entity);
      }
    }
    clearRefreshCookie(response);
    return ApiResponse.ok(null);
  }

  private void setRefreshCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
    ResponseCookie cookie = ResponseCookie.from("refresh_token", token)
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Strict")
        .path("/")
        .maxAge(maxAgeSeconds)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  private void clearRefreshCookie(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Strict")
        .path("/")
        .maxAge(0)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  private static String clientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.trim().isEmpty()) {
      int comma = xff.indexOf(',');
      return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
    }
    return request.getRemoteAddr();
  }

  public static class LoginRequest {
    public String email;
    public String password;
  }

  public static class LoginResponse {
    public String accessToken;
  }

  public static class RegisterRequest {
    public String email;
    public String password;
    public String inviteCode;
    public String emailCode;
  }

  public static class RegisterResponse {
    public Long userId;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
