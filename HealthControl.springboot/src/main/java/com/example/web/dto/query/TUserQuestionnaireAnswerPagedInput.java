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
 * 用户问卷答题查询模型
 */
@NoArgsConstructor
@Data
public class TUserQuestionnaireAnswerPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 其他风险因素模糊查询条件
     */
  	 @JsonProperty("OtherRiskFactor")
    private String OtherRiskFactor;
     /**
     * 关联用户表主键t_user.
     */
  	 @JsonProperty("UserId")
    private Integer UserId;
    /**
     * 答题提交时间时间范围
     */
    @JsonProperty("AnswerTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> AnswerTimeRange;
     /**
     * 吸烟史
     */
  	 @JsonProperty("SmokingHistory")
    private Boolean SmokingHistory;
     /**
     * 饮酒史
     */
  	 @JsonProperty("DrinkingHistory")
    private Boolean DrinkingHistory;
     /**
     * 职业用嗓
     */
  	 @JsonProperty("OccupationalVoice")
    private Boolean OccupationalVoice;
     /**
     * 反酸症状
     */
  	 @JsonProperty("AcidReflexSymptom")
    private Boolean AcidReflexSymptom;
     /**
     * 软删除标记
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
