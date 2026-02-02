 package com.eblog.interaction;

 import com.eblog.api.common.ApiResponse;
 import com.eblog.api.common.ErrorCode;
 import com.eblog.interaction.InteractionService;
 import com.eblog.interaction.mapper.PostLikeMapper;
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
@RequestMapping("/api/v1/posts/{postId}/likes")
public class PostLikeController {

  private final InteractionService interactionService;

  public PostLikeController(InteractionService interactionService) {
    this.interactionService = interactionService;
  }

  @GetMapping
  public ApiResponse<LikeStatus> getLikeStatus(@PathVariable("postId") String postIdStr, @RequestParam(value = "userId", required = false) Long userId) {
    Long postId = parsePostId(postIdStr);
    int count = interactionService.getLikeCount(postId);
    boolean liked = interactionService.isLikedByUser(postId, userId);

    LikeStatus status = new LikeStatus();
    status.count = count;
    status.liked = liked;
    return ApiResponse.ok(status);
  }

  @PostMapping
  public ApiResponse<LikeResponse> like(@PathVariable("postId") String postIdStr) {
    Long postId = parsePostId(postIdStr);
    InteractionService.LikeResult result = interactionService.likePost(postId);
    if (!result.isSuccess()) {
      ErrorCode error = result.getError();
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    LikeResponse res = new LikeResponse();
    res.likeCount = result.getLikeCount();
    return ApiResponse.ok(res);
  }

  @DeleteMapping
  public ApiResponse<Void> unlike(@PathVariable("postId") String postIdStr) {
    Long postId = parsePostId(postIdStr);
    ErrorCode error = interactionService.unlikePost(postId);
    if (error != null) {
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(null);
  }

  private Long parsePostId(String postIdStr) {
    try {
      return Long.parseLong(postIdStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid post ID: " + postIdStr, e);
    }
  }

  public static class LikeStatus {
    public int count;
    public boolean liked;
  }

  public static class LikeResponse {
    public Integer likeCount;
  }
}
