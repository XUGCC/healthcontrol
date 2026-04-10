package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险评估题目选项查询模型
 */
@NoArgsConstructor
@Data
public class TRiskAssessmentQuestionOptionPagedInput extends PagedInput {

    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("QuestionId")
    private Integer QuestionId;

    @JsonProperty("OptionText")
    private String OptionText;

    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;
}

