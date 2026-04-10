package com.example.web.dto;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.web.entity.*;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import lombok.EqualsAndHashCode;
/**
 * 科普评论类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TScienceCommentDto extends BaseDto
{

    
     
    /**
     * 回复审核时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("UpdateTime")
    private LocalDateTime UpdateTime;             
    
     
    /**
     * 关联用户表主键t_user.
     */ 
    @JsonProperty("UserId")
    private Integer UserId;          
    
     
    /**
     * 关联科普表主键
     */ 
    @JsonProperty("ScienceId")
    private Integer ScienceId;          
    
     
    /**
     * 父评论
     */ 
    @JsonProperty("ParentCommentId")
    private Integer ParentCommentId;          

    /**
     * 根评论Id（主楼Id）；主楼=自身Id，子楼=主楼Id
     */
    @JsonProperty("RootCommentId")
    private Integer RootCommentId;

    /**
     * 被回复用户Id（用于展示“回复@某人”与通知）
     */
    @JsonProperty("ReplyToUserId")
    private Integer ReplyToUserId;

    /**
     * 主楼的子回复数（用于前台展示/展开）
     */
    @JsonProperty("ReplyCount")
    private Integer ReplyCount;

    /**
     * 子回复预览（默认 2 条，可按需展开再拉取）
     */
    @JsonProperty("Children")
    private List<TScienceCommentDto> Children;
    
     
    /**
     * 评论内容
     */ 
    @JsonProperty("CommentContent")
    private String CommentContent;
    
     
    /**
     * 回复内容
     */ 
    @JsonProperty("ReplyContent")
    private String ReplyContent;
    
     
    /**
     * 审核状态
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
     * 驳回原因（对用户可见）
     */
    @JsonProperty("RejectReason")
    private String RejectReason;

    /**
     * 审核备注（内部）
     */
    @JsonProperty("AuditRemark")
    private String AuditRemark;
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Integer IsDelete;          

    /**
     * 点赞数量（前台展示用）
     */
    @JsonProperty("LikeCount")
    private Integer LikeCount;

    /**
     * 当前登录用户是否已点赞该评论
     */
    @JsonProperty("IsLiked")
    private Boolean IsLiked;

     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
     @JsonProperty("ScienceDto") 
    private THealthScienceDto ScienceDto;                        
   
     @JsonProperty("ParentCommentDto") 
    private TScienceCommentDto ParentCommentDto;                        
   
 	 /**
     * 把科普评论传输模型转换成科普评论实体
     */
    public TScienceComment MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TScienceComment TScienceComment= new TScienceComment();
     
         BeanUtils.copyProperties(TScienceComment,this);
        
        return TScienceComment;
    }

}
