package com.example.web.tools.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class IdsEnabledInput {
    @JsonProperty("Ids")
    private ArrayList<Integer> Ids;

    @JsonProperty("IsEnabled")
    private Boolean IsEnabled;
}

