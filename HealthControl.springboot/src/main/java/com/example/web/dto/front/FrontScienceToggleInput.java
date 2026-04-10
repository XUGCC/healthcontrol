package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FrontScienceToggleInput {
    @JsonProperty("ScienceId")
    private Integer ScienceId;

    /**
     * LIKE/UNLIKE 或 COLLECT/UNCOLLECT；不传则 toggle
     */
    @JsonProperty("Action")
    private String Action;
}

