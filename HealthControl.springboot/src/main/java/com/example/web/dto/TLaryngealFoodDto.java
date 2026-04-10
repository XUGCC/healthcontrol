package com.example.web.dto;
import com.example.web.enums.*;
import com.example.web.tools.dto.BaseDto;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.sql.Date;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.example.web.entity.*;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
/**
 * 喉部食物库类
 */
@Data
public class TLaryngealFoodDto extends BaseDto
{

    
     
    /**
     * 更新时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("UpdateTime")
    private LocalDateTime UpdateTime;             
    
     
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
     * 食物名称
     */ 
    @JsonProperty("FoodName")
    private String FoodName;
    
     
    /**
     * 食物别名
     */ 
    @JsonProperty("FoodAlias")
    private String FoodAlias;
    
     
    /**
     * 功效危害说明
     */ 
    @JsonProperty("EffectHarmDesc")
    private String EffectHarmDesc;
    
     
    /**
     * 忌口建议
     */ 
    @JsonProperty("SuggestContent")
    private String SuggestContent;
    
     
    /**
     * 食物配图URL
     */ 
    @JsonProperty("PicUrl")
    private String PicUrl;
    
     
    /**
     * 展示状态
     */ 
    @JsonProperty("ShowStatus")
    private Integer ShowStatus;          
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Integer IsDelete;          

     @JsonProperty("CategoryDto") 
    private TLaryngealFoodCategoryDto CategoryDto;                        
   
     @JsonProperty("CreatorDto") 
    private AppUserDto CreatorDto;                        
   
 	 /**
     * 把喉部食物库传输模型转换成喉部食物库实体
     */
    public TLaryngealFood MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TLaryngealFood TLaryngealFood= new TLaryngealFood();
     
         BeanUtils.copyProperties(TLaryngealFood,this);
        
        return TLaryngealFood;
    }

}
