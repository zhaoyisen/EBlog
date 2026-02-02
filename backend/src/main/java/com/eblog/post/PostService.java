package com.eblog.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.api.common.ErrorCode;
import com.eblog.moderation.OutboxService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
  private final PostMapper postMapper;
  private final OutboxService outboxService;

  public PostService(PostMapper postMapper, Optional<OutboxService> outboxService) {
    this.postMapper = postMapper;
    this.outboxService = outboxService.orElse(null);
  }

  public List<PostEntity> listPublic(int limit, int offset) {
    int safeLimit = Math.min(Math.max(limit, 1), 50);
    int safeOffset = Math.max(offset, 0);
    return postMapper.listPublic(safeLimit, safeOffset);
  }

  public List<PostEntity> listMy(long authorId, int limit, int offset) {
    int safeLimit = Math.min(Math.max(limit, 1), 100);
    int safeOffset = Math.max(offset, 0);
    return postMapper.listMy(authorId, safeLimit, safeOffset);
  }

  public PostEntity findBySlug(String slug) {
    if (slug == null || slug.trim().isEmpty()) {
      return null;
    }
    return postMapper.selectOne(new LambdaQueryWrapper<PostEntity>().eq(PostEntity::getSlug, slug.trim()));
  }

  @Transactional
  public CreateResult create(String title, String summary, String contentMarkdown, String tagsCsv, String category, String status, String format) {
    Long userId = currentUserId();
    if (userId == null) {
      return CreateResult.error(ErrorCode.UNAUTHORIZED);
    }
    if (isBlank(title) || isBlank(contentMarkdown)) {
      return CreateResult.error(ErrorCode.BAD_REQUEST);
    }

    String normalizedFormat = normalizeFormat(format);
    if ("MDX".equals(normalizedFormat) && !isAdmin()) {
      return CreateResult.error(ErrorCode.FORBIDDEN);
    }

    PostEntity entity = new PostEntity();
    entity.setAuthorId(userId);
    entity.setFormat(normalizedFormat);
    entity.setTitle(title.trim());
    entity.setSlug(SlugGenerator.randomSlug());
    entity.setSummary(summary == null ? null : summary.trim());
    entity.setContentMarkdown(contentMarkdown);
    entity.setTagsCsv(tagsCsv == null ? null : tagsCsv.trim());
    entity.setCategory(category == null ? null : category.trim());
    entity.setStatus(normalizeStatus(status));
    entity.setModerationStatus("PENDING");
    entity.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
    entity.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    postMapper.insert(entity);

    if ("PUBLISHED".equals(normalizeStatus(status)) && outboxService != null) {
      String deduplicationKey = "v-" + entity.getUpdatedAt().atZone(ZoneOffset.UTC).toEpochSecond();
      outboxService.enqueue("POST", entity.getId(), deduplicationKey);
    }

    return CreateResult.success(entity.getId(), entity.getSlug());
  }

  @Transactional
  public ErrorCode update(Long postId, String title, String summary, String contentMarkdown, String tagsCsv, String category, String status) {
    Long userId = currentUserId();
    if (userId == null) {
      return ErrorCode.UNAUTHORIZED;
    }
    if (postId == null || postId <= 0) {
      return ErrorCode.BAD_REQUEST;
    }

    PostEntity existing = postMapper.selectById(postId);
    if (existing == null) {
      return ErrorCode.POST_NOT_FOUND;
    }
    if (!canEdit(existing, userId)) {
      return ErrorCode.FORBIDDEN;
    }

    if (!isBlank(title)) {
      existing.setTitle(title.trim());
    }
    if (summary != null) {
      existing.setSummary(summary.trim());
    }
    if (!isBlank(contentMarkdown)) {
      existing.setContentMarkdown(contentMarkdown);
    }
    if (tagsCsv != null) {
      existing.setTagsCsv(tagsCsv.trim());
    }
    if (category != null) {
      existing.setCategory(category.trim());
    }
    String oldStatus = existing.getStatus();
    if (status != null) {
      existing.setStatus(normalizeStatus(status));
    }
    String newStatus = existing.getStatus();
    existing.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    postMapper.updateById(existing);

    if (! "PUBLISHED".equals(oldStatus) && "PUBLISHED".equals(newStatus) && outboxService != null) {
      String deduplicationKey = "v-" + existing.getUpdatedAt().atZone(ZoneOffset.UTC).toEpochSecond();
      outboxService.enqueue("POST", postId, deduplicationKey);
      existing.setModerationStatus("PENDING");
      postMapper.updateById(existing);
    }
    return null;
  }

  @Transactional
  public ErrorCode update(Long postId, String title, String summary, String contentMarkdown, String tagsCsv, String category, String status, String format) {
    ErrorCode base = update(postId, title, summary, contentMarkdown, tagsCsv, category, status);
    if (base != null) {
      return base;
    }
    if (format == null) {
      return null;
    }

    Long userId = currentUserId();
    if (userId == null) {
      return ErrorCode.UNAUTHORIZED;
    }
    PostEntity existing = postMapper.selectById(postId);
    if (existing == null) {
      return ErrorCode.POST_NOT_FOUND;
    }
    if (!canEdit(existing, userId)) {
      return ErrorCode.FORBIDDEN;
    }

    String normalizedFormat = normalizeFormat(format);
    if ("MDX".equals(normalizedFormat) && !isAdmin()) {
      return ErrorCode.FORBIDDEN;
    }
    existing.setFormat(normalizedFormat);
    existing.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    postMapper.updateById(existing);
    return null;
  }

  @Transactional
  public ErrorCode archive(Long postId) {
    Long userId = currentUserId();
    if (userId == null) {
      return ErrorCode.UNAUTHORIZED;
    }
    if (postId == null || postId <= 0) {
      return ErrorCode.BAD_REQUEST;
    }
    PostEntity existing = postMapper.selectById(postId);
    if (existing == null) {
      return ErrorCode.POST_NOT_FOUND;
    }
    if (!canEdit(existing, userId)) {
      return ErrorCode.FORBIDDEN;
    }
    existing.setStatus("ARCHIVED");
    existing.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    postMapper.updateById(existing);
    return null;
  }

  public List<PostEntity> search(String q, String tag, Long authorId, int limit, int offset) {
    int safeLimit = Math.min(Math.max(limit, 1), 50);
    int safeOffset = Math.max(offset, 0);
    return postMapper.search(isBlank(q) ? null : q.trim(), isBlank(tag) ? null : tag.trim(), authorId, safeLimit, safeOffset);
  }

  public void incrementViewCount(Long postId) {
    if (postId != null) {
      postMapper.incrementViewCount(postId);
    }
  }

  private static boolean canEdit(PostEntity post, Long userId) {
    if (post.getAuthorId() != null && post.getAuthorId().equals(userId)) {
      return true;
    }
    return isAdmin();
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

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private static String normalizeFormat(String format) {
    if (format == null) {
      return "MARKDOWN";
    }
    String f = format.trim().toUpperCase();
    if ("MDX".equals(f) || "MARKDOWN".equals(f)) {
      return f;
    }
    return "MARKDOWN";
  }

  private static String normalizeStatus(String status) {
    if (status == null) {
      return "DRAFT";
    }
    String s = status.trim().toUpperCase();
    if ("PUBLISHED".equals(s) || "DRAFT".equals(s) || "ARCHIVED".equals(s)) {
      return s;
    }
    return "DRAFT";
  }

  public static class CreateResult {
    private final ErrorCode error;
    private final Long postId;
    private final String slug;

    private CreateResult(ErrorCode error, Long postId, String slug) {
      this.error = error;
      this.postId = postId;
      this.slug = slug;
    }

    public static CreateResult success(Long postId, String slug) {
      return new CreateResult(null, postId, slug);
    }

    public static CreateResult error(ErrorCode error) {
      return new CreateResult(error, null, null);
    }

    public boolean isSuccess() {
      return error == null;
    }

    public ErrorCode getError() {
      return error;
    }

    public Long getPostId() {
      return postId;
    }

    public String getSlug() {
      return slug;
    }
  }
}
