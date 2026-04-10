package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FrontLatestQuestionnaireResultDto {
    @JsonProperty("QuestionnaireId")
    private Integer QuestionnaireId;

    @JsonProperty("Score")
    private Double Score;

    @JsonProperty("RiskAssessmentLevel")
    private Integer RiskAssessmentLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("AnswerTime")
    private LocalDateTime AnswerTime;
}

