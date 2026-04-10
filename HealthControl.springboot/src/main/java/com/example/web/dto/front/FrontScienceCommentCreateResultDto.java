package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FrontScienceCommentCreateResultDto {
    @JsonProperty("CommentId")
    private Integer CommentId;

    @JsonProperty("AuditStatus")
    private Integer AuditStatus;

    @JsonProperty("MsgTip")
    private String MsgTip;
}

