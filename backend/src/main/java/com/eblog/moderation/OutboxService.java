package com.eblog.moderation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.moderation.entity.OutboxEntity;
import com.eblog.moderation.enums.OutboxStatus;
import com.eblog.moderation.mapper.OutboxMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

  private final OutboxMapper outboxMapper;

  public OutboxService(OutboxMapper outboxMapper) {
    this.outboxMapper = outboxMapper;
  }

  @Transactional
  public void enqueue(String entityType, Long entityId, String deduplicationKey) {
    // Idempotency check: if a PENDING or PROCESSING task with same key exists, skip
    OutboxEntity existing = outboxMapper.selectOne(
      new LambdaQueryWrapper<OutboxEntity>()
        .eq(OutboxEntity::getEntityType, entityType)
        .eq(OutboxEntity::getEntityId, entityId)
        .eq(OutboxEntity::getDeduplicationKey, deduplicationKey)
        .in(OutboxEntity::getStatus, OutboxStatus.PENDING.name(), OutboxStatus.PROCESSING.name())
    );

    if (existing != null) {
      return;
    }

    OutboxEntity entity = new OutboxEntity();
    entity.setEntityType(entityType);
    entity.setEntityId(entityId);
    entity.setDeduplicationKey(deduplicationKey);
    entity.setDeduplicationKeyUpdatedAt(LocalDateTime.now());
    entity.setStatus(OutboxStatus.PENDING.name());
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());
    entity.setAttempts(0);
    outboxMapper.insert(entity);
  }

  @Transactional
  public void enqueue(String entityType, Long entityId) {
    enqueue(entityType, entityId, "auto");
  }

  public List<OutboxEntity> lockPendingTasks(int limit) {
    return outboxMapper.lockPendingTasks(limit);
  }

  public int markProcessing(Long id) {
    return outboxMapper.markProcessing(id);
  }

  public int markCompleted(Long id) {
    return outboxMapper.markCompleted(id);
  }

  public int markFailed(Long id, String error) {
    return outboxMapper.markFailed(id, error);
  }

  public int markDeadLetter(Long id, String error) {
    return outboxMapper.markDeadLetter(id, error);
  }

  public OutboxEntity findByEntity(String entityType, Long entityId, OutboxStatus status) {
    return outboxMapper.selectOne(
      new LambdaQueryWrapper<OutboxEntity>()
        .eq(OutboxEntity::getEntityType, entityType)
        .eq(OutboxEntity::getEntityId, entityId)
        .eq(OutboxEntity::getStatus, status.name())
    );
  }

  public void delete(OutboxEntity entity) {
    outboxMapper.deleteById(entity.getId());
  }
}
