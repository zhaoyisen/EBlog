package com.eblog.moderation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.moderation.entity.AuditLogEntity;
import com.eblog.moderation.enums.AuditAction;
import com.eblog.moderation.enums.ModerationStatus;
import com.eblog.moderation.enums.OutboxStatus;
import com.eblog.moderation.mapper.AuditLogMapper;
import com.eblog.post.PostEntity;
import com.eblog.post.PostMapper;
import com.eblog.comment.entity.CommentEntity;
import com.eblog.comment.mapper.CommentMapper;
import java.util.List;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean({PostMapper.class, AuditLogMapper.class})
public class ModerationService {

  private final PostMapper postMapper;
  private final CommentMapper commentMapper;
  private final AuditLogMapper auditLogMapper;
  private final WorkerService workerService;

  public ModerationService(PostMapper postMapper, CommentMapper commentMapper, AuditLogMapper auditLogMapper, WorkerService workerService) {
    this.postMapper = postMapper;
    this.commentMapper = commentMapper;
    this.auditLogMapper = auditLogMapper;
    this.workerService = workerService;
  }

  @Transactional
  public void updateModerationStatus(Long postId, ModerationStatus status, String reason, String ruleHit) {
    PostEntity post = postMapper.selectById(postId);
    if (post == null) {
      return;
    }

    post.setModerationStatus(status.name());
    post.setUpdatedAt(LocalDateTime.now());
    postMapper.updateById(post);

    if (status == ModerationStatus.REJECTED || status == ModerationStatus.NEEDS_REVIEW) {
      AuditAction action = status == ModerationStatus.REJECTED ? AuditAction.RULE_REJECT : AuditAction.REQUEST_REVIEW;
      workerService.logAudit("POST", postId, null, action, reason, ruleHit);
    }
  }

  @Transactional
  public void updateCommentModerationStatus(Long commentId, ModerationStatus status, String reason, String ruleHit) {
    CommentEntity comment = commentMapper.selectById(commentId);
    if (comment == null) {
      return;
    }

    comment.setModerationStatus(status.name());
    comment.setUpdatedAt(LocalDateTime.now());
    commentMapper.updateById(comment);

    if (status == ModerationStatus.REJECTED || status == ModerationStatus.NEEDS_REVIEW) {
      AuditAction action = status == ModerationStatus.REJECTED ? AuditAction.RULE_REJECT : AuditAction.REQUEST_REVIEW;
      workerService.logAudit("COMMENT", commentId, null, action, reason, ruleHit);
    }
  }

  public void manualModerate(Long postId, AuditAction action, String reason, Long actorId) {
    PostEntity post = postMapper.selectById(postId);
    if (post == null) {
      return;
    }

    ModerationStatus status;
    switch (action) {
      case APPROVE:
        status = ModerationStatus.APPROVED;
        break;
      case REJECT:
        status = ModerationStatus.REJECTED;
        break;
      case REQUEST_REVIEW:
        status = ModerationStatus.NEEDS_REVIEW;
        break;
      default:
        return;
    }

    post.setModerationStatus(status.name());
    post.setUpdatedAt(LocalDateTime.now());
    postMapper.updateById(post);

    workerService.logAudit("POST", postId, actorId, action, reason, null);
  }

  public void manualModerateComment(Long commentId, AuditAction action, String reason, Long actorId) {
    CommentEntity comment = commentMapper.selectById(commentId);
    if (comment == null) {
      return;
    }

    ModerationStatus status;
    switch (action) {
      case APPROVE:
        status = ModerationStatus.APPROVED;
        break;
      case REJECT:
        status = ModerationStatus.REJECTED;
        break;
      case REQUEST_REVIEW:
        status = ModerationStatus.NEEDS_REVIEW;
        break;
      default:
        return;
    }

    comment.setModerationStatus(status.name());
    comment.setUpdatedAt(LocalDateTime.now());
    commentMapper.updateById(comment);

    workerService.logAudit("COMMENT", commentId, actorId, action, reason, null);
  }

  public List<PostEntity> getPostsNeedingReview(int limit, int offset) {
    return postMapper.selectList(
      new LambdaQueryWrapper<PostEntity>()
        .eq(PostEntity::getModerationStatus, ModerationStatus.NEEDS_REVIEW.name())
        .orderByDesc(PostEntity::getCreatedAt)
        .last("LIMIT " + Math.max(limit, 1) + " OFFSET " + Math.max(offset, 0))
    );
  }

  public List<CommentEntity> getCommentsNeedingReview(int limit, int offset) {
    return commentMapper.selectList(
      new LambdaQueryWrapper<CommentEntity>()
        .eq(CommentEntity::getModerationStatus, ModerationStatus.NEEDS_REVIEW.name())
        .orderByDesc(CommentEntity::getCreatedAt)
        .last("LIMIT " + Math.max(limit, 1) + " OFFSET " + Math.max(offset, 0))
    );
  }

  public List<AuditLogEntity> getAuditLogs(String entityType, Long entityId, int limit) {
    LambdaQueryWrapper<AuditLogEntity> wrapper = new LambdaQueryWrapper<>();
    if (entityType != null) {
      wrapper.eq(AuditLogEntity::getEntityType, entityType);
    }
    if (entityId != null) {
      wrapper.eq(AuditLogEntity::getEntityId, entityId);
    }
    wrapper.orderByDesc(AuditLogEntity::getCreatedAt).last("LIMIT " + Math.max(limit, 1));
    return auditLogMapper.selectList(wrapper);
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
}
