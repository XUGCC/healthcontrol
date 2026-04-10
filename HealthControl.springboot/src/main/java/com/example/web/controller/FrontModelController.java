package com.example.web.controller;

import com.example.web.dto.front.FrontModelDtos.FrontModelCallLogSummaryInput;
import com.example.web.dto.front.FrontModelDtos.FrontModelCallLogSummaryOutput;
import com.example.web.service.front.FrontModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台模型服务接口（小程序端）
 */
@RestController
@RequestMapping("/Front/Model")
public class FrontModelController {

    @Autowired
    private FrontModelService frontModelService;

    /**
     * 获取单次检测的模型调用日志汇总信息
     */
    @RequestMapping(value = "/CallLogSummary", method = RequestMethod.POST)
    public FrontModelCallLogSummaryOutput CallLogSummary(@RequestBody FrontModelCallLogSummaryInput input) {
        return frontModelService.callLogSummary(input);
    }
}

