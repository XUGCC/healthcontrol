package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FrontQuestionnaireDetailDto {

    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("QuestionnaireTitle")
    private String QuestionnaireTitle;

    @JsonProperty("QuestionnaireDesc")
    private String QuestionnaireDesc;

    @JsonProperty("QuestionCount")
    private Integer QuestionCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("UpdateTime")
    private LocalDateTime UpdateTime;

    @JsonProperty("Questions")
    private List<FrontQuestionDto> Questions;
}

