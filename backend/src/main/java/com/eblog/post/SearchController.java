package com.eblog.post;

import com.eblog.api.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
  private final PostService postService;

  public SearchController(PostService postService) {
    this.postService = postService;
  }

  @GetMapping
  public ApiResponse<List<PostEntity>> search(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) Long authorId,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    List<PostEntity> results = postService.search(q, tag, authorId, limit, offset);
    return ApiResponse.ok(results);
  }
}
