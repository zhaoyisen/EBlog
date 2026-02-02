package com.eblog.post;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<PostEntity> {

  @Select("""
      SELECT id, author_id, format, title, slug, summary, tags_csv, category, status, moderation_status, created_at, updated_at, view_count, is_pinned, is_featured
      FROM posts
      WHERE status = 'PUBLISHED'
        AND moderation_status = 'APPROVED'
      ORDER BY created_at DESC
      LIMIT #{limit} OFFSET #{offset}
      """)
  List<PostEntity> listPublic(@Param("limit") int limit, @Param("offset") int offset);

  @Select("""
      SELECT id, author_id, format, title, slug, summary, tags_csv, category, status, moderation_status, created_at, updated_at, view_count, is_pinned, is_featured
      FROM posts
      WHERE author_id = #{authorId}
      ORDER BY updated_at DESC
      LIMIT #{limit} OFFSET #{offset}
      """)
  List<PostEntity> listMy(@Param("authorId") long authorId, @Param("limit") int limit, @Param("offset") int offset);

  @Select("<script>" +
      "SELECT id, author_id, format, title, slug, summary, tags_csv, category, status, moderation_status, created_at, updated_at, view_count, is_pinned, is_featured " +
      "FROM posts " +
      "WHERE status = 'PUBLISHED' AND moderation_status = 'APPROVED' " +
      "<if test='q != null'> AND MATCH(title, summary, content_markdown) AGAINST(#{q} IN NATURAL LANGUAGE MODE) </if> " +
      "<if test='tag != null'> AND tags_csv LIKE CONCAT('%', #{tag}, '%') </if> " +
      "<if test='authorId != null'> AND author_id = #{authorId} </if> " +
      "ORDER BY is_pinned DESC, created_at DESC " +
      "LIMIT #{limit} OFFSET #{offset}" +
      "</script>")
  List<PostEntity> search(@Param("q") String q, @Param("tag") String tag, @Param("authorId") Long authorId, @Param("limit") int limit, @Param("offset") int offset);

  @Update("UPDATE posts SET view_count = COALESCE(view_count, 0) + 1 WHERE id = #{id}")
  void incrementViewCount(@Param("id") Long id);
}
