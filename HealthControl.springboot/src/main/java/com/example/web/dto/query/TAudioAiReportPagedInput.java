package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * AI 报告分页查询入参
 */
@Data
public class TAudioAiReportPagedInput extends PagedInput {

    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("UserId")
    private Integer UserId;

    @JsonProperty("AudioScreenRecordId")
    private Integer AudioScreenRecordId;

    @JsonProperty("ReportType")
    private String ReportType;

    @JsonProperty("RiskLevel")
    private String RiskLevel;

    @JsonProperty("IsDelete")
    private Boolean IsDelete;
}

