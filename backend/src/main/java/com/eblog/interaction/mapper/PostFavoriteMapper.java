package com.eblog.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.interaction.entity.PostFavoriteEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostFavoriteMapper extends BaseMapper<PostFavoriteEntity> {

  default boolean exists(Long postId, Long userId) {
    return selectCount(
      new LambdaQueryWrapper<PostFavoriteEntity>()
        .eq(PostFavoriteEntity::getPostId, postId)
        .eq(PostFavoriteEntity::getUserId, userId)
    ) > 0;
  }

  default void deleteByPostAndUser(Long postId, Long userId) {
    delete(new LambdaQueryWrapper<PostFavoriteEntity>()
      .eq(PostFavoriteEntity::getPostId, postId)
      .eq(PostFavoriteEntity::getUserId, userId)
    );
  }
}
