package com.eblog.invite;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InviteCodeMapper extends BaseMapper<InviteCodeEntity> {

  @Update("""
      UPDATE invite_codes
      SET used_count = used_count + 1
      WHERE code = #{code}
        AND status = 'ACTIVE'
        AND (expires_at IS NULL OR expires_at > #{now})
        AND used_count < max_uses
      """)
  int consumeByCode(@Param("code") String code, @Param("now") LocalDateTime now);

  @Update("""
      UPDATE invite_codes
      SET status = 'REVOKED', revoked_at = #{now}
      WHERE code = #{code} AND status = 'ACTIVE'
      """)
  int revokeByCode(@Param("code") String code, @Param("now") LocalDateTime now);

  @Select("""
      SELECT id, code, status, max_uses, used_count, expires_at, created_at, revoked_at
      FROM invite_codes
      WHERE (#{status} IS NULL OR status = #{status})
      ORDER BY id DESC
      LIMIT #{limit} OFFSET #{offset}
      """)
  List<InviteCodeEntity> listCodes(@Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);
}
