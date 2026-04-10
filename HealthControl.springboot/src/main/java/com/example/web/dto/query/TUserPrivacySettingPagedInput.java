package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户隐私设置查询模型
 */
@NoArgsConstructor
@lombok.EqualsAndHashCode(callSuper = false)
@Data
public class TUserPrivacySettingPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
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

}
