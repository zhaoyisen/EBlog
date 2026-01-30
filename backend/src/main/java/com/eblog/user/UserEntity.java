package com.eblog.user;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("users")
public class UserEntity {
  private Long id;
  private String email;
  private String passwordHash;
  private String nickname;
  private String avatarUrl;
  private String bio;
  private String role;
  private Boolean isBanned;
  private String bannedReason;
  private LocalDateTime bannedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
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

  public Boolean getIsBanned() {
    return isBanned;
  }

  public void setIsBanned(Boolean isBanned) {
    this.isBanned = isBanned;
  }

  public String getBannedReason() {
    return bannedReason;
  }

  public void setBannedReason(String bannedReason) {
    this.bannedReason = bannedReason;
  }

  public LocalDateTime getBannedAt() {
    return bannedAt;
  }

  public void setBannedAt(LocalDateTime bannedAt) {
    this.bannedAt = bannedAt;
  }
}
