package com.example.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读通知数量响应DTO
 */
@NoArgsConstructor
@Data
public class UnreadCountDto {
    
    /**
     * 未读数量
     */
    @JsonProperty("Count")
    private Integer Count;
}
