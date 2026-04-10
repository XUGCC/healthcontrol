package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 前台模型服务相关 DTO 汇总
 */
public class FrontModelDtos {

    /**
     * 单次检测模型调用日志汇总 - 输入参数
     */
    @Data
    public static class FrontModelCallLogSummaryInput {
        @JsonProperty("DetectId")
        private Integer DetectId;
    }

    /**
     * 单次检测模型调用日志汇总 - 输出对象
     */
    @Data
    public static class FrontModelCallLogSummaryOutput {
        /**
         * 状态等级：NORMAL / WARNING / ERROR / NONE
         */
        @JsonProperty("StatusLevel")
        private String StatusLevel;

        @JsonProperty("TotalCalls")
        private Integer TotalCalls;

        @JsonProperty("SuccessCalls")
        private Integer SuccessCalls;

        @JsonProperty("FailCalls")
        private Integer FailCalls;

        /**
         * 平均耗时（毫秒）
         */
        @JsonProperty("AvgCost")
        private Integer AvgCost;

        @JsonProperty("Items")
        private List<FrontModelCallLogItem> Items;
    }

    /**
     * 单条调用日志的精简视图
     */
    @Data
    public static class FrontModelCallLogItem {
        @JsonProperty("CallLink")
        private Integer CallLink;

        @JsonProperty("CallLinkName")
        private String CallLinkName;

        @JsonProperty("ModelInterfaceType")
        private Integer ModelInterfaceType;

        @JsonProperty("SourceType")
        private String SourceType;

        @JsonProperty("ServiceName")
        private String ServiceName;

        @JsonProperty("ModelVersion")
        private String ModelVersion;

        @JsonProperty("CallTime")
        private String CallTime;

        @JsonProperty("CallStatus")
        private Boolean CallStatus;

        @JsonProperty("CallCost")
        private Integer CallCost;

        @JsonProperty("ResultSummary")
        private String ResultSummary;

        @JsonProperty("FailErrorCode")
        private String FailErrorCode;
    }
}

