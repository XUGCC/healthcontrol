package com.example.web.controller;

import com.example.web.dto.TRiskAssessmentQuestionDto;
import com.example.web.dto.query.TRiskAssessmentQuestionPagedInput;
import com.example.web.service.TRiskAssessmentQuestionService;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 风险评估题目控制器
 */
@RestController
@RequestMapping("/TRiskAssessmentQuestion")
public class TRiskAssessmentQuestionController {

    @Autowired
    private TRiskAssessmentQuestionService questionService;

    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TRiskAssessmentQuestionDto> List(@RequestBody TRiskAssessmentQuestionPagedInput input) {
        return questionService.List(input);
    }

    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TRiskAssessmentQuestionDto Get(@RequestBody TRiskAssessmentQuestionPagedInput input) {
        return questionService.Get(input);
    }

    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TRiskAssessmentQuestionDto CreateOrEdit(@RequestBody TRiskAssessmentQuestionDto input) {
        return questionService.CreateOrEdit(input);
    }

    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input) {
        questionService.Delete(input);
    }

    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input) {
        questionService.BatchDelete(input);
    }

    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
        questionService.Export(query, response);
    }
}

