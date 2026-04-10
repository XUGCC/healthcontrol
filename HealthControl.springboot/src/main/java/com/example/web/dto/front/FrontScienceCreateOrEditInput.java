package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FrontScienceCreateOrEditInput {
    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("Title")
    private String Title;

    @JsonProperty("CoverUrl")
    private String CoverUrl;

    @JsonProperty("CategoryId")
    private Integer CategoryId;

    @JsonProperty("KnowledgeType")
    private Integer KnowledgeType;

    @JsonProperty("ScienceContent")
    private String ScienceContent;

    @JsonProperty("Summary")
    private String Summary;

    /**
     * DRAFT / SUBMIT
     */
    @JsonProperty("SubmitType")
    private String SubmitType;
}

