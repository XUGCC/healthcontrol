package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * 症状日志查询模型
 */
@NoArgsConstructor
@Data
public class TSymptomLogPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 症状持续时长模糊查询条件
     */
  	 @JsonProperty("SymptomDuration")
    private String SymptomDuration;
    /**
     * 症状备注模糊查询条件
     */
  	 @JsonProperty("Remark")
    private String Remark;
     /**
     * 关联用户表主键t_user.
     */
  	 @JsonProperty("UserId")
    private Integer UserId;
     /**
     * 关联音频自查记录表主键
     */
  	 @JsonProperty("DetectId")
    private Integer DetectId;
    /**
     * 症状首次出现时间时间范围
     */
    @JsonProperty("SymptomOccurTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> SymptomOccurTimeRange;
     /**
     * 症状类型
     */
  	 @JsonProperty("SymptomType")
    private String SymptomType;
     /**
     * 症状轻重
     */
  	 @JsonProperty("SymptomLevel")
    private Integer SymptomLevel;
     /**
     * 软删除标记
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
