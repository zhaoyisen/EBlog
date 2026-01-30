package com.eblog.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("password_reset_tokens")
public class PasswordResetTokenEntity {
  private Long id;
  private Long userId;
  private String tokenHash;
  private String requestIp;
  private String requestUa;
  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;
  private LocalDateTime usedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String tokenHash) {
    this.tokenHash = tokenHash;
  }

  public String getRequestIp() {
    return requestIp;
  }

  public void setRequestIp(String requestIp) {
    this.requestIp = requestIp;
  }

  public String getRequestUa() {
    return requestUa;
  }

  public void setRequestUa(String requestUa) {
    this.requestUa = requestUa;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public LocalDateTime getUsedAt() {
    return usedAt;
  }

  public void setUsedAt(LocalDateTime usedAt) {
    this.usedAt = usedAt;
  }
}
