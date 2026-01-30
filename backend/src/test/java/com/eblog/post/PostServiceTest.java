 package com.eblog.post;

 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.junit.jupiter.api.Assertions.assertNotNull;
 import static org.junit.jupiter.api.Assertions.assertNotEquals;
 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.eq;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;

 import com.eblog.api.common.ErrorCode;
 import com.eblog.moderation.OutboxService;
 import java.util.Collections;
 import org.junit.jupiter.api.AfterEach;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoExtension;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;

 @ExtendWith(MockitoExtension.class)
 class PostServiceTest {

   @Mock
   private PostMapper postMapper;

   @Mock
   private OutboxService outboxService;

   private PostService postService;

  @BeforeEach
  void setup() {
    postService = new PostService(postMapper, java.util.Optional.of(outboxService));
  }

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createReturnsUnauthorizedWithoutAuth() {
    PostService.CreateResult res = postService.create("t", null, "c", null, null, "DRAFT", "MARKDOWN");
    assertEquals(false, res.isSuccess());
    assertEquals(ErrorCode.UNAUTHORIZED, res.getError());
  }

  @Test
  void createGeneratesSlugAndSetsAuthorId() {
    setAuth("7", "USER");
    when(postMapper.insert(any(PostEntity.class))).thenAnswer(invocation -> {
      PostEntity e = invocation.getArgument(0);
      e.setId(99L);
      return 1;
    });

    PostService.CreateResult res = postService.create("Title", "s", "Content", "t1,t2", "cat", "PUBLISHED", "MARKDOWN");

    assertEquals(true, res.isSuccess());
    assertEquals(99L, res.getPostId());
    assertNotNull(res.getSlug());

    ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
    verify(postMapper).insert(captor.capture());
    PostEntity saved = captor.getValue();
    assertEquals(7L, saved.getAuthorId());
    assertNotNull(saved.getSlug());
    assertNotEquals("", saved.getSlug());
  }

  @Test
  void updateForbiddenForNonAuthor() {
    setAuth("7", "USER");
    PostEntity existing = new PostEntity();
    existing.setId(1L);
    existing.setAuthorId(8L);
    existing.setSlug("p-abc");
    when(postMapper.selectById(1L)).thenReturn(existing);

    ErrorCode err = postService.update(1L, "New", null, "NewContent", null, null, null);
    assertEquals(ErrorCode.FORBIDDEN, err);
    verify(postMapper, never()).updateById(any(PostEntity.class));
  }

  @Test
  void updateKeepsSlugStable() {
    setAuth("7", "USER");
    PostEntity existing = new PostEntity();
    existing.setId(1L);
    existing.setAuthorId(7L);
    existing.setSlug("p-stable");
    existing.setTitle("Old");
    existing.setContentMarkdown("OldC");
    when(postMapper.selectById(1L)).thenReturn(existing);

    ErrorCode err = postService.update(1L, "New", null, "NewC", null, null, null);
    assertEquals(null, err);

    ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
    verify(postMapper).updateById(captor.capture());
    assertEquals("p-stable", captor.getValue().getSlug());
  }

  @Test
  void archiveAllowedForAuthor() {
    setAuth("7", "USER");
    PostEntity existing = new PostEntity();
    existing.setId(1L);
    existing.setAuthorId(7L);
    when(postMapper.selectById(1L)).thenReturn(existing);

    ErrorCode err = postService.archive(1L);
    assertEquals(null, err);
    verify(postMapper).updateById(any(PostEntity.class));
  }

  @Test
  void listPublicUsesMapper() {
    when(postMapper.listPublic(eq(20), eq(0))).thenReturn(Collections.emptyList());
    assertNotNull(postService.listPublic(20, 0));
  }

  @Test
  void createMdxForbiddenForNonAdmin() {
    setAuth("7", "USER");
    PostService.CreateResult res = postService.create("Title", null, "# hi", null, null, "DRAFT", "MDX");
    assertEquals(false, res.isSuccess());
    assertEquals(ErrorCode.FORBIDDEN, res.getError());
    verify(postMapper, never()).insert(any(PostEntity.class));
  }

  @Test
  void createMdxAllowedForAdmin() {
    setAuth("7", "ADMIN");
    when(postMapper.insert(any(PostEntity.class))).thenAnswer(invocation -> {
      PostEntity e = invocation.getArgument(0);
      e.setId(1L);
      return 1;
    });
    PostService.CreateResult res = postService.create("Title", null, "<Callout>hi</Callout>", null, null, "DRAFT", "MDX");
    assertEquals(true, res.isSuccess());
  }

  @Test
  void updateToMdxForbiddenForNonAdmin() {
    setAuth("7", "USER");
    PostEntity existing = new PostEntity();
    existing.setId(1L);
    existing.setAuthorId(7L);
    existing.setSlug("p-stable");
    when(postMapper.selectById(1L)).thenReturn(existing);
    when(postMapper.updateById(any(PostEntity.class))).thenReturn(1);

    ErrorCode err = postService.update(1L, "New", null, "NewC", null, null, null, "MDX");
    assertEquals(ErrorCode.FORBIDDEN, err);
  }

  private static void setAuth(String userId, String role) {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userId, null, java.util.Collections.singletonList(new SimpleGrantedAuthority(role)));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}
