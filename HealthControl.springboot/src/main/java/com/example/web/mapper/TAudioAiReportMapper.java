package com.example.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.web.entity.TAudioAiReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 音频检测AI解读报告表对应的Mapper
 */
@Mapper
public interface TAudioAiReportMapper extends BaseMapper<TAudioAiReport> {
}

