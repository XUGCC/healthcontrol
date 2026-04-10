package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序端：评论点赞/取消点赞切换入参
 */
@NoArgsConstructor
@Data
public class FrontScienceCommentLikeToggleInput {

    /**
     * 被点赞的评论Id
     */
    @JsonProperty("CommentId")
    private Integer CommentId;

    /**
     * LIKE/UNLIKE，不传则 toggle
     */
    @JsonProperty("Action")
    private String Action;
}

