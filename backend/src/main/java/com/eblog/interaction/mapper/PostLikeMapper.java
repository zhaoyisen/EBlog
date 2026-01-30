package com.eblog.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eblog.interaction.entity.PostLikeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLikeEntity> {

  default int countByPostId(Long postId) {
    return Math.toIntExact(selectCount(
      new LambdaQueryWrapper<PostLikeEntity>().eq(PostLikeEntity::getPostId, postId)
    ));
  }

  default boolean exists(Long postId, Long userId) {
    return selectCount(
      new LambdaQueryWrapper<PostLikeEntity>()
        .eq(PostLikeEntity::getPostId, postId)
        .eq(PostLikeEntity::getUserId, userId)
    ) > 0;
  }
}
