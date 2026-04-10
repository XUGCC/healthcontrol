package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FrontScienceReadInput {
    @JsonProperty("ScienceId")
    private Integer ScienceId;

    @JsonProperty("ClientKey")
    private String ClientKey;

    @JsonProperty("ReadScene")
    private String ReadScene;

    @JsonProperty("ReadDurationSec")
    private Integer ReadDurationSec;
}

