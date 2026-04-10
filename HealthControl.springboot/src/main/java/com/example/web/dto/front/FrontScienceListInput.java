package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FrontScienceListInput {
    @JsonProperty("Page")
    private Long Page;

    @JsonProperty("Limit")
    private Long Limit;

    @JsonProperty("CategoryId")
    private Integer CategoryId;

    @JsonProperty("Keyword")
    private String Keyword;

    @JsonProperty("KnowledgeType")
    private Integer KnowledgeType;

    /**
     * RECOMMEND/LATEST/HOT_7D/HOT_30D（当前实现：RECOMMEND/LATEST）
     */
    @JsonProperty("Sort")
    private String Sort;
}

