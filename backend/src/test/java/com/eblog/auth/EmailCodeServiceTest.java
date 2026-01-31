package com.eblog.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmailCodeServiceTest {

  @Test
  void generatesSixDigits() {
    String code = EmailCodeService.generateSixDigitCode();
    assertEquals(6, code.length());
    assertTrue(code.matches("\\d{6}"));
  }

  @Test
  void logsOnlyInDev() {
    assertTrue(EmailCodeService.shouldLogRegisterCode("dev"));
    assertFalse(EmailCodeService.shouldLogRegisterCode("prod"));
    assertFalse(EmailCodeService.shouldLogRegisterCode(""));
  }
}
