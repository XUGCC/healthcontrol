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
 * 健康科普表
 */
@Data
@TableName("`THealthScience`")
public class THealthScience extends BaseEntity {

      
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
     * 科普标题
     */  
    @JsonProperty("Title")
    @TableField(value="Title",updateStrategy = FieldStrategy.ALWAYS)
    private String Title;
      
  	  /**
     * 封面图URL
     */  
    @JsonProperty("CoverUrl")
    @TableField(value="CoverUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String CoverUrl;
      
  	  /**
     * 科普内容
     */  
    @JsonProperty("ScienceContent")
    @TableField(value="ScienceContent",updateStrategy = FieldStrategy.ALWAYS)
    private String ScienceContent;

    /**
     * 科普摘要（列表页使用）
     */
    @JsonProperty("Summary")
    @TableField(value="Summary",updateStrategy = FieldStrategy.ALWAYS)
    private String Summary;
      
    /**
     * 阅读量
     */  
    @JsonProperty("ReadCount")
    @TableField(value="ReadCount",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ReadCount;          
      
    /**
     * 知识类型
     */  
    @JsonProperty("KnowledgeType")
    @TableField(value="KnowledgeType",updateStrategy = FieldStrategy.ALWAYS)
    private Integer KnowledgeType;          
      
    /**
     * 展示状态
     */  
    @JsonProperty("ShowStatus")
    @TableField(value="ShowStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ShowStatus;          

    /**
     * 审核状态：0=草稿，1=待审核，2=通过，3=驳回，4=下架/屏蔽
     */
    @JsonProperty("AuditStatus")
    @TableField(value="AuditStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Integer AuditStatus;

    /**
     * 审核人Id（管理员）
     */
    @JsonProperty("AuditorId")
    @TableField(value="AuditorId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer AuditorId;

    /**
     * 审核时间
     */
    @JsonProperty("AuditTime")
    @TableField(value="AuditTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime AuditTime;

    /**
     * 驳回原因（对作者可见）
     */
    @JsonProperty("RejectReason")
    @TableField(value="RejectReason",updateStrategy = FieldStrategy.ALWAYS)
    private String RejectReason;

    /**
     * 审核备注（内部）
     */
    @JsonProperty("AuditRemark")
    @TableField(value="AuditRemark",updateStrategy = FieldStrategy.ALWAYS)
    private String AuditRemark;

    /**
     * 发布时间（通过后写入）
     */
    @JsonProperty("PublishTime")
    @TableField(value="PublishTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime PublishTime;

    /**
     * 是否置顶：0=否，1=是
     */
    @JsonProperty("IsTop")
    @TableField(value="IsTop",updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsTop;

    /**
     * 推荐权重（越大越靠前）
     */
    @JsonProperty("RecommendWeight")
    @TableField(value="RecommendWeight",updateStrategy = FieldStrategy.ALWAYS)
    private Integer RecommendWeight;

    /**
     * 点赞数（冗余，可选）
     */
    @JsonProperty("LikeCount")
    @TableField(value="LikeCount",updateStrategy = FieldStrategy.ALWAYS)
    private Integer LikeCount;

    /**
     * 收藏数（冗余，可选）
     */
    @JsonProperty("CollectCount")
    @TableField(value="CollectCount",updateStrategy = FieldStrategy.ALWAYS)
    private Integer CollectCount;

    /**
     * 评论数（冗余，可选）
     */
    @JsonProperty("CommentCount")
    @TableField(value="CommentCount",updateStrategy = FieldStrategy.ALWAYS)
    private Integer CommentCount;
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsDelete;          
  
    /**
     * 把健康科普实体转换成健康科普传输模型
     */
    public THealthScienceDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        THealthScienceDto THealthScienceDto = new THealthScienceDto();
       
        BeanUtils.copyProperties(THealthScienceDto,this);
       
        return THealthScienceDto;
    }

}
