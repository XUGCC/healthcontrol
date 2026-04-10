package com.example.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.TAudioAiReportDto;
import com.example.web.dto.query.TAudioAiReportPagedInput;
import com.example.web.entity.TAudioAiReport;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;

/**
 * 音频检测AI解读报告表功能的Service接口定义
 */
public interface TAudioAiReportService extends IService<TAudioAiReport> {

    PagedResult<TAudioAiReportDto> List(TAudioAiReportPagedInput input);

    TAudioAiReportDto Get(TAudioAiReportPagedInput input);

    TAudioAiReportDto CreateOrEdit(TAudioAiReportDto input);

    void Delete(IdInput input);

    void BatchDelete(IdsInput input);
}

