package com.eblog.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

  private final Algorithm algorithm;
  private final JWTVerifier verifier;
  private final String issuer;
  private final long accessTtlSeconds;

  public JwtService(
      @Value("${JWT_SECRET:}") String secret,
      @Value("${JWT_ISSUER:eblog}") String issuer,
      @Value("${JWT_ACCESS_TTL_SECONDS:900}") long accessTtlSeconds) {
    if (secret == null || secret.trim().isEmpty()) {
      // Dev-friendly: allow boot without secret; runtime should set it.
      secret = "dev-only-secret-change-me";
    }
    this.algorithm = Algorithm.HMAC256(secret);
    this.verifier = JWT.require(this.algorithm).withIssuer(issuer).build();
    this.issuer = issuer;
    this.accessTtlSeconds = accessTtlSeconds;
  }

  public String createAccessToken(long userId, String role) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(accessTtlSeconds);
    return JWT.create()
        .withIssuer(issuer)
        .withSubject(String.valueOf(userId))
        .withClaim("role", role)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(exp))
        .sign(algorithm);
  }

  public DecodedJWT verify(String token) throws JWTVerificationException {
    return verifier.verify(token);
  }
}
