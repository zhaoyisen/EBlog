package com.eblog.interaction;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.api.common.ErrorCode;
import com.eblog.interaction.entity.PostLikeEntity;
import com.eblog.interaction.entity.PostFavoriteEntity;
import com.eblog.interaction.mapper.PostLikeMapper;
import com.eblog.interaction.mapper.PostFavoriteMapper;
import com.eblog.post.PostEntity;
import com.eblog.post.PostMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteractionService {

  private final PostLikeMapper postLikeMapper;
  private final PostFavoriteMapper postFavoriteMapper;
  private final PostMapper postMapper;

  private static final Map<String, AtomicInteger> likeRateLimitMap = new ConcurrentHashMap<>();
  private static final int RATE_LIMIT_SECONDS = 60;
  private static final int MAX_LIKES_PER_WINDOW = 10;

  public InteractionService(
      PostLikeMapper postLikeMapper,
      PostFavoriteMapper postFavoriteMapper,
      PostMapper postMapper) {
    this.postLikeMapper = postLikeMapper;
    this.postFavoriteMapper = postFavoriteMapper;
    this.postMapper = postMapper;
  }

  @Transactional
  public LikeResult likePost(Long postId) {
    Long userId = currentUserId();
    if (userId == null) {
      return LikeResult.error(ErrorCode.UNAUTHORIZED);
    }

    if (!checkLikeRateLimit(userId)) {
      return LikeResult.error(ErrorCode.TOO_MANY_REQUESTS);
    }

    PostEntity post = postMapper.selectById(postId);
    if (post == null || !"PUBLISHED".equals(post.getStatus()) || !"APPROVED".equals(post.getModerationStatus())) {
      return LikeResult.error(ErrorCode.POST_NOT_FOUND);
    }

    if (postLikeMapper.exists(postId, userId)) {
      return LikeResult.error(ErrorCode.BAD_REQUEST);
    }

    PostLikeEntity like = new PostLikeEntity();
    like.setPostId(postId);
    like.setUserId(userId);
    like.setCreatedAt(LocalDateTime.now());
    postLikeMapper.insert(like);

    return LikeResult.success(postLikeMapper.countByPostId(postId));
  }

  @Transactional
  public ErrorCode unlikePost(Long postId) {
    Long userId = currentUserId();
    if (userId == null) {
      return ErrorCode.UNAUTHORIZED;
    }

    postLikeMapper.delete(new LambdaQueryWrapper<PostLikeEntity>()
      .eq(PostLikeEntity::getPostId, postId)
      .eq(PostLikeEntity::getUserId, userId)
    );
    return null;
  }

  @Transactional
  public ErrorCode favoritePost(Long postId) {
    Long userId = currentUserId();
    if (userId == null) {
      return ErrorCode.UNAUTHORIZED;
    }

    PostEntity post = postMapper.selectById(postId);
    if (post == null || !"PUBLISHED".equals(post.getStatus()) || !"APPROVED".equals(post.getModerationStatus())) {
      return ErrorCode.POST_NOT_FOUND;
    }

    if (postFavoriteMapper.exists(postId, userId)) {
      return ErrorCode.BAD_REQUEST;
    }

    PostFavoriteEntity favorite = new PostFavoriteEntity();
    favorite.setPostId(postId);
    favorite.setUserId(userId);
    favorite.setCreatedAt(LocalDateTime.now());
    postFavoriteMapper.insert(favorite);

    return null;
  }

  @Transactional
  public ErrorCode unfavoritePost(Long postId) {
    Long userId = currentUserId();
    if (userId == null) {
      return ErrorCode.UNAUTHORIZED;
    }

    postFavoriteMapper.deleteByPostAndUser(postId, userId);
    return null;
  }

  public int getLikeCount(Long postId) {
    return postLikeMapper.countByPostId(postId);
  }

  public boolean isLikedByUser(Long postId, Long userId) {
    if (userId == null) {
      return false;
    }
    return postLikeMapper.exists(postId, userId);
  }

  public boolean isFavoritedByUser(Long postId, Long userId) {
    if (userId == null) {
      return false;
    }
    return postFavoriteMapper.exists(postId, userId);
  }

  private boolean checkLikeRateLimit(Long userId) {
    String key = "like:" + userId;
    AtomicInteger count = likeRateLimitMap.computeIfAbsent(key, k -> new AtomicInteger(0));

    if (count.incrementAndGet() > MAX_LIKES_PER_WINDOW) {
      count.decrementAndGet();
      return false;
    }

    new Thread(() -> {
      try {
        Thread.sleep(RATE_LIMIT_SECONDS * 1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        count.decrementAndGet();
        if (count.get() == 0) {
          likeRateLimitMap.remove(key);
        }
      }
    }).start();

    return true;
  }

  private Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) {
      return null;
    }
    try {
      return Long.valueOf(String.valueOf(auth.getPrincipal()));
    } catch (Exception e) {
      return null;
    }
  }

  public static class LikeResult {
    private final ErrorCode error;
    private final Integer likeCount;

    private LikeResult(ErrorCode error, Integer likeCount) {
      this.error = error;
      this.likeCount = likeCount;
    }

    public static LikeResult success(Integer likeCount) {
      return new LikeResult(null, likeCount);
    }

    public static LikeResult error(ErrorCode error) {
      return new LikeResult(error, null);
    }

    public boolean isSuccess() {
      return error == null;
    }

    public ErrorCode getError() {
      return error;
    }

    public Integer getLikeCount() {
      return likeCount;
    }
  }
}
