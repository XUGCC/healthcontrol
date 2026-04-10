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
 * 健康科普分类表
 */
@Data
@TableName("`THealthScienceCategory`")
public class THealthScienceCategory extends BaseEntity {

    /**
     * 父级分类Id；NULL/0 表示一级分类
     */
    @JsonProperty("ParentId")
    @TableField(value="ParentId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ParentId;

    /**
     * 层级：1=一级，2=二级...
     */
    @JsonProperty("Level")
    @TableField(value="Level",updateStrategy = FieldStrategy.ALWAYS)
    private Integer Level;

    /**
     * 路径：如 /1/12/（用于快速查询子树，可选）
     */
    @JsonProperty("Path")
    @TableField(value="Path",updateStrategy = FieldStrategy.ALWAYS)
    private String Path;

      
  	  /**
     * 分类名称：如声带保护/疾病科普
     */  
    @JsonProperty("CategoryName")
    @TableField(value="CategoryName",updateStrategy = FieldStrategy.ALWAYS)
    private String CategoryName;
      
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
    private Integer ShowStatus;          
      
    /**
     * 软删除标记：0=未删除，1=已删除
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsDelete;

    /**
     * 分类图标URL（可选）
     */
    @JsonProperty("IconUrl")
    @TableField(value="IconUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String IconUrl;
  
    /**
     * 把健康科普分类实体转换成健康科普分类传输模型
     */
    public THealthScienceCategoryDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        THealthScienceCategoryDto THealthScienceCategoryDto = new THealthScienceCategoryDto();
       
        BeanUtils.copyProperties(THealthScienceCategoryDto,this);
       
        return THealthScienceCategoryDto;
    }

}
