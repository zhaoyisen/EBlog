package com.eblog.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import com.eblog.auth.RefreshTokenMapper;
import com.eblog.user.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

  private final UserMapper userMapper;
  private final RefreshTokenMapper refreshTokenMapper;

  public AdminUserController(UserMapper userMapper, RefreshTokenMapper refreshTokenMapper) {
    this.userMapper = userMapper;
    this.refreshTokenMapper = refreshTokenMapper;
  }

  @GetMapping
  public ApiResponse<List<UserListItem>> listUsers(
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByDesc(UserEntity::getCreatedAt);
    wrapper.last("LIMIT " + Math.max(limit, 1) + " OFFSET " + Math.max(offset, 0));

    return ApiResponse.ok(userMapper.selectList(wrapper).stream()
      .map(this::toUserListItem)
      .collect(java.util.stream.Collectors.toList()));
  }

  @PostMapping("/ban/{userId}")
  public ApiResponse<Void> banUser(@PathVariable Long userId, @RequestBody BanRequest request) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    UserEntity user = userMapper.selectById(userId);
    if (user == null) {
      return ApiResponse.fail(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage());
    }

    user.setIsBanned(true);
    user.setBannedReason(request.reason);
    user.setBannedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    userMapper.updateById(user);

    revokeAllRefreshTokens(userId);

    return ApiResponse.ok(null);
  }

  @PostMapping("/unban/{userId}")
  public ApiResponse<Void> unbanUser(@PathVariable Long userId) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    UserEntity user = userMapper.selectById(userId);
    if (user == null) {
      return ApiResponse.fail(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage());
    }

    user.setIsBanned(false);
    user.setBannedReason(null);
    user.setBannedAt(null);
    user.setUpdatedAt(LocalDateTime.now());
    userMapper.updateById(user);

    return ApiResponse.ok(null);
  }

  private void revokeAllRefreshTokens(Long userId) {
    refreshTokenMapper.delete(new LambdaQueryWrapper<com.eblog.auth.RefreshTokenEntity>()
      .eq(com.eblog.auth.RefreshTokenEntity::getUserId, userId)
    );
  }

  private boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getAuthorities() == null) {
      return false;
    }
    return auth.getAuthorities().stream()
      .anyMatch(a -> "ADMIN".equalsIgnoreCase(a.getAuthority()) || "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
  }

  private UserListItem toUserListItem(UserEntity user) {
    UserListItem item = new UserListItem();
    item.userId = user.getId();
    item.nickname = user.getNickname();
    item.role = user.getRole();
    item.isBanned = user.getIsBanned();
    item.bannedReason = user.getBannedReason();
    item.createdAt = user.getCreatedAt();
    return item;
  }

  public static class BanRequest {
    public String reason;
  }

  public static class UserListItem {
    public Long userId;
    public String nickname;
    public String role;
    public Boolean isBanned;
    public String bannedReason;
    public LocalDateTime createdAt;
  }
}
