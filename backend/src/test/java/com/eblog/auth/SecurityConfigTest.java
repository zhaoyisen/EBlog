package com.eblog.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
      "spring.datasource.url=jdbc:h2:mem:eblog;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password="
    })
@AutoConfigureMockMvc
class SecurityConfigTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void refreshDoesNotTriggerBasicAuthPrompt() throws Exception {
    mockMvc.perform(post("/api/v1/auth/refresh"))
        .andExpect(status().isOk()) // Returns 200 with ApiResponse.fail
        .andExpect(header().doesNotExist("WWW-Authenticate"));
  }

  @Test
  void refreshIsExcludedFromCsrf() throws Exception {
    mockMvc.perform(post("/api/v1/auth/refresh"))
        .andExpect(status().isOk()); // Should be OK even without CSRF token
  }

  @Test
  void logoutIsExcludedFromCsrf() throws Exception {
    mockMvc.perform(post("/api/v1/auth/logout"))
        .andExpect(status().isOk()); // Should be OK even without CSRF token
  }

  @Test
  void loginRequiresCsrfToken() throws Exception {
    mockMvc.perform(post("/api/v1/auth/login"))
        .andExpect(status().isForbidden()); // 403 Forbidden due to missing CSRF
  }

  @Test
  void csrfEndpointSetsCookie() throws Exception {
    mockMvc.perform(get("/api/v1/auth/csrf"))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("XSRF-TOKEN"))
        .andExpect(cookie().path("XSRF-TOKEN", "/"));
  }
}
