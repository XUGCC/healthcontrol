package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FrontScienceToggleResultDto {
    @JsonProperty("State")
    private Boolean State;

    @JsonProperty("Count")
    private Integer Count;
}

