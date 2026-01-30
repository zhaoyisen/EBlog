package com.eblog.invite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InviteCodeServiceTest {

  @Mock
  private InviteCodeMapper inviteCodeMapper;

  @Mock
  private InviteCodeUseMapper inviteCodeUseMapper;

  private InviteCodeService inviteCodeService;

  @BeforeEach
  void setup() {
    inviteCodeService = new InviteCodeService(inviteCodeMapper, inviteCodeUseMapper);
  }

  @Test
  void consumeReturnsFalseWhenUpdateFails() {
    when(inviteCodeMapper.consumeByCode(eq("CODE"), any())).thenReturn(0);

    boolean result = inviteCodeService.consume("CODE", 1L, "127.0.0.1");

    assertFalse(result);
    verify(inviteCodeUseMapper, never()).insert(any(InviteCodeUseEntity.class));
  }

  @Test
  void consumeRecordsUseWhenUpdateSucceeds() {
    InviteCodeEntity entity = new InviteCodeEntity();
    entity.setId(99L);
    entity.setCode("CODE");
    when(inviteCodeMapper.consumeByCode(eq("CODE"), any())).thenReturn(1);
    when(inviteCodeMapper.selectOne(any())).thenReturn(entity);
    when(inviteCodeUseMapper.insert(any(InviteCodeUseEntity.class))).thenReturn(1);

    boolean result = inviteCodeService.consume("CODE", null, "10.0.0.1");

    assertTrue(result);
    ArgumentCaptor<InviteCodeUseEntity> captor = ArgumentCaptor.forClass(InviteCodeUseEntity.class);
    verify(inviteCodeUseMapper).insert(captor.capture());
    InviteCodeUseEntity use = captor.getValue();
    assertEquals(99L, use.getInviteCodeId());
    assertNull(use.getUsedByUserId());
    assertEquals("10.0.0.1", use.getUsedIp());
    assertNotNull(use.getUsedAt());
  }

  @Test
  void revokeReturnsTrueWhenUpdated() {
    when(inviteCodeMapper.revokeByCode(eq("CODE"), any())).thenReturn(1);
    assertTrue(inviteCodeService.revoke("CODE"));
  }

  @Test
  void revokeReturnsFalseWhenNoRowUpdated() {
    when(inviteCodeMapper.revokeByCode(eq("CODE"), any())).thenReturn(0);
    assertFalse(inviteCodeService.revoke("CODE"));
  }
}
