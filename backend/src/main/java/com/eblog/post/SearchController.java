package com.eblog.post;

import com.eblog.api.common.ApiResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@ConditionalOnBean(PostMapper.class)
public class SearchController {
  private static final int MAX_WINDOW = 1000;
  private static final int MAX_LIMIT = 50;

  private final PostService postService;

  public SearchController(PostService postService) {
    this.postService = postService;
  }

  @GetMapping
  public ApiResponse<List<SearchResultItem>> search(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "tag", required = false) String tag,
      @RequestParam(name = "authorId", required = false) Long authorId,
      @RequestParam(name = "limit", required = false, defaultValue = "20") int limit,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
    String query = normalizeQuery(q);
    String tagNorm = TagParser.normalize(tag);

    int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
    int safeOffset = Math.max(offset, 0);

    List<PostEntity> window = postService.listPublic(MAX_WINDOW, 0);
    List<SearchResultItem> matches = new ArrayList<>();
    for (PostEntity p : window) {
      if (authorId != null && p.getAuthorId() != null && !authorId.equals(p.getAuthorId())) {
        continue;
      }
      if (authorId != null && p.getAuthorId() == null) {
        continue;
      }

      List<String> tags = TagParser.parseTags(p.getTagsCsv());
      if (tagNorm != null && !tags.contains(tagNorm)) {
        continue;
      }

      if (query != null && !matchesQuery(p, tags, query)) {
        continue;
      }

      SearchResultItem item = new SearchResultItem();
      item.id = p.getId();
      item.authorId = p.getAuthorId();
      item.title = p.getTitle();
      item.slug = p.getSlug();
      item.summary = p.getSummary();
      item.tags = tags;
      item.category = p.getCategory();
      item.createdAt = p.getCreatedAt();
      matches.add(item);
    }

    int from = Math.min(safeOffset, matches.size());
    int to = Math.min(from + safeLimit, matches.size());
    return ApiResponse.ok(matches.subList(from, to));
  }

  private static boolean matchesQuery(PostEntity p, List<String> tags, String query) {
    String title = p.getTitle();
    if (title != null && title.toLowerCase(Locale.ROOT).contains(query)) {
      return true;
    }
    String summary = p.getSummary();
    if (summary != null && summary.toLowerCase(Locale.ROOT).contains(query)) {
      return true;
    }
    for (String t : tags) {
      if (t.contains(query)) {
        return true;
      }
    }
    return false;
  }

  private static String normalizeQuery(String q) {
    if (q == null) {
      return null;
    }
    String s = q.trim();
    if (s.isEmpty()) {
      return null;
    }
    s = s.toLowerCase(Locale.ROOT);
    s = s.replaceAll("\\s+", " ");
    return s;
  }

  public static class SearchResultItem {
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
