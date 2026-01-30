 package com.eblog.comment.mapper;

 import com.baomidou.mybatisplus.core.mapper.BaseMapper;
 import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
 import com.eblog.comment.entity.CommentEntity;
 import java.util.List;
 import org.apache.ibatis.annotations.Mapper;
 import org.apache.ibatis.annotations.Param;
 import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {

  @Select("""
      SELECT * FROM comments
      WHERE post_id = #{postId}
        AND status = 'PUBLISHED'
        AND moderation_status <> 'REJECTED'
      ORDER BY created_at ASC
      LIMIT #{limit} OFFSET #{offset}
      """)
  List<CommentEntity> listPublicByPostId(@Param("postId") Long postId, @Param("limit") int limit, @Param("offset") int offset);

  default List<CommentEntity> listByPostId(Long postId, int limit, int offset) {
    return selectList(
      new LambdaQueryWrapper<CommentEntity>()
        .eq(CommentEntity::getPostId, postId)
        .orderByAsc(CommentEntity::getCreatedAt)
        .last("LIMIT " + Math.max(limit, 1) + " OFFSET " + Math.max(offset, 0))
    );
  }
}
