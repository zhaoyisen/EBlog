package com.eblog.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eblog.api.common.ErrorCode;
import com.eblog.invite.InviteCodeService;
import com.eblog.user.UserEntity;
import com.eblog.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserMapper userMapper;

  @Mock
  private EmailCodeService emailCodeService;

  @Mock
  private InviteCodeService inviteCodeService;

  private AuthService authService;

  @BeforeEach
  void setup() {
    authService = new AuthService(userMapper, emailCodeService, inviteCodeService);
  }

  @Test
  void returnsDuplicateEmailWhenUserExists() {
    when(userMapper.selectOne(any())).thenReturn(new UserEntity());

    AuthService.RegisterResult result = authService.register(
        "test@example.com", "Password123", "INVITE", "123456", "1.1.1.1");

    assertEquals(ErrorCode.DUPLICATE_EMAIL, result.getError());
    verify(emailCodeService, never()).verifyRegisterCode(anyString(), anyString());
  }

  @Test
  void returnsEmailCodeInvalidWhenVerifyFails() {
    when(userMapper.selectOne(any())).thenReturn(null);
    when(emailCodeService.verifyRegisterCode(eq("test@example.com"), eq("123456"))).thenReturn(false);

    AuthService.RegisterResult result = authService.register(
        "Test@Example.com", "Password123", "INVITE", "123456", "1.1.1.1");

    assertEquals(ErrorCode.EMAIL_CODE_INVALID, result.getError());
    verify(userMapper, never()).insert(any(UserEntity.class));
    verify(inviteCodeService, never()).consume(anyString(), any(), anyString());
  }

  @Test
  void returnsInviteInvalidWhenConsumeFails() {
    when(userMapper.selectOne(any())).thenReturn(null);
    when(emailCodeService.verifyRegisterCode(eq("test@example.com"), eq("123456"))).thenReturn(true);
    when(userMapper.insert(any(UserEntity.class))).thenAnswer(invocation -> {
      UserEntity entity = invocation.getArgument(0);
      entity.setId(42L);
      return 1;
    });
    when(inviteCodeService.consume(eq("INVITE"), eq(42L), anyString())).thenReturn(false);

    AuthService.RegisterResult result = authService.register(
        "test@example.com", "Password123", "INVITE", "123456", "127.0.0.1");

    assertEquals(ErrorCode.INVITE_CODE_INVALID, result.getError());
  }

  @Test
  void hashesPasswordBeforeInsert() {
    when(userMapper.selectOne(any())).thenReturn(null);
    when(emailCodeService.verifyRegisterCode(eq("test@example.com"), eq("123456"))).thenReturn(true);
    when(inviteCodeService.consume(eq("INVITE"), eq(7L), anyString())).thenReturn(true);
    when(userMapper.insert(any(UserEntity.class))).thenAnswer(invocation -> {
      UserEntity entity = invocation.getArgument(0);
      entity.setId(7L);
      return 1;
    });

    AuthService.RegisterResult result = authService.register(
        "test@example.com", "Password123", "INVITE", "123456", "127.0.0.1");

    assertTrue(result.isSuccess());
    assertEquals(7L, result.getUserId());

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userMapper).insert(captor.capture());
    UserEntity saved = captor.getValue();
    assertNotNull(saved.getPasswordHash());
    assertNotEquals("Password123", saved.getPasswordHash());
    assertTrue(new BCryptPasswordEncoder().matches("Password123", saved.getPasswordHash()));
  }
}
