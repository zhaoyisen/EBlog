package com.eblog.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.api.common.ErrorCode;
import com.eblog.invite.InviteCodeService;
import com.eblog.user.UserEntity;
import com.eblog.user.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AuthService {

  private final UserMapper userMapper;
  private final EmailCodeService emailCodeService;
  private final InviteCodeService inviteCodeService;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AuthService(UserMapper userMapper, EmailCodeService emailCodeService, InviteCodeService inviteCodeService) {
    this.userMapper = userMapper;
    this.emailCodeService = emailCodeService;
    this.inviteCodeService = inviteCodeService;
  }

  @Transactional
  public RegisterResult register(String email, String password, String inviteCode, String emailCode, String ip) {
    if (isBlank(email) || isBlank(password) || isBlank(inviteCode) || isBlank(emailCode)) {
      return RegisterResult.error(ErrorCode.BAD_REQUEST);
    }

    String normalizedEmail = email.trim().toLowerCase();
    UserEntity existing = userMapper.selectOne(
        new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getEmail, normalizedEmail));
    if (existing != null) {
      return RegisterResult.error(ErrorCode.DUPLICATE_EMAIL);
    }

    boolean emailOk = emailCodeService.verifyRegisterCode(normalizedEmail, emailCode.trim());
    if (!emailOk) {
      return RegisterResult.error(ErrorCode.EMAIL_CODE_INVALID);
    }

    UserEntity user = new UserEntity();
    user.setEmail(normalizedEmail);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setRole("USER");
    try {
      userMapper.insert(user);
    } catch (DuplicateKeyException ex) {
      markRollbackIfActive();
      return RegisterResult.error(ErrorCode.DUPLICATE_EMAIL);
    }

    boolean inviteOk = inviteCodeService.consume(inviteCode.trim(), user.getId(), ip);
    if (!inviteOk) {
      markRollbackIfActive();
      return RegisterResult.error(ErrorCode.INVITE_CODE_INVALID);
    }

    return RegisterResult.success(user.getId());
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private static void markRollbackIfActive() {
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
  }

  public static class RegisterResult {
    private final ErrorCode error;
    private final Long userId;

    private RegisterResult(ErrorCode error, Long userId) {
      this.error = error;
      this.userId = userId;
    }

    public static RegisterResult success(Long userId) {
      return new RegisterResult(null, userId);
    }

    public static RegisterResult error(ErrorCode error) {
      return new RegisterResult(error, null);
    }

    public boolean isSuccess() {
      return error == null;
    }

    public ErrorCode getError() {
      return error;
    }

    public Long getUserId() {
      return userId;
    }
  }
}
