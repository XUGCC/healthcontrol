package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FrontScienceCommentListInput {
    @JsonProperty("ScienceId")
    private Integer ScienceId;

    @JsonProperty("Page")
    private Long Page;

    @JsonProperty("Limit")
    private Long Limit;

    /**
     * 为空：主楼分页；不为空：拉取该主楼下子评论
     */
    @JsonProperty("ParentCommentId")
    private Integer ParentCommentId;

    /**
     * 子评论预览条数（仅当 ParentCommentId 为空，即查询主楼时使用）
     */
    @JsonProperty("PreviewLimit")
    private Long PreviewLimit;
}

