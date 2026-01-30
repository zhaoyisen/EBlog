package com.eblog.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshTokenEntity> {

  @Update("""
      UPDATE refresh_tokens
      SET revoked_at = #{now}
      WHERE user_id = #{userId}
        AND revoked_at IS NULL
      """)
  int revokeAllByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
