package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * 健康科普分类查询模型
 */
@NoArgsConstructor
@Data
public class THealthScienceCategoryPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 分类名称：如声带保护/疾病科普模糊查询条件
     */
  	 @JsonProperty("CategoryName")
    private String CategoryName;
    /**
     * 分类描述模糊查询条件
     */
  	 @JsonProperty("CategoryDesc")
    private String CategoryDesc;
     /**
     * 展示状态：0=隐藏，1=展示
     */
  	 @JsonProperty("ShowStatus")
    private Integer ShowStatus;
     /**
     * 软删除标记：0=未删除，1=已删除
     */
  	 @JsonProperty("IsDelete")
    private Integer IsDelete;

}
