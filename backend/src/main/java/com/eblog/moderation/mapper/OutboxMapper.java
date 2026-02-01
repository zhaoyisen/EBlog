package com.eblog.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eblog.moderation.entity.OutboxEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OutboxMapper extends BaseMapper<OutboxEntity> {

  @Select("""
      SELECT * FROM moderation_outbox
      WHERE status IN ('PENDING', 'FAILED')
      AND attempts < 3
      ORDER BY created_at ASC
      LIMIT #{limit}
      FOR UPDATE SKIP LOCKED
      """)
  List<OutboxEntity> lockPendingTasks(@Param("limit") int limit);

  @Update("""
      UPDATE moderation_outbox
      SET status = 'PROCESSING', updated_at = NOW()
      WHERE id = #{id}
      """)
  int markProcessing(@Param("id") Long id);

  @Update("""
      UPDATE moderation_outbox
      SET status = 'COMPLETED', updated_at = NOW()
      WHERE id = #{id}
      """)
  int markCompleted(@Param("id") Long id);

  @Update("""
      UPDATE moderation_outbox
      SET status = 'FAILED',
          attempts = attempts + 1,
          last_error = #{error},
          updated_at = NOW()
      WHERE id = #{id}
      """)
  int markFailed(@Param("id") Long id, @Param("error") String error);

  @Update("""
      UPDATE moderation_outbox
      SET status = 'DEAD_LETTER',
          attempts = attempts + 1,
          last_error = #{error},
          updated_at = NOW()
      WHERE id = #{id}
      """)
  int markDeadLetter(@Param("id") Long id, @Param("error") String error);
}
