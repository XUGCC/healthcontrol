package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FrontScienceCategoryNodeDto {
    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("CategoryName")
    private String CategoryName;

    @JsonProperty("CategoryDesc")
    private String CategoryDesc;

    @JsonProperty("SortNum")
    private Integer SortNum;

    @JsonProperty("ShowStatus")
    private Integer ShowStatus;

    @JsonProperty("IsDelete")
    private Integer IsDelete;

    @JsonProperty("ParentId")
    private Integer ParentId;

    @JsonProperty("Level")
    private Integer Level;

    @JsonProperty("Path")
    private String Path;

    @JsonProperty("IconUrl")
    private String IconUrl;

    @JsonProperty("Children")
    private List<FrontScienceCategoryNodeDto> Children = new ArrayList<>();
}

