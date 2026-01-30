package com.eblog.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

  @Mock
  private UserMapper userMapper;

  private AuthorController controller;

  @BeforeEach
  void setup() {
    controller = new AuthorController(userMapper);
  }

  @Test
  void returnsNotFoundWhenMissing() {
    when(userMapper.selectById(123L)).thenReturn(null);

    ApiResponse<AuthorController.AuthorView> res = controller.getAuthor(123L);

    assertEquals(false, res.isSuccess());
    assertNotNull(res.getError());
    assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), res.getError().getCode());
    assertNull(res.getData());
  }

  @Test
  void returnsPublicAuthorViewWithoutEmail() {
    UserEntity user = new UserEntity();
    user.setId(7L);
    user.setEmail("secret@example.com");
    user.setNickname("Atlas");
    user.setAvatarUrl("https://example.com/a.png");
    user.setBio("bio");
    user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
    when(userMapper.selectById(7L)).thenReturn(user);

    ApiResponse<AuthorController.AuthorView> res = controller.getAuthor(7L);

    assertEquals(true, res.isSuccess());
    assertNotNull(res.getData());
    assertEquals(7L, res.getData().id);
    assertEquals("Atlas", res.getData().nickname);
    assertEquals("https://example.com/a.png", res.getData().avatarUrl);
    assertEquals("bio", res.getData().bio);
  }
}
