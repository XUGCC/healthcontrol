package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 前台 AI 服务相关 DTO 汇总（Qwen-VL 多模态）
 */
public class FrontAIDtos {

    /**
     * 单条检测记录生成/获取 AI 报告 - 入参
     */
    @Data
    public static class FrontAIAnalyzeAudioRecordInput {
        @JsonProperty("RecordId")
        private Integer RecordId;

        /**
         * 是否强制重新生成（默认 false：若已有最新成功报告则直接返回）
         */
        @JsonProperty("ForceRegenerate")
        private Boolean ForceRegenerate;

        /**
         * 图谱类型：MEL/MFCC/BOTH（默认 MEL）
         */
        @JsonProperty("SpectrumType")
        private String SpectrumType;
    }

    /**
     * 上传图谱进行解读 - 入参（除文件外）
     */
    @Data
    public static class FrontAIAnalyzeSpectrumInput {
        /**
         * 图谱类型：MEL/MFCC（默认 MEL）
         */
        @JsonProperty("SpectrumType")
        private String SpectrumType;
    }

    /**
     * AI 报告输出（通用）
     */
    @Data
    public static class FrontAIReportOutput {
        @JsonProperty("ReportId")
        private Integer ReportId;

        @JsonProperty("RecordId")
        private Integer RecordId;

        @JsonProperty("ReportType")
        private String ReportType;

        @JsonProperty("SpectrumType")
        private String SpectrumType;

        @JsonProperty("RiskLevel")
        private String RiskLevel;

        @JsonProperty("RiskScore")
        private Double RiskScore;

        @JsonProperty("SummaryText")
        private String SummaryText;

        /**
         * 结构化 JSON 字符串（前端可直接 JSON.parse）
         */
        @JsonProperty("ReportJson")
        private String ReportJson;

        @JsonProperty("CreatedTime")
        private String CreatedTime;

        @JsonProperty("ModelName")
        private String ModelName;

        @JsonProperty("Status")
        private String Status;

        @JsonProperty("ErrorCode")
        private String ErrorCode;

        @JsonProperty("ErrorMessage")
        private String ErrorMessage;
    }

    /**
     * 医疗咨询对话 - 入参
     */
    @Data
    public static class FrontAIChatInput {
        @JsonProperty("Question")
        private String Question;

        @JsonProperty("BindRecordId")
        private Integer BindRecordId;

        /**
         * 前端传入的会话历史（可选，最多建议 6-10 轮）
         */
        @JsonProperty("History")
        private List<FrontAIChatMessage> History;
    }

    /**
     * 医疗咨询对话 - 单条消息
     */
    @Data
    public static class FrontAIChatMessage {
        @JsonProperty("Role")
        private String Role; // user/assistant

        @JsonProperty("Content")
        private String Content;
    }

    /**
     * 医疗咨询对话 - 输出
     */
    @Data
    public static class FrontAIChatOutput {
        @JsonProperty("Answer")
        private String Answer;

        @JsonProperty("Disclaimer")
        private String Disclaimer;
    }
}

