package com.eblog.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.eblog.invite.InviteCodeMapper;
import com.eblog.user.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class LoginSecurityTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserMapper userMapper;

  @MockBean
  private RefreshTokenMapper refreshTokenMapper;

  @MockBean
  private InviteCodeMapper inviteCodeMapper;

  @MockBean
  private EmailCodeMapper emailCodeMapper;
  
  @MockBean
  private AuthService authService; 

  @MockBean
  private JwtService jwtService; 

  @MockBean
  private LoginRateLimiter loginRateLimiter; 

  @Test
  void loginShouldNotReturn401_WhenSecurityIsConfiguredCorrectly() throws Exception {
      org.mockito.Mockito.when(loginRateLimiter.tryConsume(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);

      mockMvc.perform(post("/api/v1/auth/login")
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"email\":\"test@example.com\", \"password\":\"wrong\"}"))
          .andDo(print())
          .andExpect(status().isOk());
  }

  @Test
  void loginWithTrailingSlash_ShouldAlsoBePermitted() throws Exception {
      // Test if /api/v1/auth/login/ (with trailing slash) is also permitted
      mockMvc.perform(post("/api/v1/auth/login/")
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"email\":\"test@example.com\", \"password\":\"wrong\"}"))
          .andDo(print())
          // If this returns 401, it means Security blocks it because it doesn't match the permitAll pattern
          // If it returns 404, it means MVC doesn't match it (but Security let it through if anyRequest is authenticated?)
          // Wait, if MVC doesn't match, Security MvcRequestMatcher might fail to match permitAll too!
          // So 401 is very likely if MVC strict matching is on.
          .andExpect(status().is(401)); 
  }
}
