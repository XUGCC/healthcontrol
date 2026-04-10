package com.example.web.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.web.entity.*;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据导出申请表对应的Mapper
 */
@Mapper
public interface TDataExportRequestMapper extends BaseMapper<TDataExportRequest> {

}
