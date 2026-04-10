package com.example.web.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.web.entity.*;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户隐私设置表对应的Mapper
 */
@Mapper
public interface TUserPrivacySettingMapper  extends BaseMapper<TUserPrivacySetting> {

}
