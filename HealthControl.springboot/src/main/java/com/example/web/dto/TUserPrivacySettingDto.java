package com.example.web.dto;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.web.entity.TUserPrivacySetting;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
/**
 * 用户隐私设置类
 */
@lombok.EqualsAndHashCode(callSuper = false)
@Data
public class TUserPrivacySettingDto extends BaseDto {

    
     
    /**
     * 关联用户表主键t_user.
     */ 
    @JsonProperty("UserId")
    private Integer UserId;          
    
     
    /**
     * 本地存储开启状态：0=否，1=是
     */ 
    @JsonProperty("LocalStorageStatus")
    private Boolean LocalStorageStatus;          
    
     
    /**
     * 数据匿名授权状态：0=否，1=是
     */ 
    @JsonProperty("DataAnonymousAuth")
    private Boolean DataAnonymousAuth;          
    
     
    /**
     * 隐私协议同意状态：0=否，1=是
     */ 
    @JsonProperty("PrivacyAgreeStatus")
    private Boolean PrivacyAgreeStatus;          
    
    /**
     * 是否开启自动清理：0=否，1=是
     */
    @JsonProperty("DataRetentionEnabled")
    private Boolean DataRetentionEnabled;

    /**
     * 数据保留时长（月），NULL/0=永久保留
     */
    @JsonProperty("DataRetentionMonths")
    private Integer DataRetentionMonths;
    
    /**
     * 软删除标记：0=未删除，1=已删除
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

    @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
 	 /**
     * 把用户隐私设置传输模型转换成用户隐私设置实体
     */
    public TUserPrivacySetting MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TUserPrivacySetting TUserPrivacySetting= new TUserPrivacySetting();
     
         BeanUtils.copyProperties(TUserPrivacySetting,this);
        
        return TUserPrivacySetting;
    }

}
