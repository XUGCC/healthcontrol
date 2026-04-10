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
 * 健康科普分类类
 */
@Data
public class THealthScienceCategoryDto extends BaseDto
{

    /**
     * 父级分类Id；NULL/0 表示一级分类
     */
    @JsonProperty("ParentId")
    private Integer ParentId;

    /**
     * 层级：1=一级，2=二级...
     */
    @JsonProperty("Level")
    private Integer Level;

    /**
     * 路径：如 /1/12/（用于快速查询子树，可选）
     */
    @JsonProperty("Path")
    private String Path;
    
     
    /**
     * 分类名称：如声带保护/疾病科普
     */ 
    @JsonProperty("CategoryName")
    private String CategoryName;
    
     
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
    private Integer ShowStatus;          
    
     
    /**
     * 软删除标记：0=未删除，1=已删除
     */ 
    @JsonProperty("IsDelete")
    private Integer IsDelete;

    /**
     * 分类图标URL（可选）
     */
    @JsonProperty("IconUrl")
    private String IconUrl;

 	 /**
     * 把健康科普分类传输模型转换成健康科普分类实体
     */
    public THealthScienceCategory MapToEntity() throws InvocationTargetException, IllegalAccessException {
        THealthScienceCategory THealthScienceCategory= new THealthScienceCategory();
     
         BeanUtils.copyProperties(THealthScienceCategory,this);
        
        return THealthScienceCategory;
    }

}
