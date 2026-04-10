package com.example.web.dto;

import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

import com.example.web.entity.TRiskAssessmentQuestion;

/**
 * 风险评估题目 DTO
 */
@Data
public class TRiskAssessmentQuestionDto extends BaseDto {

    @JsonProperty("QuestionnaireId")
    private Integer QuestionnaireId;

    @JsonProperty("DimensionCode")
    private String DimensionCode;

    @JsonProperty("QuestionTitle")
    private String QuestionTitle;

    @JsonProperty("QuestionDesc")
    private String QuestionDesc;

    @JsonProperty("QuestionType")
    private Integer QuestionType;

    @JsonProperty("IsRequired")
    private Integer IsRequired;

    @JsonProperty("SortNum")
    private Integer SortNum;

    @JsonProperty("ScoreRuleJson")
    private String ScoreRuleJson;

    @JsonProperty("IsDelete")
    private Integer IsDelete;

    public TRiskAssessmentQuestion MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TRiskAssessmentQuestion entity = new TRiskAssessmentQuestion();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}

