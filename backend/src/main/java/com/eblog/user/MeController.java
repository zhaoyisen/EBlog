package com.eblog.user;

import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import com.eblog.auth.RefreshTokenMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

  private final UserMapper userMapper;
  private final RefreshTokenMapper refreshTokenMapper;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public MeController(UserMapper userMapper, RefreshTokenMapper refreshTokenMapper) {
    this.userMapper = userMapper;
    this.refreshTokenMapper = refreshTokenMapper;
  }

  @GetMapping
  public ApiResponse<UserView> getMe() {
    Long userId = currentUserId();
    if (userId == null) {
      return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }

    UserEntity user = userMapper.selectById(userId);
    if (user == null) {
      return ApiResponse.fail(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage());
    }

    UserView view = new UserView();
    view.id = user.getId();
    view.nickname = user.getNickname();
    view.avatarUrl = user.getAvatarUrl();
    view.bio = user.getBio();
    view.createdAt = user.getCreatedAt();
    return ApiResponse.ok(view);
  }

  @PutMapping
  public ApiResponse<UserView> updateMe(@RequestBody UpdateMeRequest body) {
    Long userId = currentUserId();
    if (userId == null) {
      return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }
    if (body == null) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }

    UserEntity user = userMapper.selectById(userId);
    if (user == null) {
      return ApiResponse.fail(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage());
    }

    if (body.nickname != null) {
      user.setNickname(body.nickname.trim());
    }
    if (body.bio != null) {
      user.setBio(body.bio.trim());
    }
    if (body.avatarUrl != null) {
      user.setAvatarUrl(body.avatarUrl.trim());
    }
    user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    userMapper.updateById(user);

    UserView view = new UserView();
    view.id = user.getId();
    view.nickname = user.getNickname();
    view.avatarUrl = user.getAvatarUrl();
    view.bio = user.getBio();
    view.createdAt = user.getCreatedAt();
    return ApiResponse.ok(view);
  }

  @PutMapping("/password")
  public ApiResponse<Object> changePassword(@RequestBody ChangePasswordRequest body) {
    Long userId = currentUserId();
    if (userId == null) {
      return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }
    if (body == null || isBlank(body.current_password) || isBlank(body.new_password)) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    if (body.new_password.trim().length() < 6) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), "密码长度至少为6位");
    }

    UserEntity user = userMapper.selectById(userId);
    if (user == null || user.getPasswordHash() == null) {
      return ApiResponse.fail(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage());
    }
    if (!passwordEncoder.matches(body.current_password, user.getPasswordHash())) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), "当前密码错误");
    }

    user.setPasswordHash(passwordEncoder.encode(body.new_password.trim()));
    user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    userMapper.updateById(user);
    if (refreshTokenMapper != null) {
      refreshTokenMapper.revokeAllByUserId(userId, LocalDateTime.now(ZoneOffset.UTC));
    }
    return ApiResponse.ok(null);
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

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  public static class UserView {
    public Long id;
    public String nickname;
    public String avatarUrl;
    public String bio;
    public LocalDateTime createdAt;
  }

  public static class UpdateMeRequest {
    public String nickname;
    public String avatarUrl;
    public String bio;
  }

  public static class ChangePasswordRequest {
    public String current_password;
    public String new_password;
  }
}
