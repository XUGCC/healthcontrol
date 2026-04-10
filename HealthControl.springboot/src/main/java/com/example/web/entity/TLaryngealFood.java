package com.example.web.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.poi.hpsf.Decimal;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.sql.Date;
import java.sql.Timestamp;
import lombok.Data;
import java.time.LocalDateTime;
import com.example.web.dto.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
/**
 * 喉部食物库表
 */
@Data
@TableName("`TLaryngealFood`")
public class TLaryngealFood extends BaseEntity {

      
    /**
     * 更新时间
     */  
    @JsonProperty("UpdateTime")
    @TableField(value="UpdateTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;             
      
    /**
     * 创建人ID
     */  
    @JsonProperty("CreatorId")
    @TableField(value="CreatorId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer CreatorId;          
      
    /**
     * 分类ID
     */  
    @JsonProperty("CategoryId")
    @TableField(value="CategoryId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer CategoryId;          
      
  	  /**
     * 食物名称
     */  
    @JsonProperty("FoodName")
    @TableField(value="FoodName",updateStrategy = FieldStrategy.ALWAYS)
    private String FoodName;
      
  	  /**
     * 食物别名
     */  
    @JsonProperty("FoodAlias")
    @TableField(value="FoodAlias",updateStrategy = FieldStrategy.ALWAYS)
    private String FoodAlias;
      
  	  /**
     * 功效危害说明
     */  
    @JsonProperty("EffectHarmDesc")
    @TableField(value="EffectHarmDesc",updateStrategy = FieldStrategy.ALWAYS)
    private String EffectHarmDesc;
      
  	  /**
     * 忌口建议
     */  
    @JsonProperty("SuggestContent")
    @TableField(value="SuggestContent",updateStrategy = FieldStrategy.ALWAYS)
    private String SuggestContent;
      
  	  /**
     * 食物配图URL
     */  
    @JsonProperty("PicUrl")
    @TableField(value="PicUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String PicUrl;
      
    /**
     * 展示状态
     */  
    @JsonProperty("ShowStatus")
    @TableField(value="ShowStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ShowStatus;          
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsDelete;          
  
    /**
     * 把喉部食物库实体转换成喉部食物库传输模型
     */
    public TLaryngealFoodDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TLaryngealFoodDto TLaryngealFoodDto = new TLaryngealFoodDto();
       
        BeanUtils.copyProperties(TLaryngealFoodDto,this);
       
        return TLaryngealFoodDto;
    }

}
