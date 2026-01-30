package com.eblog.comment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.api.common.ErrorCode;
import com.eblog.comment.entity.CommentEntity;
import com.eblog.comment.mapper.CommentMapper;
import com.eblog.moderation.ModerationService;
import com.eblog.moderation.OutboxService;
import com.eblog.post.PostEntity;
import com.eblog.post.PostMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean({CommentMapper.class, PostMapper.class})
public class CommentService {

  private final CommentMapper commentMapper;
  private final PostMapper postMapper;
  private final OutboxService outboxService;
  private final ModerationService moderationService;

  private static final Map<String, AtomicInteger> rateLimitMap = new ConcurrentHashMap<>();
  private static final int MIN_COMMENT_LENGTH = 1;
  private static final int MAX_COMMENT_LENGTH = 1000;
  private static final int RATE_LIMIT_SECONDS = 60;
  private static final int MAX_COMMENTS_PER_WINDOW = 3;

  public CommentService(
      CommentMapper commentMapper,
      PostMapper postMapper,
      OutboxService outboxService,
      ModerationService moderationService) {
    this.commentMapper = commentMapper;
    this.postMapper = postMapper;
    this.outboxService = outboxService;
    this.moderationService = moderationService;
  }

  public List<CommentEntity> listPublicByPostId(Long postId, int limit, int offset) {
    int safeLimit = Math.min(Math.max(limit, 1), 100);
    int safeOffset = Math.max(offset, 0);
    return commentMapper.listPublicByPostId(postId, safeLimit, safeOffset);
  }

  public List<CommentEntity> listByPostId(Long postId, int limit, int offset) {
    int safeLimit = Math.min(Math.max(limit, 1), 100);
    int safeOffset = Math.max(offset, 0);
    return commentMapper.listByPostId(postId, safeLimit, safeOffset);
  }

  @Transactional
  public CreateResult create(Long postId, String content) {
    Long userId = currentUserId();
    if (userId == null) {
      return CreateResult.error(ErrorCode.UNAUTHORIZED);
    }

    if (!checkRateLimit(userId)) {
      return CreateResult.error(ErrorCode.TOO_MANY_REQUESTS);
    }

    if (content == null || content.trim().length() < MIN_COMMENT_LENGTH) {
      return CreateResult.error(ErrorCode.BAD_REQUEST);
    }

    if (content.length() > MAX_COMMENT_LENGTH) {
      return CreateResult.error(ErrorCode.BAD_REQUEST);
    }

    PostEntity post = postMapper.selectById(postId);
    if (post == null || !"PUBLISHED".equals(post.getStatus()) || "REJECTED".equals(post.getModerationStatus())) {
      return CreateResult.error(ErrorCode.POST_NOT_FOUND);
    }

    CommentEntity comment = new CommentEntity();
    comment.setPostId(postId);
    comment.setAuthorId(userId);
    comment.setContent(content.trim());
    comment.setStatus("PUBLISHED");
    comment.setModerationStatus("PENDING");
    comment.setCreatedAt(LocalDateTime.now());
    comment.setUpdatedAt(LocalDateTime.now());
    commentMapper.insert(comment);

    if (outboxService != null) {
      outboxService.enqueue("COMMENT", comment.getId());
    }

    return CreateResult.success(comment.getId());
  }

  @Transactional
  public ErrorCode delete(Long commentId) {
    Long userId = currentUserId();
    if (userId == null) {
      return ErrorCode.UNAUTHORIZED;
    }

    CommentEntity comment = commentMapper.selectById(commentId);
    if (comment == null) {
      return ErrorCode.COMMENT_NOT_FOUND;
    }

    if (!comment.getAuthorId().equals(userId) && !isAdmin()) {
      return ErrorCode.FORBIDDEN;
    }

    commentMapper.deleteById(commentId);
    return null;
  }

  private boolean checkRateLimit(Long userId) {
    String key = "comment:" + userId;
    AtomicInteger count = rateLimitMap.computeIfAbsent(key, k -> new AtomicInteger(0));

    if (count.incrementAndGet() > MAX_COMMENTS_PER_WINDOW) {
      count.decrementAndGet();
      return false;
    }

    new Thread(() -> {
      try {
        Thread.sleep(RATE_LIMIT_SECONDS * 1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        count.decrementAndGet();
        if (count.get() == 0) {
          rateLimitMap.remove(key);
        }
      }
    }).start();

    return true;
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

  private boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getAuthorities() == null) {
      return false;
    }
    return auth.getAuthorities().stream()
      .anyMatch(a -> "ADMIN".equalsIgnoreCase(a.getAuthority()) || "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
  }

  public static class CreateResult {
    private final ErrorCode error;
    private final Long commentId;

    private CreateResult(ErrorCode error, Long commentId) {
      this.error = error;
      this.commentId = commentId;
    }

    public static CreateResult success(Long commentId) {
      return new CreateResult(null, commentId);
    }

    public static CreateResult error(ErrorCode error) {
      return new CreateResult(error, null);
    }

    public boolean isSuccess() {
      return error == null;
    }

    public ErrorCode getError() {
      return error;
    }

    public Long getCommentId() {
      return commentId;
    }
  }
}
