package com.eblog.announcement;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AnnouncementMapper extends BaseMapper<AnnouncementEntity> {
    @Select("SELECT * FROM announcements ORDER BY created_at DESC LIMIT 5")
    List<AnnouncementEntity> listLatest();
}
