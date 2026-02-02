 package com.eblog.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eblog.api.common.ApiResponse;
import com.eblog.api.common.ErrorCode;
import com.eblog.metadata.CategoryEntity;
import com.eblog.metadata.CategoryMapper;
import com.eblog.metadata.CategoryService;
import com.eblog.metadata.TagEntity;
import com.eblog.metadata.TagMapper;
import com.eblog.metadata.TagService;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/api/v1")
public class TagController {
  private static final int MAX_LIMIT = 50;

  private final PostService postService;
  private final TagMapper tagMapper;
  private final CategoryMapper categoryMapper;
  private final CategoryService categoryService;
  private final TagService tagService;

  public TagController(
      PostService postService,
      TagMapper tagMapper,
      CategoryMapper categoryMapper,
      CategoryService categoryService,
      TagService tagService) {
    this.postService = postService;
    this.tagMapper = tagMapper;
    this.categoryMapper = categoryMapper;
    this.categoryService = categoryService;
    this.tagService = tagService;
  }

  @GetMapping("/tags")
  public ApiResponse<List<TagResponse>> listTags() {
    List<TagEntity> tags = tagMapper.selectList(
        Wrappers.<TagEntity>lambdaQuery()
            .orderByAsc(TagEntity::getName)
    );

    List<TagResponse> res = tags.stream()
        .map(t -> {
          TagResponse tr = new TagResponse();
          tr.id = t.getId();
          tr.name = t.getName();
          tr.slug = t.getSlug();
          tr.postCount = t.getPostCount();
          return tr;
        })
        .collect(Collectors.toList());

    return ApiResponse.ok(res);
  }

  @GetMapping("/tags/popular")
  public ApiResponse<List<TagResponse>> listPopularTags(
      @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
    List<TagEntity> tags = tagService.getPopular(limit);
    List<TagResponse> res = tags.stream()
        .map(t -> {
          TagResponse tr = new TagResponse();
          tr.id = t.getId();
          tr.name = t.getName();
          tr.slug = t.getSlug();
          tr.postCount = t.getPostCount();
          return tr;
        })
        .collect(Collectors.toList());
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

    List<PostEntity> matches = postService.search(null, normalized, null, safeLimit, safeOffset);

    List<PostSummaryView> views = matches.stream().map(p -> {
        PostSummaryView v = new PostSummaryView();
        v.id = p.getId();
        v.authorId = p.getAuthorId();
        v.title = p.getTitle();
        v.slug = p.getSlug();
        v.summary = p.getSummary();
        v.tags = TagParser.parseTags(p.getTagsCsv());
        v.category = p.getCategory();
        v.createdAt = p.getCreatedAt();
        return v;
    }).collect(Collectors.toList());

    return ApiResponse.ok(views);
  }

  @GetMapping("/categories")
  public ApiResponse<List<CategoryResponse>> listCategories() {
    List<CategoryEntity> categories = categoryMapper.selectList(
        Wrappers.<CategoryEntity>lambdaQuery()
            .orderByAsc(CategoryEntity::getName)
    );

    List<CategoryResponse> res = categories.stream()
        .map(c -> {
          CategoryResponse cr = new CategoryResponse();
          cr.id = c.getId();
          cr.name = c.getName();
          cr.slug = c.getSlug();
          cr.description = c.getDescription();
          cr.postCount = c.getPostCount();
          return cr;
        })
        .collect(Collectors.toList());

    return ApiResponse.ok(res);
  }

  // ========== Admin endpoints for tags ==========

  @GetMapping("/tags/page")
  public ApiResponse<Page<TagResponse>> listTagsPage(
      @RequestParam(name = "page", required = false, defaultValue = "1") int page,
      @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
    Page<TagEntity> tagPage = tagService.list(page, size);
    Page<TagResponse> responsePage = new Page<>(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
    List<TagResponse> responses = tagPage.getRecords().stream()
        .map(t -> {
          TagResponse tr = new TagResponse();
          tr.id = t.getId();
          tr.name = t.getName();
          tr.slug = t.getSlug();
          tr.postCount = t.getPostCount();
          return tr;
        })
        .collect(Collectors.toList());
    responsePage.setRecords(responses);
    return ApiResponse.ok(responsePage);
  }

  @GetMapping("/tags/{id}")
  public ApiResponse<TagResponse> getTag(@PathVariable("id") Long id) {
    TagEntity tag = tagService.get(id);
    if (tag == null) {
      return ApiResponse.fail(ErrorCode.NOT_FOUND.getCode(), ErrorCode.NOT_FOUND.getMessage());
    }
    TagResponse tr = new TagResponse();
    tr.id = tag.getId();
    tr.name = tag.getName();
    tr.slug = tag.getSlug();
    tr.postCount = tag.getPostCount();
    return ApiResponse.ok(tr);
  }

  @PostMapping("/tags")
  public ApiResponse<TagResponse> createTag(@RequestBody TagCreateRequest body) {
    if (body == null || body.name == null || body.name.trim().isEmpty()) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }

    TagEntity tag = tagService.create(body.name, body.slug);

    if (tag == null) {
      if (!isAdmin()) {
        return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
      }
      return ApiResponse.fail(ErrorCode.CONFLICT.getCode(), "标签名称已存在");
    }

    TagResponse tr = new TagResponse();
    tr.id = tag.getId();
    tr.name = tag.getName();
    tr.slug = tag.getSlug();
    tr.postCount = tag.getPostCount();
    return ApiResponse.ok(tr);
  }

  @PutMapping("/tags/{id}")
  public ApiResponse<TagResponse> updateTag(
      @PathVariable("id") Long id,
      @RequestBody TagUpdateRequest body) {
    if (body == null) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }

    ErrorCode error = tagService.update(id, body.name, body.slug);

    if (error != null) {
      if (error == ErrorCode.NOT_FOUND) {
        return ApiResponse.fail(ErrorCode.NOT_FOUND.getCode(), ErrorCode.NOT_FOUND.getMessage());
      }
      if (error == ErrorCode.CONFLICT) {
        return ApiResponse.fail(ErrorCode.CONFLICT.getCode(), "标签名称或slug已存在");
      }
      if (error == ErrorCode.FORBIDDEN) {
        return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
      }
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }

    TagEntity tag = tagService.get(id);
    TagResponse tr = new TagResponse();
    tr.id = tag.getId();
    tr.name = tag.getName();
    tr.slug = tag.getSlug();
    tr.postCount = tag.getPostCount();
    return ApiResponse.ok(tr);
  }

  @DeleteMapping("/tags/{id}")
  public ApiResponse<Object> deleteTag(@PathVariable("id") Long id) {
    ErrorCode error = tagService.delete(id);
    if (error != null) {
      if (error == ErrorCode.NOT_FOUND) {
        return ApiResponse.fail(ErrorCode.NOT_FOUND.getCode(), ErrorCode.NOT_FOUND.getMessage());
      }
      if (error == ErrorCode.CONFLICT) {
        return ApiResponse.fail(ErrorCode.CONFLICT.getCode(), "该标签下还有文章，无法删除");
      }
      if (error == ErrorCode.FORBIDDEN) {
        return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
      }
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(null);
  }

  // ========== Admin endpoints for categories ==========

  @GetMapping("/categories/page")
  public ApiResponse<Page<CategoryResponse>> listCategoriesPage(
      @RequestParam(name = "page", required = false, defaultValue = "1") int page,
      @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
    Page<CategoryEntity> categoryPage = categoryService.list(page, size);
    Page<CategoryResponse> responsePage = new Page<>(
        categoryPage.getCurrent(),
        categoryPage.getSize(),
        categoryPage.getTotal()
    );
    List<CategoryResponse> responses = categoryPage.getRecords().stream()
        .map(c -> {
          CategoryResponse cr = new CategoryResponse();
          cr.id = c.getId();
          cr.name = c.getName();
          cr.slug = c.getSlug();
          cr.description = c.getDescription();
          cr.postCount = c.getPostCount();
          return cr;
        })
        .collect(Collectors.toList());
    responsePage.setRecords(responses);
    return ApiResponse.ok(responsePage);
  }

  @GetMapping("/categories/{id}")
  public ApiResponse<CategoryResponse> getCategory(@PathVariable("id") Long id) {
    CategoryEntity category = categoryService.get(id);
    if (category == null) {
      return ApiResponse.fail(ErrorCode.NOT_FOUND.getCode(), ErrorCode.NOT_FOUND.getMessage());
    }
    CategoryResponse cr = new CategoryResponse();
    cr.id = category.getId();
    cr.name = category.getName();
    cr.slug = category.getSlug();
    cr.description = category.getDescription();
    cr.postCount = category.getPostCount();
    return ApiResponse.ok(cr);
  }

  @PostMapping("/categories")
  public ApiResponse<CategoryResponse> createCategory(@RequestBody CategoryCreateRequest body) {
    if (body == null || body.name == null || body.name.trim().isEmpty()) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }

    CategoryEntity category = categoryService.create(body.name, body.description, body.slug);

    if (category == null) {
      if (!isAdmin()) {
        return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
      }
      return ApiResponse.fail(ErrorCode.CONFLICT.getCode(), "分类名称已存在");
    }

    CategoryResponse cr = new CategoryResponse();
    cr.id = category.getId();
    cr.name = category.getName();
    cr.slug = category.getSlug();
    cr.description = category.getDescription();
    cr.postCount = category.getPostCount();
    return ApiResponse.ok(cr);
  }

  @PutMapping("/categories/{id}")
  public ApiResponse<CategoryResponse> updateCategory(
      @PathVariable("id") Long id,
      @RequestBody CategoryUpdateRequest body) {
    if (body == null) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }

    ErrorCode error = categoryService.update(id, body.name, body.description, body.slug);

    if (error != null) {
      if (error == ErrorCode.NOT_FOUND) {
        return ApiResponse.fail(ErrorCode.NOT_FOUND.getCode(), ErrorCode.NOT_FOUND.getMessage());
      }
      if (error == ErrorCode.CONFLICT) {
        return ApiResponse.fail(ErrorCode.CONFLICT.getCode(), "分类名称或slug已存在");
      }
      if (error == ErrorCode.FORBIDDEN) {
        return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
      }
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }

    CategoryEntity category = categoryService.get(id);
    CategoryResponse cr = new CategoryResponse();
    cr.id = category.getId();
    cr.name = category.getName();
    cr.slug = category.getSlug();
    cr.description = category.getDescription();
    cr.postCount = category.getPostCount();
    return ApiResponse.ok(cr);
  }

  @DeleteMapping("/categories/{id}")
  public ApiResponse<Object> deleteCategory(@PathVariable("id") Long id) {
    ErrorCode error = categoryService.delete(id);
    if (error != null) {
      if (error == ErrorCode.NOT_FOUND) {
        return ApiResponse.fail(ErrorCode.NOT_FOUND.getCode(), ErrorCode.NOT_FOUND.getMessage());
      }
      if (error == ErrorCode.CONFLICT) {
        return ApiResponse.fail(ErrorCode.CONFLICT.getCode(), "该分类下还有文章，无法删除");
      }
      if (error == ErrorCode.FORBIDDEN) {
        return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
      }
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(null);
  }

  private static boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getAuthorities() == null) {
      return false;
    }
    for (GrantedAuthority a : auth.getAuthorities()) {
      String role = a.getAuthority();
      if ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role)) {
        return true;
      }
    }
    return false;
  }

  public static class TagResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    public Long id;
    public String name;
    public String slug;
    public Integer postCount;
  }

  public static class CategoryResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    public Long id;
    public String name;
    public String slug;
    public String description;
    public Integer postCount;
  }

  public static class TagCreateRequest {
    public String name;
    public String slug;
  }

  public static class TagUpdateRequest {
    public String name;
    public String slug;
  }

  public static class CategoryCreateRequest {
    public String name;
    public String description;
    public String slug;
  }

  public static class CategoryUpdateRequest {
    public String name;
    public String description;
    public String slug;
  }

  public static class PostSummaryView {
    @JsonSerialize(using = ToStringSerializer.class)
    public Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    public Long authorId;
    public String title;
    public String slug;
    public String summary;
    public List<String> tags;
    public String category;
    public LocalDateTime createdAt;
  }
}
