package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.example.web.tools.jackson.FlexibleLocalDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * 喉镜照片记录查询模型
 */
@NoArgsConstructor
@Data
public class TLaryngoscopePhotoPagedInput extends PagedInput {
    
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
     * 关联音频自查记录表主键t
     */
  	 @JsonProperty("DetectId")
    private Integer DetectId;
    /**
     * 审核更新时间时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(contentUsing = FlexibleLocalDateTimeDeserializer.class)
    private List<LocalDateTime> UpdateTimeRange;
    /**
     * 照片上传时间时间范围
     */
    @JsonProperty("UploadTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(contentUsing = FlexibleLocalDateTimeDeserializer.class)
    private List<LocalDateTime> UploadTimeRange;
     /**
     * 审核状态
     */
  	 @JsonProperty("AuditStatus")
    private Boolean AuditStatus;
     /**
     * 软删除标记
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
