package com.eblog.invite;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("invite_code_uses")
public class InviteCodeUseEntity {
  private Long id;
  private Long inviteCodeId;
  private Long usedByUserId;
  private String usedIp;
  private LocalDateTime usedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getInviteCodeId() {
    return inviteCodeId;
  }

  public void setInviteCodeId(Long inviteCodeId) {
    this.inviteCodeId = inviteCodeId;
  }

  public Long getUsedByUserId() {
    return usedByUserId;
  }

  public void setUsedByUserId(Long usedByUserId) {
    this.usedByUserId = usedByUserId;
  }

  public String getUsedIp() {
    return usedIp;
  }

  public void setUsedIp(String usedIp) {
    this.usedIp = usedIp;
  }

  public LocalDateTime getUsedAt() {
    return usedAt;
  }

  public void setUsedAt(LocalDateTime usedAt) {
    this.usedAt = usedAt;
  }
}
