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
 * 喉部食物库分类类
 */
@Data
public class TLaryngealFoodCategoryDto extends BaseDto
{

    
     
    /**
     * 分类名称：如润喉类/辛辣类
     */ 
    @JsonProperty("CategoryName")
    private String CategoryName;
    
     
    /**
     * 分类类型：0=友好，1=忌口
     */ 
    @JsonProperty("CategoryType")
    private Boolean CategoryType;          
    
     
    /**
     * 分类描述
     */ 
    @JsonProperty("CategoryDesc")
    private String CategoryDesc;
    
     
    /**
     * 排序值：升序，值越小越靠前
     */ 
    @JsonProperty("SortNum")
    private Integer SortNum;          
    
     
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

 	 /**
     * 把喉部食物库分类传输模型转换成喉部食物库分类实体
     */
    public TLaryngealFoodCategory MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TLaryngealFoodCategory TLaryngealFoodCategory= new TLaryngealFoodCategory();
     
         BeanUtils.copyProperties(TLaryngealFoodCategory,this);
        
        return TLaryngealFoodCategory;
    }

}
