package com.eblog.post;

import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
  private final PostService postService;
  private final MarkdownRenderer markdownRenderer;

  public PostController(PostService postService, MarkdownRenderer markdownRenderer) {
    this.postService = postService;
    this.markdownRenderer = markdownRenderer;
  }

  @GetMapping
  public ApiResponse<List<PostSummary>> list(
      @RequestParam(name = "limit", required = false, defaultValue = "20") int limit,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
    List<PostEntity> posts = postService.listPublic(limit, offset);
    List<PostSummary> res = new ArrayList<>();
    for (PostEntity p : posts) {
      PostSummary s = new PostSummary();
      s.id = p.getId();
      s.authorId = p.getAuthorId();
      s.title = p.getTitle();
      s.slug = p.getSlug();
      s.summary = p.getSummary();
      s.tagsCsv = p.getTagsCsv();
      s.category = p.getCategory();
      s.createdAt = p.getCreatedAt();
      res.add(s);
    }
    return ApiResponse.ok(res);
  }

  @GetMapping("/{slug}")
  public ApiResponse<PostDetail> get(@PathVariable("slug") String slug) {
    PostEntity p = postService.findBySlug(slug);
    if (p == null || !"PUBLISHED".equalsIgnoreCase(p.getStatus()) || "REJECTED".equalsIgnoreCase(p.getModerationStatus())) {
      return ApiResponse.fail(ErrorCode.POST_NOT_FOUND.getCode(), ErrorCode.POST_NOT_FOUND.getMessage());
    }
    PostDetail d = new PostDetail();
    d.id = p.getId();
    d.authorId = p.getAuthorId();
    d.title = p.getTitle();
    d.slug = p.getSlug();
    d.summary = p.getSummary();
    d.contentMarkdown = p.getContentMarkdown();
    d.format = p.getFormat();
    if ("MDX".equalsIgnoreCase(p.getFormat())) {
      d.contentHtml = null;
    } else {
      d.contentHtml = markdownRenderer.renderToHtml(p.getContentMarkdown());
    }
    d.tagsCsv = p.getTagsCsv();
    d.category = p.getCategory();
    d.createdAt = p.getCreatedAt();
    d.updatedAt = p.getUpdatedAt();
    return ApiResponse.ok(d);
  }

  @PostMapping
  public ApiResponse<CreateResponse> create(@RequestBody CreateRequest body) {
    if (body == null) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    PostService.CreateResult result = postService.create(
        body.title, body.summary, body.contentMarkdown, body.tagsCsv, body.category, body.status, body.format);
    if (!result.isSuccess()) {
      ErrorCode error = result.getError();
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    CreateResponse res = new CreateResponse();
    res.postId = result.getPostId();
    res.slug = result.getSlug();
    return ApiResponse.ok(res);
  }

  @PutMapping("/{id}")
  public ApiResponse<Object> update(@PathVariable("id") Long id, @RequestBody UpdateRequest body) {
    if (body == null) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    ErrorCode error = postService.update(id, body.title, body.summary, body.contentMarkdown, body.tagsCsv, body.category, body.status, body.format);
    if (error != null) {
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(null);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Object> archive(@PathVariable("id") Long id) {
    ErrorCode error = postService.archive(id);
    if (error != null) {
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(null);
  }

  public static class CreateRequest {
    public String title;
    public String summary;
    public String contentMarkdown;
    public String tagsCsv;
    public String category;
    public String status;
    public String format;
  }

  public static class UpdateRequest {
    public String title;
    public String summary;
    public String contentMarkdown;
    public String tagsCsv;
    public String category;
    public String status;
    public String format;
  }

  public static class CreateResponse {
    public Long postId;
    public String slug;
  }

  public static class PostSummary {
    public Long id;
    public Long authorId;
    public String title;
    public String slug;
    public String summary;
    public String tagsCsv;
    public String category;
    public LocalDateTime createdAt;
  }

  public static class PostDetail {
    public Long id;
    public Long authorId;
    public String title;
    public String slug;
    public String summary;
    public String contentMarkdown;
    public String contentHtml;
    public String format;
    public String tagsCsv;
    public String category;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
  }
}
