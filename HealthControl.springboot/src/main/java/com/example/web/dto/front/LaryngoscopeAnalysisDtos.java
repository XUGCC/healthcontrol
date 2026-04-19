package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 喉镜影像档案：本地模型预测与 Qwen-VL 图像分析 DTO
 */
public class LaryngoscopeAnalysisDtos {

    @Data
    public static class LocalPredictInput {
        @JsonProperty("Id")
        private Integer Id;

        @JsonProperty("ForceRegenerate")
        private Boolean ForceRegenerate;
    }

    @Data
    public static class QwenAnalyzeInput {
        @JsonProperty("Id")
        private Integer Id;

        @JsonProperty("ForceRegenerate")
        private Boolean ForceRegenerate;

        @JsonProperty("IncludeLocalPredictionContext")
        private Boolean IncludeLocalPredictionContext;
    }

    @Data
    public static class LocalPredictionOutput {
        @JsonProperty("PredictionId")
        private Integer PredictionId;

        @JsonProperty("LaryngoscopePhotoId")
        private Integer LaryngoscopePhotoId;

        @JsonProperty("Status")
        private String Status;

        @JsonProperty("PredictedClassId")
        private Integer PredictedClassId;

        @JsonProperty("PredictedLabel")
        private String PredictedLabel;

        @JsonProperty("Confidence")
        private Double Confidence;

        @JsonProperty("Probabilities")
        private List<Double> Probabilities;

        @JsonProperty("ProbabilitiesJson")
        private String ProbabilitiesJson;

        @JsonProperty("HeatmapUrl")
        private String HeatmapUrl;

        @JsonProperty("ModelName")
        private String ModelName;

        @JsonProperty("ModelVersion")
        private String ModelVersion;

        @JsonProperty("CreatedTime")
        private String CreatedTime;

        @JsonProperty("ErrorCode")
        private String ErrorCode;

        @JsonProperty("ErrorMessage")
        private String ErrorMessage;
    }

    @Data
    public static class QwenAnalysisOutput {
        @JsonProperty("AnalysisId")
        private Integer AnalysisId;

        @JsonProperty("LaryngoscopePhotoId")
        private Integer LaryngoscopePhotoId;

        @JsonProperty("Status")
        private String Status;

        @JsonProperty("RiskLevel")
        private String RiskLevel;

        @JsonProperty("SummaryText")
        private String SummaryText;

        @JsonProperty("ReportJson")
        private String ReportJson;

        @JsonProperty("ModelName")
        private String ModelName;

        @JsonProperty("PromptVersion")
        private String PromptVersion;

        @JsonProperty("IncludeLocalPredictionContext")
        private Boolean IncludeLocalPredictionContext;

        @JsonProperty("LocalPredictionRecordId")
        private Integer LocalPredictionRecordId;

        @JsonProperty("CreatedTime")
        private String CreatedTime;

        @JsonProperty("ErrorCode")
        private String ErrorCode;

        @JsonProperty("ErrorMessage")
        private String ErrorMessage;
    }
}
