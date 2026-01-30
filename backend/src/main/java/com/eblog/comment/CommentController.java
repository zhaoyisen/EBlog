 package com.eblog.comment;

 import com.eblog.api.common.ApiResponse;
 import com.eblog.api.common.ErrorCode;
 import com.eblog.comment.mapper.CommentMapper;
 import com.eblog.comment.entity.CommentEntity;
 import java.time.LocalDateTime;
 import java.util.ArrayList;
 import java.util.List;
 import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
 import org.springframework.web.bind.annotation.DeleteMapping;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@ConditionalOnBean(CommentMapper.class)
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping
  public ApiResponse<List<CommentDetail>> list(
      @PathVariable("postId") Long postId,
      @RequestParam(name = "limit", required = false, defaultValue = "20") int limit,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
    List<CommentEntity> comments = commentService.listPublicByPostId(postId, limit, offset);
    List<CommentDetail> res = new ArrayList<>();
    for (CommentEntity c : comments) {
      CommentDetail d = new CommentDetail();
      d.id = c.getId();
      d.postId = c.getPostId();
      d.authorId = c.getAuthorId();
      d.content = c.getContent();
      d.createdAt = c.getCreatedAt();
      res.add(d);
    }
    return ApiResponse.ok(res);
  }

  @PostMapping
  public ApiResponse<CreateResponse> create(
      @PathVariable("postId") Long postId,
      @RequestBody CreateRequest body) {
    if (body == null || body.content == null) {
      return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }
    CommentService.CreateResult result = commentService.create(postId, body.content);
    if (!result.isSuccess()) {
      ErrorCode error = result.getError();
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    CreateResponse res = new CreateResponse();
    res.commentId = result.getCommentId();
    return ApiResponse.ok(res);
  }

  @DeleteMapping("/{commentId}")
  public ApiResponse<Object> delete(@PathVariable("commentId") Long commentId) {
    ErrorCode error = commentService.delete(commentId);
    if (error != null) {
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(null);
  }

  public static class CreateRequest {
    public String content;
  }

  public static class CreateResponse {
    public Long commentId;
  }

  public static class CommentDetail {
    public Long id;
    public Long postId;
    public Long authorId;
    public String content;
    public LocalDateTime createdAt;
  }
}
