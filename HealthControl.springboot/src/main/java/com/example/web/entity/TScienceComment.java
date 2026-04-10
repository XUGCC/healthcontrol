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
 * 科普评论表
 */
@Data
@TableName("`TScienceComment`")
public class TScienceComment extends BaseEntity {

      
    /**
     * 回复审核时间
     */  
    @JsonProperty("UpdateTime")
    @TableField(value="UpdateTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;             
      
    /**
     * 关联用户表主键t_user.
     */  
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;          
      
    /**
     * 关联科普表主键
     */  
    @JsonProperty("ScienceId")
    @TableField(value="ScienceId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ScienceId;          
      
    /**
     * 父评论
     */  
    @JsonProperty("ParentCommentId")
    @TableField(value="ParentCommentId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ParentCommentId;          

    /**
     * 根评论Id（主楼Id）；主楼=自身Id，子楼=主楼Id
     */
    @JsonProperty("RootCommentId")
    @TableField(value="RootCommentId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer RootCommentId;

    /**
     * 被回复用户Id（用于展示“回复@某人”与通知）
     */
    @JsonProperty("ReplyToUserId")
    @TableField(value="ReplyToUserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ReplyToUserId;
      
  	  /**
     * 评论内容
     */  
    @JsonProperty("CommentContent")
    @TableField(value="CommentContent",updateStrategy = FieldStrategy.ALWAYS)
    private String CommentContent;
      
  	  /**
     * 回复内容
     */  
    @JsonProperty("ReplyContent")
    @TableField(value="ReplyContent",updateStrategy = FieldStrategy.ALWAYS)
    private String ReplyContent;
      
    /**
     * 审核状态
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
     * 驳回原因（对用户可见）
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
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsDelete;          
  
    /**
     * 把科普评论实体转换成科普评论传输模型
     */
    public TScienceCommentDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TScienceCommentDto TScienceCommentDto = new TScienceCommentDto();
       
        BeanUtils.copyProperties(TScienceCommentDto,this);
       
        return TScienceCommentDto;
    }

}
