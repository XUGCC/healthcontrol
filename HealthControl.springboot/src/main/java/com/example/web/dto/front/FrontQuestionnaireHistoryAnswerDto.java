package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 前台历史记录中，单题答案展示 DTO
 */
@Data
public class FrontQuestionnaireHistoryAnswerDto {

    @JsonProperty("QuestionId")
    private Integer QuestionId;

    @JsonProperty("QuestionTitle")
    private String QuestionTitle;

    @JsonProperty("QuestionType")
    private Integer QuestionType;

    /**
     * 文本化后的答案（如“从不吸烟”“偶尔吸烟”等），单选/是非/多选会拼接多个
     */
    @JsonProperty("AnswerTextList")
    private List<String> AnswerTextList;
}

