package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 音频检测AI解读报告表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`taudio_ai_report`")
public class TAudioAiReport extends BaseEntity {

    @JsonProperty("UpdateTime")
    @TableField(value = "UpdateTime", updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;

    @JsonProperty("UserId")
    @TableField(value = "UserId", updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;

    @JsonProperty("AudioScreenRecordId")
    @TableField(value = "AudioScreenRecordId", updateStrategy = FieldStrategy.ALWAYS)
    private Integer AudioScreenRecordId;

    @JsonProperty("ReportType")
    @TableField(value = "ReportType", updateStrategy = FieldStrategy.ALWAYS)
    private String ReportType;

    @JsonProperty("SpectrumType")
    @TableField(value = "SpectrumType", updateStrategy = FieldStrategy.ALWAYS)
    private String SpectrumType;

    @JsonProperty("ModelProvider")
    @TableField(value = "ModelProvider", updateStrategy = FieldStrategy.ALWAYS)
    private String ModelProvider;

    @JsonProperty("ModelName")
    @TableField(value = "ModelName", updateStrategy = FieldStrategy.ALWAYS)
    private String ModelName;

    @JsonProperty("ModelVersion")
    @TableField(value = "ModelVersion", updateStrategy = FieldStrategy.ALWAYS)
    private String ModelVersion;

    @JsonProperty("Status")
    @TableField(value = "Status", updateStrategy = FieldStrategy.ALWAYS)
    private String Status;

    @JsonProperty("ParseStatus")
    @TableField(value = "ParseStatus", updateStrategy = FieldStrategy.ALWAYS)
    private String ParseStatus;

    @JsonProperty("RiskLevel")
    @TableField(value = "RiskLevel", updateStrategy = FieldStrategy.ALWAYS)
    private String RiskLevel;

    @JsonProperty("RiskScore")
    @TableField(value = "RiskScore", updateStrategy = FieldStrategy.ALWAYS)
    private Double RiskScore;

    @JsonProperty("SummaryText")
    @TableField(value = "SummaryText", updateStrategy = FieldStrategy.ALWAYS)
    private String SummaryText;

    @JsonProperty("ReportJson")
    @TableField(value = "ReportJson", updateStrategy = FieldStrategy.ALWAYS)
    private String ReportJson;

    @JsonProperty("RawRequest")
    @TableField(value = "RawRequest", updateStrategy = FieldStrategy.ALWAYS)
    private String RawRequest;

    @JsonProperty("RawResponse")
    @TableField(value = "RawResponse", updateStrategy = FieldStrategy.ALWAYS)
    private String RawResponse;

    @JsonProperty("ErrorCode")
    @TableField(value = "ErrorCode", updateStrategy = FieldStrategy.ALWAYS)
    private String ErrorCode;

    @JsonProperty("ErrorMessage")
    @TableField(value = "ErrorMessage", updateStrategy = FieldStrategy.ALWAYS)
    private String ErrorMessage;

    @JsonProperty("LatencyMs")
    @TableField(value = "LatencyMs", updateStrategy = FieldStrategy.ALWAYS)
    private Integer LatencyMs;

    @JsonProperty("IsDelete")
    @TableField(value = "IsDelete", updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;
}

