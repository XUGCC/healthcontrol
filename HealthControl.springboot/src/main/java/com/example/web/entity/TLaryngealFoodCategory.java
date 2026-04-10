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
 * 喉部食物库分类表
 */
@Data
@TableName("`TLaryngealFoodCategory`")
public class TLaryngealFoodCategory extends BaseEntity {

      
  	  /**
     * 分类名称：如润喉类/辛辣类
     */  
    @JsonProperty("CategoryName")
    @TableField(value="CategoryName",updateStrategy = FieldStrategy.ALWAYS)
    private String CategoryName;
      
    /**
     * 分类类型：0=友好，1=忌口
     */  
    @JsonProperty("CategoryType")
    @TableField(value="CategoryType",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean CategoryType;          
      
  	  /**
     * 分类描述
     */  
    @JsonProperty("CategoryDesc")
    @TableField(value="CategoryDesc",updateStrategy = FieldStrategy.ALWAYS)
    private String CategoryDesc;
      
    /**
     * 排序值：升序，值越小越靠前
     */  
    @JsonProperty("SortNum")
    @TableField(value="SortNum",updateStrategy = FieldStrategy.ALWAYS)
    private Integer SortNum;          
      
    /**
     * 展示状态：0=隐藏，1=展示
     */  
    @JsonProperty("ShowStatus")
    @TableField(value="ShowStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean ShowStatus;          
      
    /**
     * 软删除标记：0=未删除，1=已删除
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把喉部食物库分类实体转换成喉部食物库分类传输模型
     */
    public TLaryngealFoodCategoryDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TLaryngealFoodCategoryDto TLaryngealFoodCategoryDto = new TLaryngealFoodCategoryDto();
       
        BeanUtils.copyProperties(TLaryngealFoodCategoryDto,this);
       
        return TLaryngealFoodCategoryDto;
    }

}
