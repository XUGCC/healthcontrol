package com.example.web.dto.front;

import com.example.web.dto.THealthScienceDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FrontScienceDetailDto {
    @JsonProperty("Science")
    private THealthScienceDto Science;

    @JsonProperty("UserState")
    private FrontScienceUserStateDto UserState;
}

