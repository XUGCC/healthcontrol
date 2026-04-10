package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 小程序端提交的单题答案
 */
@Data
public class FrontQuestionnaireAnswerItemDto {

    @JsonProperty("QuestionId")
    private Integer QuestionId;

    /**
     * 对于单选/是非题：数组长度为 1；
     * 对于多选题：可多个；
     * 对于填空题：仅取第一个元素作为文本答案。
     */
    @JsonProperty("AnswerValues")
    private List<String> AnswerValues;
}

