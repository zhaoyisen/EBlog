 package com.eblog.moderation;

 import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
 import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import com.eblog.moderation.entity.AuditLogEntity;
 import com.eblog.moderation.enums.AuditAction;
 import com.eblog.moderation.enums.ModerationStatus;
 import com.eblog.moderation.mapper.OutboxMapper;
 import com.eblog.post.PostEntity;
 import com.eblog.post.PostMapper;
 import com.eblog.user.UserEntity;
 import com.eblog.user.UserMapper;
 import com.eblog.comment.entity.CommentEntity;
 import com.eblog.comment.mapper.CommentMapper;
 import java.util.List;
 import java.util.stream.Collectors;
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

@RestController
@RequestMapping("/api/v1/admin/moderation")
public class AdminModerationController {

  private final ModerationService moderationService;
  private final PostMapper postMapper;
  private final UserMapper userMapper;
  private final CommentMapper commentMapper;

  public AdminModerationController(
      ModerationService moderationService,
      PostMapper postMapper,
      UserMapper userMapper,
      CommentMapper commentMapper) {
    this.moderationService = moderationService;
    this.postMapper = postMapper;
    this.userMapper = userMapper;
    this.commentMapper = commentMapper;
  }

  @GetMapping("/review-queue")
  public ApiResponse<List<ModerationItem>> getReviewQueue(
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    List<PostEntity> posts = moderationService.getPostsNeedingReview(limit, offset);
    List<ModerationItem> items = posts.stream()
      .map(this::toModerationItem)
      .collect(Collectors.toList());

    return ApiResponse.ok(items);
  }

  @PostMapping("/approve/{postId}")
  public ApiResponse<Void> approvePost(@PathVariable Long postId, @RequestBody ModerationRequest request) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }
    moderationService.manualModerate(postId, AuditAction.APPROVE, request.getReason(), currentUserId());
    return ApiResponse.ok(null);
  }

  @PostMapping("/reject/{postId}")
  public ApiResponse<Void> rejectPost(@PathVariable Long postId, @RequestBody ModerationRequest request) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }
    moderationService.manualModerate(postId, AuditAction.REJECT, request.getReason(), currentUserId());
    return ApiResponse.ok(null);
  }

  @GetMapping("/audit-logs")
  public ApiResponse<List<AuditLogEntity>> getAuditLogs(
      @RequestParam(required = false) String entityType,
      @RequestParam(required = false) Long entityId,
      @RequestParam(defaultValue = "50") int limit) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    List<AuditLogEntity> logs = moderationService.getAuditLogs(entityType, entityId, limit);
    return ApiResponse.ok(logs);
  }

  @GetMapping("/review-queue/comments")
  public ApiResponse<List<CommentModerationItem>> getCommentReviewQueue(
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    List<CommentEntity> comments = moderationService.getCommentsNeedingReview(limit, offset);
    List<CommentModerationItem> items = comments.stream()
      .map(this::toCommentModerationItem)
      .collect(Collectors.toList());

    return ApiResponse.ok(items);
  }

  @PostMapping("/approve/comment/{commentId}")
  public ApiResponse<Void> approveComment(@PathVariable Long commentId, @RequestBody ModerationRequest request) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }
    moderationService.manualModerateComment(commentId, AuditAction.APPROVE, request.getReason(), currentUserId());
    return ApiResponse.ok(null);
  }

  @PostMapping("/reject/comment/{commentId}")
  public ApiResponse<Void> rejectComment(@PathVariable Long commentId, @RequestBody ModerationRequest request) {
    if (!isAdmin()) {
      return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }
    moderationService.manualModerateComment(commentId, AuditAction.REJECT, request.getReason(), currentUserId());
    return ApiResponse.ok(null);
  }

  private boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getAuthorities() == null) {
      return false;
    }
    return auth.getAuthorities().stream()
      .anyMatch(a -> "ADMIN".equalsIgnoreCase(a.getAuthority()) || "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
  }

  private Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) {
      return null;
    }
    try {
      return Long.valueOf(String.valueOf(auth.getPrincipal()));
    } catch (Exception e) {
      return null;
    }
  }

  private ModerationItem toModerationItem(PostEntity post) {
    ModerationItem item = new ModerationItem();
    item.setPostId(post.getId());
    item.setTitle(post.getTitle());
    item.setAuthorId(post.getAuthorId());
    item.setModerationStatus(post.getModerationStatus());
    item.setCreatedAt(post.getCreatedAt());
    return item;
  }

  private CommentModerationItem toCommentModerationItem(CommentEntity comment) {
    CommentModerationItem item = new CommentModerationItem();
    item.setCommentId(comment.getId());
    item.setPostId(comment.getPostId());
    item.setAuthorId(comment.getAuthorId());
    item.setContent(comment.getContent());
    item.setModerationStatus(comment.getModerationStatus());
    item.setCreatedAt(comment.getCreatedAt());
    return item;
  }

  public static class ModerationRequest {
    private String reason;

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }
  }

  public static class ModerationItem {
    private Long postId;
    private String title;
    private Long authorId;
    private String moderationStatus;
    private java.time.LocalDateTime createdAt;

    public Long getPostId() {
      return postId;
    }

    public void setPostId(Long postId) {
      this.postId = postId;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public Long getAuthorId() {
      return authorId;
    }

    public void setAuthorId(Long authorId) {
      this.authorId = authorId;
    }

    public String getModerationStatus() {
      return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
      this.moderationStatus = moderationStatus;
    }

    public java.time.LocalDateTime getCreatedAt() {
      return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
      this.createdAt = createdAt;
    }
  }

  public static class CommentModerationItem {
    private Long commentId;
    private Long postId;
    private Long authorId;
    private String content;
    private String moderationStatus;
    private java.time.LocalDateTime createdAt;

    public Long getCommentId() {
      return commentId;
    }

    public void setCommentId(Long commentId) {
      this.commentId = commentId;
    }

    public Long getPostId() {
      return postId;
    }

    public void setPostId(Long postId) {
      this.postId = postId;
    }

    public Long getAuthorId() {
      return authorId;
    }

    public void setAuthorId(Long authorId) {
      this.authorId = authorId;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getModerationStatus() {
      return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
      this.moderationStatus = moderationStatus;
    }

    public java.time.LocalDateTime getCreatedAt() {
      return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
      this.createdAt = createdAt;
    }
  }
}
