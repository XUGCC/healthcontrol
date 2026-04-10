package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FrontScienceMyPublishListInput {
    @JsonProperty("Page")
    private Long Page;

    @JsonProperty("Limit")
    private Long Limit;

    @JsonProperty("AuditStatus")
    private Integer AuditStatus;
}

