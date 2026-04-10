package com.example.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.TRiskAssessmentQuestionDto;
import com.example.web.dto.query.TRiskAssessmentQuestionPagedInput;
import com.example.web.entity.TRiskAssessmentQuestion;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 * 风险评估题目 Service
 */
public interface TRiskAssessmentQuestionService extends IService<TRiskAssessmentQuestion> {

    PagedResult<TRiskAssessmentQuestionDto> List(TRiskAssessmentQuestionPagedInput input);

    TRiskAssessmentQuestionDto CreateOrEdit(TRiskAssessmentQuestionDto input);

    TRiskAssessmentQuestionDto Get(TRiskAssessmentQuestionPagedInput input);

    void Delete(IdInput input);

    void BatchDelete(IdsInput input);

    void Export(@RequestParam String query, HttpServletResponse response) throws IOException;
}

