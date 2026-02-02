 package com.eblog.post;

import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import com.eblog.user.UserMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
  private final PostService postService;
  private final MarkdownRenderer markdownRenderer;
  private final UserMapper userMapper;
  // 作者信息缓存（避免重复查询）
  private final Map<Long, String> authorNicknameCache = new HashMap<>();
  private final Map<Long, String> authorAvatarCache = new HashMap<>();

  public PostController(PostService postService, MarkdownRenderer markdownRenderer, UserMapper userMapper) {
    this.postService = postService;
    this.markdownRenderer = markdownRenderer;
    this.userMapper = userMapper;
  }

  @GetMapping
  public ApiResponse<List<PostSummary>> list(
      @RequestParam(name = "limit", required = false, defaultValue = "20") int limit,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
    List<PostEntity> posts = postService.listPublic(limit, offset);
    List<PostSummary> res = new ArrayList<>();
    for (PostEntity p : posts) {
      PostSummary s = new PostSummary();
      s.id = p.getId();
      s.authorId = p.getAuthorId();
      s.authorName = getAuthorNickname(p.getAuthorId());
      s.authorAvatar = getAuthorAvatar(p.getAuthorId());
      s.title = p.getTitle();
      s.slug = p.getSlug();
      s.summary = p.getSummary();
      s.tagsCsv = p.getTagsCsv();
      s.category = p.getCategory();
      s.createdAt = p.getCreatedAt();
      s.status = p.getStatus();
      s.moderationStatus = p.getModerationStatus();
      res.add(s);
    }
    return ApiResponse.ok(res);
  }

  @GetMapping("/my")
  public ApiResponse<List<MyPostSummary>> listMy(
      @RequestParam(name = "limit", required = false, defaultValue = "20") int limit,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
    Long userId = currentUserId();
    if (userId == null) {
      return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }
    List<PostEntity> posts = postService.listMy(userId, limit, offset);
    List<MyPostSummary> res = new ArrayList<>();
    for (PostEntity p : posts) {
      MyPostSummary s = new MyPostSummary();
      s.id = p.getId();
      s.title = p.getTitle();
      s.slug = p.getSlug();
      s.summary = p.getSummary();
      s.category = p.getCategory();
      s.status = p.getStatus();
      s.createdAt = p.getCreatedAt();
      s.updatedAt = p.getUpdatedAt();
      res.add(s);
    }
    return ApiResponse.ok(res);
  }

  @GetMapping("/{slug}")
  public ApiResponse<PostDetail> get(@PathVariable("slug") String slug) {
    PostEntity p = postService.findBySlug(slug);
    if (p == null) {
      return ApiResponse.fail(ErrorCode.POST_NOT_FOUND.getCode(), ErrorCode.POST_NOT_FOUND.getMessage());
    }

    boolean isPublic = "PUBLISHED".equalsIgnoreCase(p.getStatus()) && "APPROVED".equalsIgnoreCase(p.getModerationStatus());
    if (!isPublic) {
      Long userId = currentUserId();
      boolean isAuthor = userId != null && userId.equals(p.getAuthorId());
      if (!isAuthor && !isAdmin()) {
        return ApiResponse.fail(ErrorCode.POST_NOT_FOUND.getCode(), ErrorCode.POST_NOT_FOUND.getMessage());
      }
    }

    PostDetail d = new PostDetail();
    d.id = p.getId();
    d.authorId = p.getAuthorId();
    d.authorName = getAuthorNickname(p.getAuthorId());
    d.authorAvatar = getAuthorAvatar(p.getAuthorId());
    d.title = p.getTitle();
    d.slug = p.getSlug();
    d.summary = p.getSummary();
    d.contentMarkdown = p.getContentMarkdown();
    d.format = p.getFormat();
    if ("MDX".equalsIgnoreCase(p.getFormat())) {
      d.contentHtml = null;
    } else {
      d.contentHtml = markdownRenderer.renderToHtml(p.getContentMarkdown());
    }
    d.tagsCsv = p.getTagsCsv();
    d.category = p.getCategory();
    d.createdAt = p.getCreatedAt();
    d.updatedAt = p.getUpdatedAt();
    return ApiResponse.ok(d);
  }

  @PostMapping
  public ApiResponse<CreateResponse> create(@RequestBody CreateRequest body) {
    if (body == null) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    PostService.CreateResult result = postService.create(
        body.title, body.summary, body.contentMarkdown, body.tagsCsv, body.category, body.status, body.format);
    if (!result.isSuccess()) {
      ErrorCode error = result.getError();
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    CreateResponse res = new CreateResponse();
    res.postId = result.getPostId();
    res.slug = result.getSlug();
    return ApiResponse.ok(res);
  }

  @PutMapping("/{id}")
  public ApiResponse<Object> update(@PathVariable("id") Long id, @RequestBody UpdateRequest body) {
    if (body == null) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    ErrorCode error = postService.update(id, body.title, body.summary, body.contentMarkdown, body.tagsCsv, body.category, body.status, body.format);
    if (error != null) {
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(null);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Object> archive(@PathVariable("id") Long id) {
    ErrorCode error = postService.archive(id);
    if (error != null) {
      return ApiResponse.fail(error.getCode(), error.getMessage());
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

  private String getAuthorNickname(Long authorId) {
    if (authorId == null) {
      return null;
    }
    if (!authorNicknameCache.containsKey(authorId)) {
      var user = userMapper.selectById(authorId);
      authorNicknameCache.put(authorId, user != null && user.getNickname() != null && !user.getNickname().trim().isEmpty()
          ? user.getNickname().trim()
          : "#" + authorId);
    }
    return authorNicknameCache.get(authorId);
  }

  private String getAuthorAvatar(Long authorId) {
    if (authorId == null) {
      return null;
    }
    if (!authorAvatarCache.containsKey(authorId)) {
      var user = userMapper.selectById(authorId);
      authorAvatarCache.put(authorId, user != null ? user.getAvatarUrl() : null);
    }
    return authorAvatarCache.get(authorId);
  }

  private static boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getAuthorities() == null) {
      return false;
    }
    for (GrantedAuthority a : auth.getAuthorities()) {
      String role = a.getAuthority();
      if ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role)) {
        return true;
      }
    }
    return false;
  }

  public static class CreateRequest {
    public String title;
    public String summary;
    public String contentMarkdown;
    public String tagsCsv;
    public String category;
    public String status;
    public String format;
  }

  public static class UpdateRequest {
    public String title;
    public String summary;
    public String contentMarkdown;
    public String tagsCsv;
    public String category;
    public String status;
    public String format;
  }

  public static class CreateResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    public Long postId;
    public String slug;
  }

  public static class PostSummary {
    @JsonSerialize(using = ToStringSerializer.class)
    public Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    public Long authorId;
    public String authorName;
    public String authorAvatar;
    public String title;
    public String slug;
    public String summary;
    public String tagsCsv;
    public String category;
    public LocalDateTime createdAt;
    public String status;
    public String moderationStatus;
  }

  public static class MyPostSummary {
    @JsonSerialize(using = ToStringSerializer.class)
    public Long id;
    public String title;
    public String slug;
    public String summary;
    public String category;
    public String status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
  }

  public static class PostDetail {
    @JsonSerialize(using = ToStringSerializer.class)
    public Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    public Long authorId;
    public String authorName;
    public String authorAvatar;
    public String title;
    public String slug;
    public String summary;
    public String contentMarkdown;
    public String contentHtml;
    public String format;
    public String tagsCsv;
    public String category;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
  }
}
