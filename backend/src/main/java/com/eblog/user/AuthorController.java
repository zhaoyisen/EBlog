package com.eblog.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {
  private final UserMapper userMapper;

  public AuthorController(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @GetMapping
  public ApiResponse<List<AuthorView>> listAuthors(
      @RequestParam(name = "limit", required = false, defaultValue = "20") int limit,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
    int safeLimit = Math.min(Math.max(limit, 1), 50);
    int safeOffset = Math.max(offset, 0);

    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByDesc(UserEntity::getCreatedAt);
    wrapper.last("LIMIT " + safeLimit + " OFFSET " + safeOffset);

    List<UserEntity> users = userMapper.selectList(wrapper);
    List<AuthorView> res = new ArrayList<>();
    for (UserEntity user : users) {
      AuthorView view = new AuthorView();
      view.id = user.getId();
      view.nickname = user.getNickname();
      view.avatarUrl = user.getAvatarUrl();
      view.bio = user.getBio();
      view.createdAt = user.getCreatedAt();
      res.add(view);
    }
    return ApiResponse.ok(res);
  }

  @GetMapping("/{id}")
  public ApiResponse<AuthorView> getAuthor(@PathVariable("id") Long id) {
    if (id == null || id <= 0) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    UserEntity user = userMapper.selectById(id);
    if (user == null) {
      return ApiResponse.fail(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage());
    }
    AuthorView view = new AuthorView();
    view.id = user.getId();
    view.nickname = user.getNickname();
    view.avatarUrl = user.getAvatarUrl();
    view.bio = user.getBio();
    view.createdAt = user.getCreatedAt();
    return ApiResponse.ok(view);
  }

  public static class AuthorView {
    public Long id;
    public String nickname;
    public String avatarUrl;
    public String bio;
    public LocalDateTime createdAt;
  }
}
