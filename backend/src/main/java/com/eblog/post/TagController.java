package com.eblog.post;

import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TagController {
  private static final int MAX_AGG_ROWS = 1000;
  private static final int MAX_LIMIT = 50;

  private final PostService postService;

  public TagController(PostService postService) {
    this.postService = postService;
  }

  @GetMapping("/tags")
  public ApiResponse<List<TagCount>> listTags() {
    List<PostEntity> posts = postService.listPublic(MAX_AGG_ROWS, 0);
    Map<String, Integer> counts = new HashMap<String, Integer>();
    for (PostEntity p : posts) {
      for (String tag : TagParser.parseTags(p.getTagsCsv())) {
        counts.put(tag, counts.getOrDefault(tag, 0) + 1);
      }
    }
    List<TagCount> res = new ArrayList<>();
    for (Map.Entry<String, Integer> e : counts.entrySet()) {
      TagCount tc = new TagCount();
      tc.tag = e.getKey();
      tc.count = e.getValue();
      res.add(tc);
    }
    res.sort((a, b) -> {
      int byCount = Integer.compare(b.count, a.count);
      if (byCount != 0) return byCount;
      return a.tag.compareTo(b.tag);
    });
    return ApiResponse.ok(res);
  }

  @GetMapping("/tags/{tag}")
  public ApiResponse<List<PostSummaryView>> listByTag(
      @PathVariable("tag") String tag,
      @RequestParam(name = "limit", required = false, defaultValue = "20") int limit,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
    String normalized = TagParser.normalize(tag);
    if (normalized == null) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }

    int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
    int safeOffset = Math.max(offset, 0);

    // MVP: filter in-memory from a bounded window.
    // TODO: normalize tags into a join table for accurate filtering/pagination.
    List<PostEntity> window = postService.listPublic(MAX_AGG_ROWS, 0);
    List<PostSummaryView> matches = new ArrayList<>();
    for (PostEntity p : window) {
      List<String> tags = TagParser.parseTags(p.getTagsCsv());
      if (tags.contains(normalized)) {
        PostSummaryView v = new PostSummaryView();
        v.id = p.getId();
        v.authorId = p.getAuthorId();
        v.title = p.getTitle();
        v.slug = p.getSlug();
        v.summary = p.getSummary();
        v.tags = tags;
        v.category = p.getCategory();
        v.createdAt = p.getCreatedAt();
        matches.add(v);
      }
    }

    int from = Math.min(safeOffset, matches.size());
    int to = Math.min(from + safeLimit, matches.size());
    return ApiResponse.ok(matches.subList(from, to));
  }

  @GetMapping("/categories")
  public ApiResponse<List<CategoryCount>> listCategories() {
    List<PostEntity> posts = postService.listPublic(MAX_AGG_ROWS, 0);
    Map<String, Integer> counts = new HashMap<String, Integer>();
    for (PostEntity p : posts) {
      String c = p.getCategory();
      if (c == null || c.trim().isEmpty()) {
        continue;
      }
      String normalized = c.trim();
      counts.put(normalized, counts.getOrDefault(normalized, 0) + 1);
    }
    List<CategoryCount> res = new ArrayList<>();
    for (Map.Entry<String, Integer> e : counts.entrySet()) {
      CategoryCount cc = new CategoryCount();
      cc.category = e.getKey();
      cc.count = e.getValue();
      res.add(cc);
    }
    res.sort((a, b) -> {
      int byCount = Integer.compare(b.count, a.count);
      if (byCount != 0) return byCount;
      return a.category.compareTo(b.category);
    });
    return ApiResponse.ok(res);
  }

  public static class TagCount {
    public String tag;
    public int count;
  }

  public static class CategoryCount {
    public String category;
    public int count;
  }

  public static class PostSummaryView {
    public Long id;
    public Long authorId;
    public String title;
    public String slug;
    public String summary;
    public List<String> tags;
    public String category;
    public LocalDateTime createdAt;
  }
}
