package com.example.web.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.web.entity.*;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户就诊准备报告表对应的Mapper
 */
@Mapper
public interface TUserMedicalPrepareReportMapper  extends BaseMapper<TUserMedicalPrepareReport> {

}
