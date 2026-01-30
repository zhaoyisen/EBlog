package com.eblog.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  @Test
  void createsAndVerifiesAccessToken() {
    JwtService jwtService = new JwtService("test-secret", "eblog", 60);
    String token = jwtService.createAccessToken(123L, "USER");
    assertNotNull(token);

    DecodedJWT decoded = jwtService.verify(token);
    assertEquals("123", decoded.getSubject());
    assertEquals("USER", decoded.getClaim("role").asString());
  }
}
