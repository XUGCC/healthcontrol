package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FrontQuestionnaireSubmitResultDto {
    @JsonProperty("Score")
    private Double Score;

    @JsonProperty("RiskAssessmentLevel")
    private Integer RiskAssessmentLevel;

    @JsonProperty("ResultSummary")
    private String ResultSummary;
}

