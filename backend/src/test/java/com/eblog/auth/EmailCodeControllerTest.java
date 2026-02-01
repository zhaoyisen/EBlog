package com.eblog.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmailCodeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmailCodeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private EmailCodeService emailCodeService;

  @MockBean
  private LoginRateLimiter rateLimiter;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void sendRegisterReturnsSuccessAndTriggersService() throws Exception {
    when(rateLimiter.tryConsume(anyString())).thenReturn(true);

    mockMvc.perform(post("/api/v1/auth/email-code/send-register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"test@example.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(emailCodeService).sendRegisterCode("test@example.com");
  }

  @Test
  void sendRegisterWhenRateLimitedReturnsFail() throws Exception {
    when(rateLimiter.tryConsume(anyString())).thenReturn(false);

    mockMvc.perform(post("/api/v1/auth/email-code/send-register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"test@example.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"));

    verify(emailCodeService, org.mockito.Mockito.never()).sendRegisterCode(anyString());
  }

  @Test
  void sendRegisterNormalizesEmailBeforeCallingService() throws Exception {
    when(rateLimiter.tryConsume(anyString())).thenReturn(true);

    mockMvc.perform(post("/api/v1/auth/email-code/send-register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"  TeSt@Example.COM  \"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(emailCodeService).sendRegisterCode("test@example.com");
  }

  @Test
  void sendRegisterSkipsServiceWhenEmailInvalid() throws Exception {
    when(rateLimiter.tryConsume(anyString())).thenReturn(true);

    mockMvc.perform(post("/api/v1/auth/email-code/send-register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"invalid\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(emailCodeService, org.mockito.Mockito.never()).sendRegisterCode(anyString());
  }
}
