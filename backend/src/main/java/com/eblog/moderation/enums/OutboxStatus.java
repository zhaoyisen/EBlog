package com.eblog.moderation.enums;

public enum OutboxStatus {
  PENDING,
  PROCESSING,
  COMPLETED,
  FAILED,
  DEAD_LETTER
}
