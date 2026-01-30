package com.eblog.post;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.eblog.api.common.ApiResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

  @Mock
  private PostService postService;

  private SearchController controller;

  @BeforeEach
  void setup() {
    controller = new SearchController(postService);
  }

  @Test
  void searchesByTitleAndTagAndAuthor() {
    PostEntity a = new PostEntity();
    a.setId(1L);
    a.setAuthorId(10L);
    a.setTitle("Java Streams");
    a.setSlug("p-1");
    a.setSummary("intro");
    a.setTagsCsv("java,backend");
    a.setCategory("Tech");
    a.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

    PostEntity b = new PostEntity();
    b.setId(2L);
    b.setAuthorId(11L);
    b.setTitle("Cooking");
    b.setSlug("p-2");
    b.setSummary("food");
    b.setTagsCsv("life");
    b.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

    when(postService.listPublic(anyInt(), eq(0))).thenReturn(List.of(a, b));

    ApiResponse<List<SearchController.SearchResultItem>> res = controller.search(
        "java", "java", 10L, 20, 0);

    assertEquals(true, res.isSuccess());
    assertNotNull(res.getData());
    assertEquals(1, res.getData().size());
    assertEquals(1L, res.getData().get(0).id);
  }

  @Test
  void emptyQueryReturnsAllWithinWindow() {
    PostEntity a = new PostEntity();
    a.setId(1L);
    a.setAuthorId(10L);
    a.setTitle("A");
    a.setSlug("p-1");

    PostEntity b = new PostEntity();
    b.setId(2L);
    b.setAuthorId(11L);
    b.setTitle("B");
    b.setSlug("p-2");

    when(postService.listPublic(anyInt(), eq(0))).thenReturn(List.of(a, b));

    ApiResponse<List<SearchController.SearchResultItem>> res = controller.search(
        " ", null, null, 20, 0);

    assertEquals(true, res.isSuccess());
    assertNotNull(res.getData());
    assertEquals(2, res.getData().size());
  }
}
