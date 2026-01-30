package com.eblog.post;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostMapper extends BaseMapper<PostEntity> {

  @Select("""
      SELECT id, author_id, format, title, slug, summary, tags_csv, category, status, moderation_status, created_at, updated_at
      FROM posts
      WHERE status = 'PUBLISHED'
        AND moderation_status <> 'REJECTED'
      ORDER BY created_at DESC
      LIMIT #{limit} OFFSET #{offset}
      """)
  List<PostEntity> listPublic(@Param("limit") int limit, @Param("offset") int offset);
}
