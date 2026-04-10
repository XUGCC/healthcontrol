package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FrontScienceReadResultDto {
    @JsonProperty("ReadCount")
    private Integer ReadCount;

    @JsonProperty("Deduped")
    private Boolean Deduped;
}

