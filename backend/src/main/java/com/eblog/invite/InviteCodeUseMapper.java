package com.eblog.invite;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InviteCodeUseMapper extends BaseMapper<InviteCodeUseEntity> {

  @Select("""
      SELECT id, invite_code_id, used_by_user_id, used_ip, used_at
      FROM invite_code_uses
      WHERE invite_code_id = #{inviteCodeId}
      ORDER BY id DESC
      LIMIT #{limit} OFFSET #{offset}
      """)
  List<InviteCodeUseEntity> listUses(@Param("inviteCodeId") Long inviteCodeId,
      @Param("limit") int limit,
      @Param("offset") int offset);
}
