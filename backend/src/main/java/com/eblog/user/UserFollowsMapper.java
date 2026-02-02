package com.eblog.user;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserFollowsMapper {
    @Insert("INSERT IGNORE INTO user_follows (follower_id, followee_id, created_at) VALUES (#{followerId}, #{followeeId}, NOW())")
    void insert(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    @Delete("DELETE FROM user_follows WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    void delete(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    @Select("SELECT followee_id FROM user_follows WHERE follower_id = #{followerId}")
    List<Long> selectFollowees(@Param("followerId") Long followerId);

    @Select("SELECT follower_id FROM user_follows WHERE followee_id = #{followeeId}")
    List<Long> selectFollowers(@Param("followeeId") Long followeeId);

    @Select("SELECT COUNT(*) > 0 FROM user_follows WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);
}
