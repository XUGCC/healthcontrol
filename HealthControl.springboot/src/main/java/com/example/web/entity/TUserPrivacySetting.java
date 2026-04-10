package com.example.web.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.example.web.dto.TUserPrivacySettingDto;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
/**
 * 用户隐私设置表
 */
@lombok.EqualsAndHashCode(callSuper = false)
@Data
@TableName("`TUserPrivacySetting`")
public class TUserPrivacySetting extends BaseEntity {

      
    /**
     * 关联用户表主键t_user.
     */  
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;          
      
    /**
     * 本地存储开启状态：0=否，1=是
     */  
    @JsonProperty("LocalStorageStatus")
    @TableField(value="LocalStorageStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean LocalStorageStatus;          
      
    /**
     * 数据匿名授权状态：0=否，1=是
     */  
    @JsonProperty("DataAnonymousAuth")
    @TableField(value="DataAnonymousAuth",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean DataAnonymousAuth;          
      
    /**
     * 隐私协议同意状态：0=否，1=是
     */  
    @JsonProperty("PrivacyAgreeStatus")
    @TableField(value="PrivacyAgreeStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean PrivacyAgreeStatus;          
      
    /**
     * 是否开启自动清理：0=否，1=是
     */
    @JsonProperty("DataRetentionEnabled")
    @TableField(value="DataRetentionEnabled",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean DataRetentionEnabled;

    /**
     * 数据保留时长（月），NULL/0=永久保留
     */
    @JsonProperty("DataRetentionMonths")
    @TableField(value="DataRetentionMonths",updateStrategy = FieldStrategy.ALWAYS)
    private Integer DataRetentionMonths;

    /**
     * 软删除标记：0=未删除，1=已删除
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把用户隐私设置实体转换成用户隐私设置传输模型
     */
    public TUserPrivacySettingDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TUserPrivacySettingDto TUserPrivacySettingDto = new TUserPrivacySettingDto();
       
        BeanUtils.copyProperties(TUserPrivacySettingDto,this);
       
        return TUserPrivacySettingDto;
    }

}
