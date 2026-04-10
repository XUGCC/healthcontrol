package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FrontQuestionnaireDetailInput {
    @JsonProperty("QuestionnaireId")
    private Integer QuestionnaireId;
}

