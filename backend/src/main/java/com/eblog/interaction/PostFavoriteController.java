 package com.eblog.interaction;

 import com.eblog.api.common.ApiResponse;
 import com.eblog.api.common.ErrorCode;
 import com.eblog.interaction.InteractionService;
 import com.eblog.interaction.mapper.PostFavoriteMapper;
 import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
 import org.springframework.web.bind.annotation.DeleteMapping;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/{postId}/favorites")
public class PostFavoriteController {

  private final InteractionService interactionService;

  public PostFavoriteController(InteractionService interactionService) {
    this.interactionService = interactionService;
  }

  @GetMapping
  public ApiResponse<FavoriteStatus> getFavoriteStatus(@PathVariable("postId") String postIdStr, @RequestParam(value = "userId", required = false) Long userId) {
    Long postId = parsePostId(postIdStr);
    boolean favorited = interactionService.isFavoritedByUser(postId, userId);
    FavoriteStatus status = new FavoriteStatus();
    status.favorited = favorited;
    return ApiResponse.ok(status);
  }

  @PostMapping
  public ApiResponse<Void> favorite(@PathVariable("postId") String postIdStr) {
    Long postId = parsePostId(postIdStr);
    ErrorCode error = interactionService.favoritePost(postId);
    if (error != null) {
      return ApiResponse.fail(error.getCode(), error.getMessage());
    }
    return ApiResponse.ok(null);
  }

  @DeleteMapping
  public ApiResponse<Void> unfavorite(@PathVariable("postId") String postIdStr) {
    Long postId = parsePostId(postIdStr);
    ErrorCode error = interactionService.unfavoritePost(postId);
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

  public static class FavoriteStatus {
    public boolean favorited;
  }
}
