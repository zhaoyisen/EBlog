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
      SELECT * FROM outbox
      WHERE status = 'PENDING'
      ORDER BY created_at ASC
      LIMIT #{limit}
      FOR UPDATE SKIP LOCKED
      """)
  List<OutboxEntity> lockPendingTasks(@Param("limit") int limit);

  @Update("""
      UPDATE outbox
      SET status = 'PROCESSING', updated_at = NOW()
      WHERE id = #{id}
      """)
  int markProcessing(@Param("id") Long id);

  @Update("""
      UPDATE outbox
      SET status = 'COMPLETED', updated_at = NOW()
      WHERE id = #{id}
      """)
  int markCompleted(@Param("id") Long id);

  @Update("""
      UPDATE outbox
      SET status = 'FAILED',
          attempts = attempts + 1,
          last_error = #{error},
          updated_at = NOW()
      WHERE id = #{id}
      """)
  int markFailed(@Param("id") Long id, @Param("error") String error);
}
