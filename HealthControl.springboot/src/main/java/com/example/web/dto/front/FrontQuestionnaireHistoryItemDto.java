package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 前台展示的单条问卷历史记录 DTO
 */
@Data
public class FrontQuestionnaireHistoryItemDto {

    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("QuestionnaireId")
    private Integer QuestionnaireId;

    @JsonProperty("QuestionnaireTitle")
    private String QuestionnaireTitle;

    @JsonProperty("RiskAssessmentScore")
    private Double RiskAssessmentScore;

    @JsonProperty("RiskLevel")
    private Integer RiskLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("AnswerTime")
    private LocalDateTime AnswerTime;

    @JsonProperty("ResultSummary")
    private String ResultSummary;

    /**
     * 题目 + 文本化后的答案列表
     */
    @JsonProperty("Answers")
    private List<FrontQuestionnaireHistoryAnswerDto> Answers;
}

