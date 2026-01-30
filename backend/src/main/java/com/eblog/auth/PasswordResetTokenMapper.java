package com.eblog.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PasswordResetTokenMapper extends BaseMapper<PasswordResetTokenEntity> {

  @Update("""
      UPDATE password_reset_tokens
      SET used_at = #{now}
      WHERE id = #{id}
        AND used_at IS NULL
      """)
  int markUsed(@Param("id") Long id, @Param("now") LocalDateTime now);
}
