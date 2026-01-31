package com.eblog.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.eblog.invite.InviteCodeEntity;
import com.eblog.invite.InviteCodeMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnabledIfSystemProperty(named = "eblog.testcontainers", matches = "true")
class RegisterIntegrationTest {

  @Container
  static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("eblog")
      .withUsername("eblog")
      .withPassword("eblog");

  @DynamicPropertySource
  static void mysqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
    registry.add("spring.flyway.enabled", () -> "true");
    registry.add("spring.mail.host", () -> "localhost");
    registry.add("spring.mail.port", () -> "2525");
    registry.add("spring.mail.username", () -> "");
    registry.add("spring.mail.password", () -> "");
    registry.add("app.jwt.secret", () -> "test-secret");
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private EmailCodeMapper emailCodeMapper;

  @Autowired
  private InviteCodeMapper inviteCodeMapper;

  @Test
  void registerThenLoginHappyPath() {
    String email = "user" + UUID.randomUUID() + "@example.com";
    String password = "Password123";
    String inviteCode = "invite-" + UUID.randomUUID();
    String emailCode = "123456";

    InviteCodeEntity invite = new InviteCodeEntity();
    invite.setCode(inviteCode);
    invite.setStatus("ACTIVE");
    invite.setMaxUses(1);
    invite.setUsedCount(0);
    invite.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
    inviteCodeMapper.insert(invite);

    EmailCodeEntity codeEntity = new EmailCodeEntity();
    codeEntity.setEmail(email);
    codeEntity.setPurpose("REGISTER");
    codeEntity.setCodeHash(RefreshTokenHasher.sha256Hex(emailCode));
    codeEntity.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
    codeEntity.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(10));
    emailCodeMapper.insert(codeEntity);

    Map<String, Object> registerBody = new HashMap<>();
    registerBody.put("email", email);
    registerBody.put("password", password);
    registerBody.put("inviteCode", inviteCode);
    registerBody.put("emailCode", emailCode);

    ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
        "/api/v1/auth/register", registerBody, Map.class);
    assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
    Map<String, Object> registerPayload = registerResponse.getBody();
    assertNotNull(registerPayload);
    assertTrue(Boolean.TRUE.equals(registerPayload.get("success")));
    Map<String, Object> registerData = (Map<String, Object>) registerPayload.get("data");
    assertNotNull(registerData);
    assertNotNull(registerData.get("userId"));

    Map<String, Object> loginBody = new HashMap<>();
    loginBody.put("email", email);
    loginBody.put("password", password);

    ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
        "/api/v1/auth/login", loginBody, Map.class);
    assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    Map<String, Object> loginPayload = loginResponse.getBody();
    assertNotNull(loginPayload);
    assertTrue(Boolean.TRUE.equals(loginPayload.get("success")));
    Map<String, Object> loginData = (Map<String, Object>) loginPayload.get("data");
    assertNotNull(loginData);
    assertNotNull(loginData.get("accessToken"));
  }
}
