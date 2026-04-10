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
 * 风险评估问卷查询模型
 */
@NoArgsConstructor
@Data
public class TRiskAssessmentQuestionnairePagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 问卷标题模糊查询条件
     */
  	 @JsonProperty("QuestionnaireTitle")
    private String QuestionnaireTitle;
    /**
     * 问卷描述模糊查询条件
     */
  	 @JsonProperty("QuestionnaireDesc")
    private String QuestionnaireDesc;
     /**
     * 创建人ID
     */
  	 @JsonProperty("CreatorId")
    private Integer CreatorId;
    /**
     * 更新时间时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;

}
