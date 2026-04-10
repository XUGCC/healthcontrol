package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FrontScienceListItemDto {
    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("Title")
    private String Title;

    @JsonProperty("CoverUrl")
    private String CoverUrl;

    @JsonProperty("Summary")
    private String Summary;

    @JsonProperty("CategoryId")
    private Integer CategoryId;

    @JsonProperty("KnowledgeType")
    private Integer KnowledgeType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("PublishTime")
    private LocalDateTime PublishTime;

    @JsonProperty("ReadCount")
    private Integer ReadCount;

    @JsonProperty("LikeCount")
    private Integer LikeCount;

    @JsonProperty("CollectCount")
    private Integer CollectCount;

    @JsonProperty("CommentCount")
    private Integer CommentCount;

    @JsonProperty("IsTop")
    private Integer IsTop;

    @JsonProperty("RecommendWeight")
    private Integer RecommendWeight;

    /**
     * 作者名称（优先展示实名 Name，若为空则回退到账号 UserName）
     */
    @JsonProperty("AuthorName")
    private String AuthorName;

    /**
     * 审核状态（我的发布使用）：0=草稿，1=待审核，2=通过，3=驳回，4=下架/屏蔽
     */
    @JsonProperty("AuditStatus")
    private Integer AuditStatus;

    /**
     * 驳回原因（我的发布使用）
     */
    @JsonProperty("RejectReason")
    private String RejectReason;
}

