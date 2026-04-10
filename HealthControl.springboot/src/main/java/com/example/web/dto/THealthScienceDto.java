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
 * 健康科普类
 */
@Data
public class THealthScienceDto extends BaseDto
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
     * 科普标题
     */ 
    @JsonProperty("Title")
    private String Title;
    
     
    /**
     * 封面图URL
     */ 
    @JsonProperty("CoverUrl")
    private String CoverUrl;
    
     
    /**
     * 科普内容
     */ 
    @JsonProperty("ScienceContent")
    private String ScienceContent;

    /**
     * 科普摘要（列表页使用）
     */
    @JsonProperty("Summary")
    private String Summary;
    
     
    /**
     * 阅读量
     */ 
    @JsonProperty("ReadCount")
    private Integer ReadCount;          
    
     
    /**
     * 知识类型
     */ 
    @JsonProperty("KnowledgeType")
    private Integer KnowledgeType;          
    
     
    /**
     * 展示状态
     */ 
    @JsonProperty("ShowStatus")
    private Integer ShowStatus;          

    /**
     * 审核状态：0=草稿，1=待审核，2=通过，3=驳回，4=下架/屏蔽
     */
    @JsonProperty("AuditStatus")
    private Integer AuditStatus;

    /**
     * 审核人Id（管理员）
     */
    @JsonProperty("AuditorId")
    private Integer AuditorId;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("AuditTime")
    private LocalDateTime AuditTime;

    /**
     * 驳回原因（对作者可见）
     */
    @JsonProperty("RejectReason")
    private String RejectReason;

    /**
     * 审核备注（内部）
     */
    @JsonProperty("AuditRemark")
    private String AuditRemark;

    /**
     * 发布时间（通过后写入）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("PublishTime")
    private LocalDateTime PublishTime;

    /**
     * 是否置顶：0=否，1=是
     */
    @JsonProperty("IsTop")
    private Integer IsTop;

    /**
     * 推荐权重（越大越靠前）
     */
    @JsonProperty("RecommendWeight")
    private Integer RecommendWeight;

    /**
     * 点赞数（冗余，可选）
     */
    @JsonProperty("LikeCount")
    private Integer LikeCount;

    /**
     * 收藏数（冗余，可选）
     */
    @JsonProperty("CollectCount")
    private Integer CollectCount;

    /**
     * 评论数（冗余，可选）
     */
    @JsonProperty("CommentCount")
    private Integer CommentCount;
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Integer IsDelete;          

     @JsonProperty("CreatorDto") 
    private AppUserDto CreatorDto;                        
   
     @JsonProperty("CategoryDto") 
    private THealthScienceCategoryDto CategoryDto;                        
   
 	 /**
     * 把健康科普传输模型转换成健康科普实体
     */
    public THealthScience MapToEntity() throws InvocationTargetException, IllegalAccessException {
        THealthScience THealthScience= new THealthScience();
     
         BeanUtils.copyProperties(THealthScience,this);
        
        return THealthScience;
    }

}
