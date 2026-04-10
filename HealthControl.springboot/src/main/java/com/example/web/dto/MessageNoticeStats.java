package com.example.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息通知统计信息
 */
@NoArgsConstructor
@Data
public class MessageNoticeStats {
    
    /**
     * 总数
     */
    @JsonProperty("Total")
    private Integer Total;
    
    /**
     * 已读数量
     */
    @JsonProperty("Read")
    private Integer Read;
    
    /**
     * 未读数量
     */
    @JsonProperty("Unread")
    private Integer Unread;
}
