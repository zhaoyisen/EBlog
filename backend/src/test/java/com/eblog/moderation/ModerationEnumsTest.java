 package com.eblog.moderation;

 import com.eblog.moderation.enums.OutboxStatus;
 import com.eblog.moderation.enums.AuditAction;
 import com.eblog.moderation.enums.ModerationStatus;
 import org.junit.jupiter.api.Test;
 import static org.junit.jupiter.api.Assertions.*;

class OutboxStatusTest {

  @Test
  void testEnumValues() {
    OutboxStatus[] values = OutboxStatus.values();
    assertEquals(5, values.length);
  }

  @Test
  void testEnumNames() {
    assertEquals("PENDING", OutboxStatus.PENDING.name());
    assertEquals("PROCESSING", OutboxStatus.PROCESSING.name());
    assertEquals("COMPLETED", OutboxStatus.COMPLETED.name());
    assertEquals("FAILED", OutboxStatus.FAILED.name());
    assertEquals("DEAD_LETTER", OutboxStatus.DEAD_LETTER.name());
  }
}

class AuditActionTest {

  @Test
  void testEnumValues() {
    AuditAction[] values = AuditAction.values();
    assertEquals(4, values.length);
  }

  @Test
  void testEnumNames() {
    assertEquals("APPROVE", AuditAction.APPROVE.name());
    assertEquals("REJECT", AuditAction.REJECT.name());
    assertEquals("REQUEST_REVIEW", AuditAction.REQUEST_REVIEW.name());
    assertEquals("RULE_REJECT", AuditAction.RULE_REJECT.name());
  }
}

class ModerationStatusTest {

  @Test
  void testEnumValues() {
    ModerationStatus[] values = ModerationStatus.values();
    assertEquals(4, values.length);
  }

  @Test
  void testEnumNames() {
    assertEquals("PENDING", ModerationStatus.PENDING.name());
    assertEquals("APPROVED", ModerationStatus.APPROVED.name());
    assertEquals("REJECTED", ModerationStatus.REJECTED.name());
    assertEquals("NEEDS_REVIEW", ModerationStatus.NEEDS_REVIEW.name());
  }
}
