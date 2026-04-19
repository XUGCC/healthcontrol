package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 喉镜本地模型预测记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`tlaryngoscope_local_prediction_record`")
public class TLaryngoscopeLocalPredictionRecord extends BaseEntity {

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

    @TableField(value = "PredictionStatus", updateStrategy = FieldStrategy.ALWAYS)
    private String PredictionStatus;

    @TableField(value = "PredictedClassId", updateStrategy = FieldStrategy.ALWAYS)
    private Integer PredictedClassId;

    @TableField(value = "PredictedLabel", updateStrategy = FieldStrategy.ALWAYS)
    private String PredictedLabel;

    @TableField(value = "Confidence", updateStrategy = FieldStrategy.ALWAYS)
    private Double Confidence;

    @TableField(value = "ProbabilitiesJson", updateStrategy = FieldStrategy.ALWAYS)
    private String ProbabilitiesJson;

    @TableField(value = "HeatmapUrl", updateStrategy = FieldStrategy.ALWAYS)
    private String HeatmapUrl;

    @TableField(value = "HeatmapLocalPath", updateStrategy = FieldStrategy.ALWAYS)
    private String HeatmapLocalPath;

    @TableField(value = "ModelName", updateStrategy = FieldStrategy.ALWAYS)
    private String ModelName;

    @TableField(value = "ModelVersion", updateStrategy = FieldStrategy.ALWAYS)
    private String ModelVersion;

    @TableField(value = "ModelPath", updateStrategy = FieldStrategy.ALWAYS)
    private String ModelPath;

    @TableField(value = "ScriptPath", updateStrategy = FieldStrategy.ALWAYS)
    private String ScriptPath;

    @TableField(value = "RuntimeEnv", updateStrategy = FieldStrategy.ALWAYS)
    private String RuntimeEnv;

    @TableField(value = "RawOutputJson", updateStrategy = FieldStrategy.ALWAYS)
    private String RawOutputJson;

    @TableField(value = "ErrorCode", updateStrategy = FieldStrategy.ALWAYS)
    private String ErrorCode;

    @TableField(value = "ErrorMessage", updateStrategy = FieldStrategy.ALWAYS)
    private String ErrorMessage;

    @TableField(value = "DurationMs", updateStrategy = FieldStrategy.ALWAYS)
    private Integer DurationMs;

    @TableField(value = "IsDelete", updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;
}
