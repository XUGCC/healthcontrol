package com.example.web.controller;

import com.example.web.dto.TAudioAiReportDto;
import com.example.web.dto.query.TAudioAiReportPagedInput;
import com.example.web.service.TAudioAiReportService;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI解读报告控制器（通用 CRUD，管理端/调试可用）
 */
@RestController
@RequestMapping("/TAudioAiReport")
public class TAudioAiReportController {

    @Autowired
    private TAudioAiReportService TAudioAiReportService;

    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TAudioAiReportDto> List(@RequestBody TAudioAiReportPagedInput input) {
        return TAudioAiReportService.List(input);
    }

    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TAudioAiReportDto Get(@RequestBody TAudioAiReportPagedInput input) {
        return TAudioAiReportService.Get(input);
    }

    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TAudioAiReportDto CreateOrEdit(@RequestBody TAudioAiReportDto input) {
        return TAudioAiReportService.CreateOrEdit(input);
    }

    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input) {
        TAudioAiReportService.Delete(input);
    }

    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input) {
        TAudioAiReportService.BatchDelete(input);
    }
}

