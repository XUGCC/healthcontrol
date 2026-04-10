package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import lombok.EqualsAndHashCode;

/**
 * 健康科普查询模型
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class THealthSciencePagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 科普标题模糊查询条件
     */
  	 @JsonProperty("Title")
    private String Title;
     /**
     * 创建人ID
     */
  	 @JsonProperty("CreatorId")
    private Integer CreatorId;
     /**
     * 分类ID
     */
  	 @JsonProperty("CategoryId")
    private Integer CategoryId;
    /**
     * 更新时间时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;

    /**
     * 审核状态筛选：0=草稿，1=待审核，2=通过，3=驳回，4=下架/屏蔽
     */
    @JsonProperty("AuditStatus")
    private Integer AuditStatus;

}
