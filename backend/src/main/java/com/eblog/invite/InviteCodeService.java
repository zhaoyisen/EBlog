package com.eblog.invite;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteCodeService {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final InviteCodeMapper inviteCodeMapper;
  private final InviteCodeUseMapper inviteCodeUseMapper;

  public InviteCodeService(InviteCodeMapper inviteCodeMapper, InviteCodeUseMapper inviteCodeUseMapper) {
    this.inviteCodeMapper = inviteCodeMapper;
    this.inviteCodeUseMapper = inviteCodeUseMapper;
  }

  public List<InviteCodeEntity> listCodes(String status, int limit, int offset) {
    String normalized = (status == null || status.trim().isEmpty()) ? null : status.trim();
    return inviteCodeMapper.listCodes(normalized, limit, offset);
  }

  public List<InviteCodeUseEntity> listUses(Long inviteCodeId, int limit, int offset) {
    return inviteCodeUseMapper.listUses(inviteCodeId, limit, offset);
  }

  public InviteCodeEntity findByCode(String code) {
    if (code == null || code.trim().isEmpty()) {
      return null;
    }
    return inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCodeEntity>().eq(InviteCodeEntity::getCode, code));
  }

  public List<String> createCodes(int count, int maxUses, LocalDateTime expiresAt) {
    List<String> codes = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      codes.add(createOneCode(maxUses, expiresAt));
    }
    return codes;
  }

  public boolean revoke(String code) {
    if (code == null || code.trim().isEmpty()) {
      return false;
    }
    int updated = inviteCodeMapper.revokeByCode(code.trim(), LocalDateTime.now(ZoneOffset.UTC));
    return updated > 0;
  }

  @Transactional
  public boolean consume(String code, Long usedByUserId, String usedIp) {
    if (code == null || code.trim().isEmpty()) {
      return false;
    }
    String normalized = code.trim();
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    int updated = inviteCodeMapper.consumeByCode(normalized, now);
    if (updated <= 0) {
      return false;
    }
    InviteCodeEntity entity = inviteCodeMapper.selectOne(
        new LambdaQueryWrapper<InviteCodeEntity>().eq(InviteCodeEntity::getCode, normalized));
    if (entity == null) {
      return false;
    }
    InviteCodeUseEntity use = new InviteCodeUseEntity();
    use.setInviteCodeId(entity.getId());
    use.setUsedByUserId(usedByUserId);
    use.setUsedIp(usedIp);
    use.setUsedAt(now);
    inviteCodeUseMapper.insert(use);
    return true;
  }

  private String createOneCode(int maxUses, LocalDateTime expiresAt) {
    int attempts = 0;
    while (attempts < 5) {
      String code = randomCode();
      InviteCodeEntity entity = new InviteCodeEntity();
      entity.setCode(code);
      entity.setStatus("ACTIVE");
      entity.setMaxUses(maxUses);
      entity.setUsedCount(0);
      entity.setExpiresAt(expiresAt);
      entity.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
      entity.setRevokedAt(null);
      try {
        inviteCodeMapper.insert(entity);
        return code;
      } catch (Exception ex) {
        attempts++;
      }
    }
    throw new IllegalStateException("Failed to generate unique invite code");
  }

  private static String randomCode() {
    byte[] bytes = new byte[8];
    RANDOM.nextBytes(bytes);
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      int v = b & 0xff;
      sb.append(Integer.toHexString((v >> 4) & 0xf));
      sb.append(Integer.toHexString(v & 0xf));
    }
    return sb.toString();
  }
}
