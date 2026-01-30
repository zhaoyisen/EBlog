package com.eblog.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eblog.moderation.entity.AuditLogEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntity> {
}
