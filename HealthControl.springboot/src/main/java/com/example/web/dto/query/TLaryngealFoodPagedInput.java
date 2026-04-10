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
 * 喉部食物库查询模型
 */
@NoArgsConstructor
@Data
public class TLaryngealFoodPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 食物名称模糊查询条件
     */
  	 @JsonProperty("FoodName")
    private String FoodName;
    /**
     * 食物别名模糊查询条件
     */
  	 @JsonProperty("FoodAlias")
    private String FoodAlias;
    /**
     * 功效危害说明模糊查询条件
     */
  	 @JsonProperty("EffectHarmDesc")
    private String EffectHarmDesc;
    /**
     * 忌口建议模糊查询条件
     */
  	 @JsonProperty("SuggestContent")
    private String SuggestContent;
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

}
