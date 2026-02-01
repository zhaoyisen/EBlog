package com.eblog.moderation;

import com.eblog.moderation.entity.OutboxEntity;
import com.eblog.moderation.enums.OutboxStatus;
import com.eblog.moderation.mapper.OutboxMapper;
import com.eblog.moderation.mapper.AuditLogMapper;
import com.eblog.post.PostMapper;
import com.eblog.comment.mapper.CommentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxStabilityTest {

    @Mock private OutboxMapper outboxMapper;
    @Mock private RuleEngine ruleEngine;
    @Mock private AuditLogMapper auditLogMapper;
    @Mock private PostMapper postMapper;
    @Mock private CommentMapper commentMapper;
    @Mock private ModerationService moderationService;

    private OutboxService outboxService;
    private WorkerService workerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        outboxService = new OutboxService(outboxMapper);
        workerService = new WorkerService(outboxService, ruleEngine, auditLogMapper, postMapper, commentMapper, moderationService);
    }

    @Test
    void testEnqueueIdempotency() {
        // Given: an existing PENDING task with same key
        when(outboxMapper.selectOne(any())).thenReturn(new OutboxEntity());

        // When: trying to enqueue again
        outboxService.enqueue("POST", 1L, "key1");

        // Then: insert should not be called
        verify(outboxMapper, never()).insert(any(OutboxEntity.class));
    }

    @Test
    void testEnqueueNewTask() {
        // Given: no existing task
        when(outboxMapper.selectOne(any())).thenReturn(null);

        // When: enqueuing
        outboxService.enqueue("POST", 1L, "key1");

        // Then: insert should be called with correct fields
        ArgumentCaptor<OutboxEntity> captor = ArgumentCaptor.forClass(OutboxEntity.class);
        verify(outboxMapper).insert(captor.capture());
        OutboxEntity saved = captor.getValue();
        assertEquals("POST", saved.getEntityType());
        assertEquals(1L, saved.getEntityId());
        assertEquals("key1", saved.getDeduplicationKey());
        assertEquals(OutboxStatus.PENDING.name(), saved.getStatus());
    }

    @Test
    void testWorkerFailureRetry() {
        // Given: a task that fails on its 1st attempt
        OutboxEntity task = new OutboxEntity();
        task.setId(10L);
        task.setEntityType("POST");
        task.setEntityId(100L);
        task.setAttempts(0);

        when(outboxMapper.lockPendingTasks(anyInt())).thenReturn(Collections.singletonList(task));
        // Simulate failure in processPost (via postMapper)
        when(postMapper.selectById(anyLong())).thenThrow(new RuntimeException("DB Error"));

        // When: processing outbox
        workerService.processOutbox();

        // Then: markFailed should be called (since attempts < 3)
        verify(outboxMapper).markFailed(eq(10L), contains("DB Error"));
        verify(outboxMapper, never()).markDeadLetter(anyLong(), anyString());
    }

    @Test
    void testWorkerDeadLetter() {
        // Given: a task that fails on its 3rd attempt (attempts=2)
        OutboxEntity task = new OutboxEntity();
        task.setId(10L);
        task.setEntityType("POST");
        task.setEntityId(100L);
        task.setAttempts(2);

        when(outboxMapper.lockPendingTasks(anyInt())).thenReturn(Collections.singletonList(task));
        when(postMapper.selectById(anyLong())).thenThrow(new RuntimeException("Fatal Error"));

        // When: processing outbox
        workerService.processOutbox();

        // Then: markDeadLetter should be called
        verify(outboxMapper).markDeadLetter(eq(10L), contains("Fatal Error"));
        verify(outboxMapper, never()).markFailed(anyLong(), anyString());
    }
}
