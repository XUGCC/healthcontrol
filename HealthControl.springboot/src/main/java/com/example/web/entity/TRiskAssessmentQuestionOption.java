package com.example.web.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 风险评估问卷题目选项表（triskassessmentquestionoption）
 */
@Data
@TableName("`TRiskAssessmentQuestionOption`")
public class TRiskAssessmentQuestionOption extends BaseEntity {

    @JsonProperty("QuestionId")
    @TableField(value = "QuestionId", updateStrategy = FieldStrategy.ALWAYS)
    private Integer QuestionId;

    @JsonProperty("OptionText")
    @TableField(value = "OptionText", updateStrategy = FieldStrategy.ALWAYS)
    private String OptionText;

    @JsonProperty("OptionValue")
    @TableField(value = "OptionValue", updateStrategy = FieldStrategy.ALWAYS)
    private String OptionValue;

    @JsonProperty("ScoreValue")
    @TableField(value = "ScoreValue", updateStrategy = FieldStrategy.ALWAYS)
    private Integer ScoreValue;

    @JsonProperty("SortNum")
    @TableField(value = "SortNum", updateStrategy = FieldStrategy.ALWAYS)
    private Integer SortNum;

    @JsonProperty("IsDelete")
    @TableField(value = "IsDelete", updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsDelete;
}

