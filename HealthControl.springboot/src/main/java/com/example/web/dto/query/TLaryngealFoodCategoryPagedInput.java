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
 * 喉部食物库分类查询模型
 */
@NoArgsConstructor
@Data
public class TLaryngealFoodCategoryPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 分类名称：如润喉类/辛辣类模糊查询条件
     */
  	 @JsonProperty("CategoryName")
    private String CategoryName;
    /**
     * 分类描述模糊查询条件
     */
  	 @JsonProperty("CategoryDesc")
    private String CategoryDesc;
     /**
     * 分类类型：0=友好，1=忌口
     */
  	 @JsonProperty("CategoryType")
    private Boolean CategoryType;
     /**
     * 展示状态：0=隐藏，1=展示
     */
  	 @JsonProperty("ShowStatus")
    private Boolean ShowStatus;
     /**
     * 软删除标记：0=未删除，1=已删除
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
