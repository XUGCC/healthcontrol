package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 风险评估问卷题目表（triskassessmentquestion）
 */
@Data
@TableName("`TRiskAssessmentQuestion`")
public class TRiskAssessmentQuestion extends BaseEntity {

    @JsonProperty("QuestionnaireId")
    @TableField(value = "QuestionnaireId", updateStrategy = FieldStrategy.ALWAYS)
    private Integer QuestionnaireId;

    @JsonProperty("DimensionCode")
    @TableField(value = "DimensionCode", updateStrategy = FieldStrategy.ALWAYS)
    private String DimensionCode;

    @JsonProperty("QuestionTitle")
    @TableField(value = "QuestionTitle", updateStrategy = FieldStrategy.ALWAYS)
    private String QuestionTitle;

    @JsonProperty("QuestionDesc")
    @TableField(value = "QuestionDesc", updateStrategy = FieldStrategy.ALWAYS)
    private String QuestionDesc;

    /**
     * 题型：1=单选，2=多选，3=是/否，4=填空，5=量表
     */
    @JsonProperty("QuestionType")
    @TableField(value = "QuestionType", updateStrategy = FieldStrategy.ALWAYS)
    private Integer QuestionType;

    @JsonProperty("IsRequired")
    @TableField(value = "IsRequired", updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsRequired;

    @JsonProperty("SortNum")
    @TableField(value = "SortNum", updateStrategy = FieldStrategy.ALWAYS)
    private Integer SortNum;

    /**
     * 复杂计分规则（当前版本暂不使用，预留）
     */
    @JsonProperty("ScoreRuleJson")
    @TableField(value = "ScoreRuleJson", updateStrategy = FieldStrategy.ALWAYS)
    private String ScoreRuleJson;

    @JsonProperty("IsDelete")
    @TableField(value = "IsDelete", updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsDelete;
}

