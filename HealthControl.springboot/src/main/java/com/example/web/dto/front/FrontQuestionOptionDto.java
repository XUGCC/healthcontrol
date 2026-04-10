package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FrontQuestionOptionDto {

    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("OptionText")
    private String OptionText;

    @JsonProperty("OptionValue")
    private String OptionValue;

    @JsonProperty("ScoreValue")
    private Integer ScoreValue;
}

