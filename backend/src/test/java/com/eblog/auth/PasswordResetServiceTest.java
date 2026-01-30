package com.eblog.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eblog.api.common.ErrorCode;
import com.eblog.user.UserEntity;
import com.eblog.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordResetTokenMapper tokenMapper;

  @Mock
  private RefreshTokenMapper refreshTokenMapper;

  @Mock
  private PasswordResetRateLimiter rateLimiter;

  @Mock
  private JavaMailSender mailSender;

  private PasswordResetService passwordResetService;

  @BeforeEach
  void setup() {
    passwordResetService = new PasswordResetService(
        userMapper,
        tokenMapper,
        refreshTokenMapper,
        rateLimiter,
        mailSender,
        "noreply@example.com",
        1800);
  }

  @Test
  void requestResetDoesNothingWhenRateLimited() {
    when(rateLimiter.tryConsume(anyString())).thenReturn(false);

    passwordResetService.requestReset("user@example.com", "1.1.1.1", "UA");

    verify(userMapper, never()).selectOne(any());
    verify(tokenMapper, never()).insert(any(PasswordResetTokenEntity.class));
    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void requestResetDoesNothingWhenUserNotFound() {
    when(rateLimiter.tryConsume(anyString())).thenReturn(true);
    when(userMapper.selectOne(any())).thenReturn(null);

    passwordResetService.requestReset("user@example.com", "1.1.1.1", "UA");

    verify(tokenMapper, never()).insert(any(PasswordResetTokenEntity.class));
    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void requestResetStoresHashAndSendsTokenInEmail() {
    when(rateLimiter.tryConsume(anyString())).thenReturn(true);
    UserEntity user = new UserEntity();
    user.setId(7L);
    user.setEmail("user@example.com");
    when(userMapper.selectOne(any())).thenReturn(user);

    passwordResetService.requestReset("User@Example.com", "1.1.1.1", "UA");

    ArgumentCaptor<PasswordResetTokenEntity> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
    verify(tokenMapper).insert(tokenCaptor.capture());
    PasswordResetTokenEntity saved = tokenCaptor.getValue();
    assertEquals(7L, saved.getUserId());
    assertNotNull(saved.getTokenHash());
    assertEquals(64, saved.getTokenHash().length());

    ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(mailCaptor.capture());
    SimpleMailMessage msg = mailCaptor.getValue();
    assertEquals("EBlog 重置密码", msg.getSubject());
    String text = msg.getText();
    assertNotNull(text);

    String token = extractToken(text);
    assertNotNull(token);
    assertEquals(saved.getTokenHash(), RefreshTokenHasher.sha256Hex(token));
  }

  @Test
  void resetPasswordReturnsInvalidWhenTokenNotFound() {
    when(tokenMapper.selectOne(any())).thenReturn(null);

    ErrorCode error = passwordResetService.resetPassword(
        "user@example.com", "token", "NewPassword123");

    assertEquals(ErrorCode.PASSWORD_RESET_TOKEN_INVALID, error);
    verify(userMapper, never()).updateById(any(UserEntity.class));
    verify(refreshTokenMapper, never()).revokeAllByUserId(anyLong(), any());
  }

  @Test
  void resetPasswordUpdatesPasswordAndRevokesAllRefreshTokens() {
    PasswordResetTokenEntity reset = new PasswordResetTokenEntity();
    reset.setId(1L);
    reset.setUserId(2L);
    when(tokenMapper.selectOne(any())).thenReturn(reset);

    UserEntity user = new UserEntity();
    user.setId(2L);
    user.setEmail("user@example.com");
    when(userMapper.selectById(2L)).thenReturn(user);

    when(tokenMapper.markUsed(eq(1L), any())).thenReturn(1);

    ErrorCode error = passwordResetService.resetPassword(
        "user@example.com", "the-token", "NewPassword123");

    assertEquals(null, error);

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userMapper).updateById(captor.capture());
    UserEntity saved = captor.getValue();
    assertNotNull(saved.getPasswordHash());
    assertNotEquals("NewPassword123", saved.getPasswordHash());
    assertEquals(true, new BCryptPasswordEncoder().matches("NewPassword123", saved.getPasswordHash()));

    verify(refreshTokenMapper).revokeAllByUserId(eq(2L), any());
  }

  private static String extractToken(String text) {
    String marker = "重置令牌：";
    int idx = text.indexOf(marker);
    if (idx < 0) {
      return null;
    }
    int start = idx + marker.length();
    int end = text.indexOf('\n', start);
    if (end < 0) {
      end = text.length();
    }
    String token = text.substring(start, end).trim();
    return token.isEmpty() ? null : token;
  }
}
