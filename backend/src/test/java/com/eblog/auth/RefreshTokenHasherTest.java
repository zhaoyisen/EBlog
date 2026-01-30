package com.eblog.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RefreshTokenHasherTest {

  @Test
  void sha256HexIsDeterministic() {
    String a = RefreshTokenHasher.sha256Hex("token");
    String b = RefreshTokenHasher.sha256Hex("token");
    assertEquals(a, b);
    assertTrue(a.length() == 64);
  }
}
