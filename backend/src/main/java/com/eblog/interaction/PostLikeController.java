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
@ConditionalOnBean(PostLikeMapper.class)
public class PostLikeController {

  private final InteractionService interactionService;

  public PostLikeController(InteractionService interactionService) {
    this.interactionService = interactionService;
  }

  @GetMapping
  public ApiResponse<LikeStatus> getLikeStatus(@PathVariable("postId") Long postId, @RequestParam(value = "userId", required = false) Long userId) {
    int count = interactionService.getLikeCount(postId);
    boolean liked = interactionService.isLikedByUser(postId, userId);

    LikeStatus status = new LikeStatus();
    status.count = count;
    status.liked = liked;
    return ApiResponse.ok(status);
  }

  @PostMapping
  public ApiResponse<LikeResponse> like(@PathVariable("postId") Long postId) {
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
  public ApiResponse<Void> unlike(@PathVariable("postId") Long postId) {
    ErrorCode error = interactionService.unlikePost(postId);
    if (error != null) {
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(null);
  }

  public static class LikeStatus {
    public int count;
    public boolean liked;
  }

  public static class LikeResponse {
    public Integer likeCount;
  }
}
