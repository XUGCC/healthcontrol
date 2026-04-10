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
 * 个人喉部健康档案查询模型
 */
@NoArgsConstructor
@Data
public class TPersonalLaryngealHealthRecordPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 近期主要不适症状模糊查询条件
     */
  	 @JsonProperty("RecentSymptom")
    private String RecentSymptom;
    /**
     * 症状持续时间：如1周/1月模糊查询条件
     */
  	 @JsonProperty("SymptomDuration")
    private String SymptomDuration;
    /**
     * 日常护嗓习惯模糊查询条件
     */
  	 @JsonProperty("DailyVoiceCare")
    private String DailyVoiceCare;
    /**
     * 健康趋势标签：如好转/恶化/稳定模糊查询条件
     */
  	 @JsonProperty("HealthTrendTag")
    private String HealthTrendTag;
     /**
     * 关联用户表主键t_user.
     */
  	 @JsonProperty("UserId")
    private Integer UserId;
     /**
     * 关联最新检测
     */
  	 @JsonProperty("LatestDetectId")
    private Integer LatestDetectId;
    /**
     * 末次自查时间时间范围
     */
    @JsonProperty("LastScreenTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> LastScreenTimeRange;
     /**
     * 风险评估等级：0=低，1=中，2=高
     */
  	 @JsonProperty("RiskAssessmentLevel")
    private Integer RiskAssessmentLevel;
     /**
     * 软删除标记：0=未删除，1=已删除
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
