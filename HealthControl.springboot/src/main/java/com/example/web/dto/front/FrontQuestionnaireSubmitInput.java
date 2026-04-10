package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class FrontQuestionnaireSubmitInput {
    @JsonProperty("QuestionnaireId")
    private Integer QuestionnaireId;

    /**
     * 通用题目答案列表
     */
    @JsonProperty("Answers")
    private List<FrontQuestionnaireAnswerItemDto> Answers;
}

