package com.eblog.auth;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenGenerator {
  private static final SecureRandom RANDOM = new SecureRandom();

  public static String randomToken() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
