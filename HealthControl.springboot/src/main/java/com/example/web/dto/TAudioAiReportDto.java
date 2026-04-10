package com.example.web.dto;

import com.example.web.entity.TAudioAiReport;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

/**
 * 音频检测AI解读报告 DTO
 */
@Data
public class TAudioAiReportDto {

    @JsonProperty("Id")
    private Integer Id;

    @JsonProperty("CreationTime")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime CreationTime;

    @JsonProperty("UpdateTime")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;

    @JsonProperty("UserId")
    private Integer UserId;

    @JsonProperty("AudioScreenRecordId")
    private Integer AudioScreenRecordId;

    @JsonProperty("ReportType")
    private String ReportType;

    @JsonProperty("SpectrumType")
    private String SpectrumType;

    @JsonProperty("ModelProvider")
    private String ModelProvider;

    @JsonProperty("ModelName")
    private String ModelName;

    @JsonProperty("ModelVersion")
    private String ModelVersion;

    @JsonProperty("Status")
    private String Status;

    @JsonProperty("ParseStatus")
    private String ParseStatus;

    @JsonProperty("RiskLevel")
    private String RiskLevel;

    @JsonProperty("RiskScore")
    private Double RiskScore;

    @JsonProperty("SummaryText")
    private String SummaryText;

    @JsonProperty("ReportJson")
    private String ReportJson;

    @JsonProperty("RawRequest")
    private String RawRequest;

    @JsonProperty("RawResponse")
    private String RawResponse;

    @JsonProperty("ErrorCode")
    private String ErrorCode;

    @JsonProperty("ErrorMessage")
    private String ErrorMessage;

    @JsonProperty("LatencyMs")
    private Integer LatencyMs;

    @JsonProperty("IsDelete")
    private Boolean IsDelete;

    public TAudioAiReport MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TAudioAiReport entity = new TAudioAiReport();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}

