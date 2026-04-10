package com.example.web.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.web.entity.*;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户饮食记录表对应的Mapper
 */
@Mapper
public interface TUserDietRecordMapper  extends BaseMapper<TUserDietRecord> {

}
