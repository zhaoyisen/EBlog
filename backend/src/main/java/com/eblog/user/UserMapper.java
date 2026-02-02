package com.eblog.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    @Select("SELECT u.* " +
            "FROM users u " +
            "LEFT JOIN posts p ON u.id = p.author_id AND p.status = 'PUBLISHED' AND p.moderation_status = 'APPROVED' " +
            "GROUP BY u.id " +
            "ORDER BY COUNT(p.id) DESC " +
            "LIMIT #{limit}")
    List<UserEntity> selectActiveUsers(@Param("limit") int limit);
}
