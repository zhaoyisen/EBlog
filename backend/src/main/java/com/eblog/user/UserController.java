package com.eblog.user;

import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserMapper userMapper;
  private final UserFollowsMapper userFollowsMapper;

  public UserController(UserMapper userMapper, UserFollowsMapper userFollowsMapper) {
    this.userMapper = userMapper;
    this.userFollowsMapper = userFollowsMapper;
  }

  @GetMapping("/active")
  public ApiResponse<List<MeController.UserView>> listActive(
      @RequestParam(name = "limit", defaultValue = "10") int limit) {
    int safeLimit = Math.min(Math.max(limit, 1), 50);
    List<UserEntity> users = userMapper.selectActiveUsers(safeLimit);
    return ApiResponse.ok(users.stream().map(this::toView).collect(Collectors.toList()));
  }

  @PostMapping("/{id}/follow")
  public ApiResponse<Void> follow(@PathVariable Long id) {
    Long currentUserId = currentUserId();
    if (currentUserId == null) {
      return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }
    if (id.equals(currentUserId)) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), "Cannot follow yourself");
    }
    userFollowsMapper.insert(currentUserId, id);
    return ApiResponse.ok(null);
  }

  @DeleteMapping("/{id}/follow")
  public ApiResponse<Void> unfollow(@PathVariable Long id) {
    Long currentUserId = currentUserId();
    if (currentUserId == null) {
      return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }
    userFollowsMapper.delete(currentUserId, id);
    return ApiResponse.ok(null);
  }

  @GetMapping("/{id}/followers")
  public ApiResponse<List<MeController.UserView>> getFollowers(@PathVariable Long id) {
    List<Long> followerIds = userFollowsMapper.selectFollowers(id);
    if (followerIds.isEmpty()) {
      return ApiResponse.ok(List.of());
    }
    List<UserEntity> users = userMapper.selectBatchIds(followerIds);
    return ApiResponse.ok(users.stream().map(this::toView).collect(Collectors.toList()));
  }

  @GetMapping("/{id}/following")
  public ApiResponse<List<MeController.UserView>> getFollowing(@PathVariable Long id) {
    List<Long> followeeIds = userFollowsMapper.selectFollowees(id);
    if (followeeIds.isEmpty()) {
      return ApiResponse.ok(List.of());
    }
    List<UserEntity> users = userMapper.selectBatchIds(followeeIds);
    return ApiResponse.ok(users.stream().map(this::toView).collect(Collectors.toList()));
  }

  private MeController.UserView toView(UserEntity user) {
    MeController.UserView view = new MeController.UserView();
    view.id = user.getId();
    view.email = null; // Hide email for public
    view.nickname = user.getNickname();
    view.avatarUrl = user.getAvatarUrl();
    view.bio = user.getBio();
    view.createdAt = user.getCreatedAt();
    return view;
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
}
