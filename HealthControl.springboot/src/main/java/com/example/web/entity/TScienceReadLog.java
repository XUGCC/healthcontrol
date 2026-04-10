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

import java.time.LocalDateTime;

/**
 * 科普阅读去重日志表
 */
@Data
@TableName("`TScienceReadLog`")
public class TScienceReadLog extends BaseEntity {

    @JsonProperty("UserId")
    @TableField(value = "UserId", updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;

    @JsonProperty("ClientKey")
    @TableField(value = "ClientKey", updateStrategy = FieldStrategy.ALWAYS)
    private String ClientKey;

    @JsonProperty("ScienceId")
    @TableField(value = "ScienceId", updateStrategy = FieldStrategy.ALWAYS)
    private Integer ScienceId;

    @JsonProperty("WindowStartTime")
    @TableField(value = "WindowStartTime", updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime WindowStartTime;

    @JsonProperty("ReadScene")
    @TableField(value = "ReadScene", updateStrategy = FieldStrategy.ALWAYS)
    private String ReadScene;

    @JsonProperty("ReadDurationSec")
    @TableField(value = "ReadDurationSec", updateStrategy = FieldStrategy.ALWAYS)
    private Integer ReadDurationSec;

    @JsonProperty("IsDelete")
    @TableField(value = "IsDelete", updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsDelete;
}

