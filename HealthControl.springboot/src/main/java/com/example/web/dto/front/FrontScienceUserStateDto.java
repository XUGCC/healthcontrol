package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FrontScienceUserStateDto {
    @JsonProperty("IsLiked")
    private Boolean IsLiked;

    @JsonProperty("IsCollected")
    private Boolean IsCollected;
}

