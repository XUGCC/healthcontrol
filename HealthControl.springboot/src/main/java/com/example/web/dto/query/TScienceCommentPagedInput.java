package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * 科普评论查询模型
 */
@NoArgsConstructor
@Data
public class TScienceCommentPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 评论内容模糊查询条件
     */
  	 @JsonProperty("CommentContent")
    private String CommentContent;
    /**
     * 回复内容模糊查询条件
     */
  	 @JsonProperty("ReplyContent")
    private String ReplyContent;
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
     * 回复审核时间时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;
     /**
     * 审核状态
     */
  	 @JsonProperty("AuditStatus")
    private Integer AuditStatus;
     /**
     * 软删除标记
     */
  	 @JsonProperty("IsDelete")
    private Integer IsDelete;

}
