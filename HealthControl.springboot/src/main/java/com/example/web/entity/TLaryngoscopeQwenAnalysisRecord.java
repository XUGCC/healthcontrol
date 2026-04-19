package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 喉镜 Qwen-VL 图像分析记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`tlaryngoscope_qwen_analysis_record`")
public class TLaryngoscopeQwenAnalysisRecord extends BaseEntity {

    @TableField(value = "UpdateTime", updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime UpdateTime;

    @TableField(value = "UserId", updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;

    @TableField(value = "LaryngoscopePhotoId", updateStrategy = FieldStrategy.ALWAYS)
    private Integer LaryngoscopePhotoId;

    @TableField(value = "SourcePhotoUrl", updateStrategy = FieldStrategy.ALWAYS)
    private String SourcePhotoUrl;

    @TableField(value = "SourcePhotoLocalPath", updateStrategy = FieldStrategy.ALWAYS)
    private String SourcePhotoLocalPath;

    @TableField(value = "AnalysisStatus", updateStrategy = FieldStrategy.ALWAYS)
    private String AnalysisStatus;

    @TableField(value = "ReportType", updateStrategy = FieldStrategy.ALWAYS)
    private String ReportType;

    @TableField(value = "RiskLevel", updateStrategy = FieldStrategy.ALWAYS)
    private String RiskLevel;

    @TableField(value = "RiskScore", updateStrategy = FieldStrategy.ALWAYS)
    private Double RiskScore;

    @TableField(value = "SummaryText", updateStrategy = FieldStrategy.ALWAYS)
    private String SummaryText;

    @TableField(value = "ReportJson", updateStrategy = FieldStrategy.ALWAYS)
    private String ReportJson;

    @TableField(value = "RawRequest", updateStrategy = FieldStrategy.ALWAYS)
    private String RawRequest;

    @TableField(value = "RawResponse", updateStrategy = FieldStrategy.ALWAYS)
    private String RawResponse;

    @TableField(value = "ModelProvider", updateStrategy = FieldStrategy.ALWAYS)
    private String ModelProvider;

    @TableField(value = "ModelName", updateStrategy = FieldStrategy.ALWAYS)
    private String ModelName;

    @TableField(value = "ModelVersion", updateStrategy = FieldStrategy.ALWAYS)
    private String ModelVersion;

    @TableField(value = "PromptVersion", updateStrategy = FieldStrategy.ALWAYS)
    private String PromptVersion;

    @TableField(value = "ParseStatus", updateStrategy = FieldStrategy.ALWAYS)
    private String ParseStatus;

    @TableField(value = "IncludeLocalPredictionContext", updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IncludeLocalPredictionContext;

    @TableField(value = "LocalPredictionRecordId", updateStrategy = FieldStrategy.ALWAYS)
    private Integer LocalPredictionRecordId;

    @TableField(value = "LocalPredictionSnapshotJson", updateStrategy = FieldStrategy.ALWAYS)
    private String LocalPredictionSnapshotJson;

    @TableField(value = "ErrorCode", updateStrategy = FieldStrategy.ALWAYS)
    private String ErrorCode;

    @TableField(value = "ErrorMessage", updateStrategy = FieldStrategy.ALWAYS)
    private String ErrorMessage;

    @TableField(value = "LatencyMs", updateStrategy = FieldStrategy.ALWAYS)
    private Integer LatencyMs;

    @TableField(value = "IsDelete", updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;
}
