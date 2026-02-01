package com.eblog.moderation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("moderation_outbox")
public class OutboxEntity {
  private Long id;
  private String entityType;
  private Long entityId;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Integer attempts;
  private String lastError;
  private String deduplicationKey;
  private LocalDateTime deduplicationKeyUpdatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public Long getEntityId() {
    return entityId;
  }

  public void setEntityId(Long entityId) {
    this.entityId = entityId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Integer getAttempts() {
    return attempts;
  }

  public void setAttempts(Integer attempts) {
    this.attempts = attempts;
  }

  public String getLastError() {
    return lastError;
  }

  public void setLastError(String lastError) {
    this.lastError = lastError;
  }

  public String getDeduplicationKey() {
    return deduplicationKey;
  }

  public void setDeduplicationKey(String deduplicationKey) {
    this.deduplicationKey = deduplicationKey;
  }

  public LocalDateTime getDeduplicationKeyUpdatedAt() {
    return deduplicationKeyUpdatedAt;
  }

  public void setDeduplicationKeyUpdatedAt(LocalDateTime deduplicationKeyUpdatedAt) {
    this.deduplicationKeyUpdatedAt = deduplicationKeyUpdatedAt;
  }
}
