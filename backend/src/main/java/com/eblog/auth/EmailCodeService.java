package com.eblog.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailCodeService {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Logger log = LoggerFactory.getLogger(EmailCodeService.class);

  private final EmailCodeMapper emailCodeMapper;
  private final JavaMailSender mailSender;
  private final String mailFrom;
  private final long codeTtlSeconds;
  private final String appEnv;

  public EmailCodeService(
      EmailCodeMapper emailCodeMapper,
      JavaMailSender mailSender,
      @Value("${app.mail.from}") String mailFrom,
      @Value("${app.env}") String appEnv,
      @Value("${app.email-code.ttl-seconds}") long codeTtlSeconds) {
    this.emailCodeMapper = emailCodeMapper;
    this.mailSender = mailSender;
    this.mailFrom = mailFrom;
    this.appEnv = appEnv;
    this.codeTtlSeconds = codeTtlSeconds;
  }

  public void sendRegisterCode(String email) {
    String code = generateSixDigitCode();
    if (shouldLogRegisterCode(appEnv)) {
      log.info("register email code: email={}, code={}", email, code);
    }
    String hash = RefreshTokenHasher.sha256Hex(code);

    EmailCodeEntity entity = new EmailCodeEntity();
    entity.setEmail(email);
    entity.setPurpose("REGISTER");
    entity.setCodeHash(hash);
    entity.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
    entity.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(codeTtlSeconds));
    emailCodeMapper.insert(entity);

    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(email);
    if (mailFrom != null && !mailFrom.trim().isEmpty()) {
      msg.setFrom(mailFrom);
    }
    msg.setSubject("EBlog 注册验证码");
    msg.setText("你的验证码是：" + code + "\n\n有效期：" + (codeTtlSeconds / 60) + " 分钟\n\n如果不是你本人操作，请忽略此邮件。\n");
    mailSender.send(msg);
  }

  public boolean verifyRegisterCode(String email, String code) {
    String hash = RefreshTokenHasher.sha256Hex(code);
    EmailCodeEntity entity = emailCodeMapper.selectOne(
        new LambdaQueryWrapper<EmailCodeEntity>()
            .eq(EmailCodeEntity::getEmail, email)
            .eq(EmailCodeEntity::getPurpose, "REGISTER")
            .eq(EmailCodeEntity::getCodeHash, hash)
            .isNull(EmailCodeEntity::getUsedAt)
            .gt(EmailCodeEntity::getExpiresAt, LocalDateTime.now(ZoneOffset.UTC))
            .orderByDesc(EmailCodeEntity::getId)
            .last("LIMIT 1"));
    if (entity == null) {
      return false;
    }
    entity.setUsedAt(LocalDateTime.now(ZoneOffset.UTC));
    emailCodeMapper.updateById(entity);
    return true;
  }

  static String generateSixDigitCode() {
    int n = RANDOM.nextInt(1000000);
    return String.format("%06d", n);
  }

  static boolean shouldLogRegisterCode(String appEnv) {
    return appEnv != null && "dev".equalsIgnoreCase(appEnv.trim());
  }
}
