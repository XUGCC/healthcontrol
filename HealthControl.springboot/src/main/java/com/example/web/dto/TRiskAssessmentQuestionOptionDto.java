package com.example.web.dto;

import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

import com.example.web.entity.TRiskAssessmentQuestionOption;

/**
 * 风险评估题目选项 DTO
 */
@Data
public class TRiskAssessmentQuestionOptionDto extends BaseDto {

    @JsonProperty("QuestionId")
    private Integer QuestionId;

    @JsonProperty("OptionText")
    private String OptionText;

    @JsonProperty("OptionValue")
    private String OptionValue;

    @JsonProperty("ScoreValue")
    private Integer ScoreValue;

    @JsonProperty("SortNum")
    private Integer SortNum;

    @JsonProperty("IsDelete")
    private Integer IsDelete;

    public TRiskAssessmentQuestionOption MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TRiskAssessmentQuestionOption entity = new TRiskAssessmentQuestionOption();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}

