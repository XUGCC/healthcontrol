package com.example.web.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 模型接口调用日志统计 DTO
 */
public class ModelInterfaceStatsDtos {

    @Data
    public static class ModelInterfaceStatsSummaryInput {
        /**
         * 开始日期，格式 yyyy-MM-dd（可选，默认近 7 天）
         */
        @JsonProperty("DateStart")
        private String DateStart;

        /**
         * 结束日期，格式 yyyy-MM-dd（可选，默认近 7 天）
         */
        @JsonProperty("DateEnd")
        private String DateEnd;
    }

    @Data
    public static class ModelInterfaceStatsSummaryOutput {
        @JsonProperty("TotalCalls")
        private Long TotalCalls;

        @JsonProperty("SuccessCalls")
        private Long SuccessCalls;

        @JsonProperty("FailCalls")
        private Long FailCalls;

        /**
         * 失败率 0~1
         */
        @JsonProperty("FailRate")
        private Double FailRate;

        /**
         * 平均耗时（毫秒）
         */
        @JsonProperty("AvgCost")
        private Integer AvgCost;

        @JsonProperty("ByQualityLevel")
        private List<QualityBucket> ByQualityLevel;

        @JsonProperty("ByCallLink")
        private List<CallLinkBucket> ByCallLink;
    }

    @Data
    public static class QualityBucket {
        @JsonProperty("QualityLevel")
        private Integer QualityLevel;

        @JsonProperty("Count")
        private Long Count;
    }

    @Data
    public static class CallLinkBucket {
        @JsonProperty("CallLink")
        private Integer CallLink;

        @JsonProperty("CallLinkName")
        private String CallLinkName;

        @JsonProperty("Total")
        private Long Total;

        @JsonProperty("Fail")
        private Long Fail;

        @JsonProperty("AvgCost")
        private Integer AvgCost;
    }
}

