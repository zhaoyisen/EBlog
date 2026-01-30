package com.eblog.moderation;

import com.eblog.moderation.entity.AuditLogEntity;
import com.eblog.moderation.entity.OutboxEntity;
import com.eblog.moderation.enums.AuditAction;
import com.eblog.moderation.enums.ModerationStatus;
import com.eblog.moderation.enums.OutboxStatus;
import com.eblog.moderation.mapper.AuditLogMapper;
import com.eblog.moderation.mapper.OutboxMapper;
import com.eblog.post.PostEntity;
import com.eblog.post.PostMapper;
import com.eblog.comment.entity.CommentEntity;
import com.eblog.comment.mapper.CommentMapper;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean({OutboxMapper.class, PostMapper.class})
public class WorkerService {

  private static final Logger log = LoggerFactory.getLogger(WorkerService.class);

  private final OutboxService outboxService;
  private final RuleEngine ruleEngine;
  private final AuditLogMapper auditLogMapper;
  private final PostMapper postMapper;
  private final CommentMapper commentMapper;
  private final ModerationService moderationService;

  @Value("${WORKER_INTERVAL_SECONDS:10}")
  private int workerIntervalSeconds;

  @Value("${WORKER_BATCH_SIZE:10}")
  private int workerBatchSize;

  public WorkerService(
      OutboxService outboxService,
      RuleEngine ruleEngine,
      AuditLogMapper auditLogMapper,
      PostMapper postMapper,
      CommentMapper commentMapper,
      ModerationService moderationService) {
    this.outboxService = outboxService;
    this.ruleEngine = ruleEngine;
    this.auditLogMapper = auditLogMapper;
    this.postMapper = postMapper;
    this.commentMapper = commentMapper;
    this.moderationService = moderationService;
    ruleEngine.init();
  }

  @Scheduled(
    fixedRateString = "#{${WORKER_INTERVAL_SECONDS:10} * 1000}",
    initialDelayString = "#{${WORKER_INTERVAL_SECONDS:10} * 1000}"
  )
  @Transactional
  public void processOutbox() {
    try {
      List<OutboxEntity> tasks = outboxService.lockPendingTasks(workerBatchSize);
      if (tasks.isEmpty()) {
        return;
      }

      log.info("Processing {} outbox tasks", tasks.size());

      for (OutboxEntity task : tasks) {
        processTask(task);
      }
    } catch (Exception e) {
      log.error("Error processing outbox", e);
    }
  }

  private void processTask(OutboxEntity task) {
    try {
      outboxService.markProcessing(task.getId());

    if ("POST".equals(task.getEntityType())) {
      processPost(task);
    } else if ("COMMENT".equals(task.getEntityType())) {
      processComment(task);
    } else {
      log.warn("Unknown entity type: {}", task.getEntityType());
    }

      outboxService.markCompleted(task.getId());
      log.info("Processed outbox task: {} {}", task.getEntityType(), task.getEntityId());
    } catch (Exception e) {
      log.error("Error processing outbox task: {} {}", task.getEntityType(), task.getEntityId(), e);
      outboxService.markFailed(task.getId(), e.getMessage());
    }
  }

  private void processPost(OutboxEntity task) {
    PostEntity post = postMapper.selectById(task.getEntityId());
    if (post == null) {
      log.warn("Post not found for outbox task: {}", task.getEntityId());
      return;
    }

    RuleEngine.RuleResult result = ruleEngine.evaluate(post.getTitle(), post.getContentMarkdown());
    ModerationStatus newStatus = result.getStatus();

    if (newStatus == ModerationStatus.APPROVED) {
      moderationService.updateModerationStatus(post.getId(), ModerationStatus.APPROVED, null, null);
    } else if (newStatus == ModerationStatus.REJECTED) {
      moderationService.updateModerationStatus(post.getId(), ModerationStatus.REJECTED, result.getReason(), result.getRuleHit());
    } else if (newStatus == ModerationStatus.NEEDS_REVIEW) {
      moderationService.updateModerationStatus(post.getId(), ModerationStatus.NEEDS_REVIEW, result.getReason(), result.getRuleHit());
    }
  }

  private void processComment(OutboxEntity task) {
    CommentEntity comment = commentMapper.selectById(task.getEntityId());
    if (comment == null) {
      log.warn("Comment not found for outbox task: {}", task.getEntityId());
      return;
    }

    RuleEngine.RuleResult result = ruleEngine.evaluate("", comment.getContent());
    ModerationStatus newStatus = result.getStatus();

    if (newStatus == ModerationStatus.APPROVED) {
      moderationService.updateCommentModerationStatus(comment.getId(), ModerationStatus.APPROVED, null, null);
    } else if (newStatus == ModerationStatus.REJECTED) {
      moderationService.updateCommentModerationStatus(comment.getId(), ModerationStatus.REJECTED, result.getReason(), result.getRuleHit());
    } else if (newStatus == ModerationStatus.NEEDS_REVIEW) {
      moderationService.updateCommentModerationStatus(comment.getId(), ModerationStatus.NEEDS_REVIEW, result.getReason(), result.getRuleHit());
    }
  }

  public void logAudit(String entityType, Long entityId, Long actorId, AuditAction action, String reason, String ruleHit) {
    AuditLogEntity log = new AuditLogEntity();
    log.setEntityType(entityType);
    log.setEntityId(entityId);
    log.setActorId(actorId);
    log.setAction(action.name());
    log.setReason(reason);
    log.setRuleHit(ruleHit);
    log.setCreatedAt(LocalDateTime.now());
    auditLogMapper.insert(log);
  }
}
