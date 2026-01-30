package com.eblog.post;

import java.security.SecureRandom;

public final class SlugGenerator {
  private static final SecureRandom RANDOM = new SecureRandom();

  private SlugGenerator() {}

  public static String randomSlug() {
    // 10 bytes -> 20 hex chars. Prefix avoids empty/invalid slugs for non-latin titles.
    byte[] bytes = new byte[10];
    RANDOM.nextBytes(bytes);
    StringBuilder sb = new StringBuilder();
    sb.append("p-");
    for (byte b : bytes) {
      int v = b & 0xff;
      sb.append(Integer.toHexString((v >> 4) & 0xf));
      sb.append(Integer.toHexString(v & 0xf));
    }
    return sb.toString();
  }
}
