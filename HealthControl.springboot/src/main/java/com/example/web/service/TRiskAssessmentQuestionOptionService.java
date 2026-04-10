package com.example.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.TRiskAssessmentQuestionOptionDto;
import com.example.web.dto.query.TRiskAssessmentQuestionOptionPagedInput;
import com.example.web.entity.TRiskAssessmentQuestionOption;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 * 风险评估题目选项 Service
 */
public interface TRiskAssessmentQuestionOptionService extends IService<TRiskAssessmentQuestionOption> {

    PagedResult<TRiskAssessmentQuestionOptionDto> List(TRiskAssessmentQuestionOptionPagedInput input);

    TRiskAssessmentQuestionOptionDto CreateOrEdit(TRiskAssessmentQuestionOptionDto input);

    TRiskAssessmentQuestionOptionDto Get(TRiskAssessmentQuestionOptionPagedInput input);

    void Delete(IdInput input);

    void BatchDelete(IdsInput input);

    void Export(@RequestParam String query, HttpServletResponse response) throws IOException;
}

