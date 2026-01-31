package com.eblog.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.api.common.ErrorCode;
import com.eblog.user.UserEntity;
import com.eblog.user.UserMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {
  private final UserMapper userMapper;
  private final PasswordResetTokenMapper tokenMapper;
  private final RefreshTokenMapper refreshTokenMapper;
  private final PasswordResetRateLimiter rateLimiter;
  private final JavaMailSender mailSender;
  private final String mailFrom;
  private final long ttlSeconds;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public PasswordResetService(
      UserMapper userMapper,
      PasswordResetTokenMapper tokenMapper,
      RefreshTokenMapper refreshTokenMapper,
      PasswordResetRateLimiter rateLimiter,
      JavaMailSender mailSender,
      @Value("${app.mail.from}") String mailFrom,
      @Value("${app.password-reset.ttl-seconds}") long ttlSeconds) {
    this.userMapper = userMapper;
    this.tokenMapper = tokenMapper;
    this.refreshTokenMapper = refreshTokenMapper;
    this.rateLimiter = rateLimiter;
    this.mailSender = mailSender;
    this.mailFrom = mailFrom;
    this.ttlSeconds = ttlSeconds;
  }

  public void requestReset(String email, String ip, String ua) {
    if (email == null || email.trim().isEmpty()) {
      return;
    }

    String normalizedEmail = email.trim().toLowerCase();
    String key = (ip == null ? "" : ip) + ":" + normalizedEmail;
    if (!rateLimiter.tryConsume(key)) {
      return;
    }

    UserEntity user = userMapper.selectOne(
        new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getEmail, normalizedEmail));
    if (user == null) {
      return;
    }

    String rawToken = TokenGenerator.randomToken();
    String tokenHash = RefreshTokenHasher.sha256Hex(rawToken);
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

    PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
    entity.setUserId(user.getId());
    entity.setTokenHash(tokenHash);
    entity.setRequestIp(ip);
    entity.setRequestUa(ua);
    entity.setCreatedAt(now);
    entity.setExpiresAt(now.plusSeconds(ttlSeconds));
    entity.setUsedAt(null);
    try {
      tokenMapper.insert(entity);
      SimpleMailMessage msg = new SimpleMailMessage();
      msg.setTo(normalizedEmail);
      if (mailFrom != null && !mailFrom.trim().isEmpty()) {
        msg.setFrom(mailFrom);
      }
      msg.setSubject("EBlog 重置密码");
      msg.setText("你正在重置 EBlog 账号密码。\n\n" +
          "重置令牌：" + rawToken + "\n\n" +
          "有效期：" + (ttlSeconds / 60) + " 分钟\n\n" +
          "如果不是你本人操作，请忽略此邮件。\n");
      mailSender.send(msg);
    } catch (Exception ex) {
      // best-effort; always act as if request was accepted
    }
  }

  @Transactional
  public ErrorCode resetPassword(String email, String token, String newPassword) {
    if (isBlank(email) || isBlank(token) || isBlank(newPassword)) {
      return ErrorCode.BAD_REQUEST;
    }
    if (newPassword.trim().length() < 8) {
      return ErrorCode.BAD_REQUEST;
    }

    String normalizedEmail = email.trim().toLowerCase();
    String tokenHash = RefreshTokenHasher.sha256Hex(token.trim());
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

    PasswordResetTokenEntity reset = tokenMapper.selectOne(
        new LambdaQueryWrapper<PasswordResetTokenEntity>()
            .eq(PasswordResetTokenEntity::getTokenHash, tokenHash)
            .isNull(PasswordResetTokenEntity::getUsedAt)
            .gt(PasswordResetTokenEntity::getExpiresAt, now)
            .orderByDesc(PasswordResetTokenEntity::getId)
            .last("LIMIT 1"));
    if (reset == null) {
      return ErrorCode.PASSWORD_RESET_TOKEN_INVALID;
    }

    UserEntity user = userMapper.selectById(reset.getUserId());
    if (user == null || user.getEmail() == null || !normalizedEmail.equals(user.getEmail())) {
      return ErrorCode.PASSWORD_RESET_TOKEN_INVALID;
    }

    int marked = tokenMapper.markUsed(reset.getId(), now);
    if (marked <= 0) {
      return ErrorCode.PASSWORD_RESET_TOKEN_INVALID;
    }

    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userMapper.updateById(user);

    refreshTokenMapper.revokeAllByUserId(user.getId(), now);
    return null;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
