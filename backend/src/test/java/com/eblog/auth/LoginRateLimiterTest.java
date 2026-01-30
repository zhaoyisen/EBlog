package com.eblog.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoginRateLimiterTest {

  @Test
  void blocksAfterMaxAttemptsInWindow() {
    LoginRateLimiter limiter = new LoginRateLimiter();
    String key = "127.0.0.1:foo@example.com";
    for (int i = 0; i < 10; i++) {
      assertTrue(limiter.tryConsume(key));
    }
    assertFalse(limiter.tryConsume(key));
  }
}
