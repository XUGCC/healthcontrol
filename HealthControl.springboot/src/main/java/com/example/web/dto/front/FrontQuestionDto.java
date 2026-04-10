package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FrontQuestionDto {

    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("QuestionTitle")
    private String QuestionTitle;

    @JsonProperty("QuestionDesc")
    private String QuestionDesc;

    /**
     * 题型：1=单选，2=多选，3=是/否，4=填空，5=量表
     */
    @JsonProperty("QuestionType")
    private Integer QuestionType;

    @JsonProperty("IsRequired")
    private Integer IsRequired;

    @JsonProperty("Options")
    private List<FrontQuestionOptionDto> Options;
}

