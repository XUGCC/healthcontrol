package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FrontScienceCategoryListInput {
    @JsonProperty("IncludeEmpty")
    private Boolean IncludeEmpty;
}

