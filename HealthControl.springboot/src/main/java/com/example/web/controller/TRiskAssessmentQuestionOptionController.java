package com.example.web.controller;

import com.example.web.dto.TRiskAssessmentQuestionOptionDto;
import com.example.web.dto.query.TRiskAssessmentQuestionOptionPagedInput;
import com.example.web.service.TRiskAssessmentQuestionOptionService;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 风险评估题目选项控制器
 */
@RestController
@RequestMapping("/TRiskAssessmentQuestionOption")
public class TRiskAssessmentQuestionOptionController {

    @Autowired
    private TRiskAssessmentQuestionOptionService optionService;

    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TRiskAssessmentQuestionOptionDto> List(@RequestBody TRiskAssessmentQuestionOptionPagedInput input) {
        return optionService.List(input);
    }

    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TRiskAssessmentQuestionOptionDto Get(@RequestBody TRiskAssessmentQuestionOptionPagedInput input) {
        return optionService.Get(input);
    }

    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TRiskAssessmentQuestionOptionDto CreateOrEdit(@RequestBody TRiskAssessmentQuestionOptionDto input) {
        return optionService.CreateOrEdit(input);
    }

    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input) {
        optionService.Delete(input);
    }

    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input) {
        optionService.BatchDelete(input);
    }

    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
        optionService.Export(query, response);
    }
}

