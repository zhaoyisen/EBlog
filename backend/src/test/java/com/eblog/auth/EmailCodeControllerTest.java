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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EmailCodeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private EmailCodeService emailCodeService;

  @MockBean
  private LoginRateLimiter rateLimiter;

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
}
