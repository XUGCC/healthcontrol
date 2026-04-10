package com.example.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.web.entity.TScienceReadLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科普阅读去重日志表对应的Mapper
 */
@Mapper
public interface TScienceReadLogMapper extends BaseMapper<TScienceReadLog> {
}

