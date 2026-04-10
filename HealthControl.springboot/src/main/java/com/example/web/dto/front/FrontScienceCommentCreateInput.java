package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FrontScienceCommentCreateInput {
    @JsonProperty("ScienceId")
    private Integer ScienceId;

    @JsonProperty("ParentCommentId")
    private Integer ParentCommentId;

    @JsonProperty("RootCommentId")
    private Integer RootCommentId;

    @JsonProperty("ReplyToUserId")
    private Integer ReplyToUserId;

    @JsonProperty("CommentContent")
    private String CommentContent;
}

