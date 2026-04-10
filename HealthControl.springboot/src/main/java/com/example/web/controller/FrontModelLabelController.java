package com.example.web.controller;

import com.example.web.dto.TModelOptimizeLabelDto;
import com.example.web.dto.TAudioScreenRecordDto;
import com.example.web.dto.query.TModelOptimizeLabelPagedInput;
import com.example.web.service.TModelOptimizeLabelService;
import com.example.web.tools.dto.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 前台模型优化标注接口（小程序端）
 */
@RestController
@RequestMapping("/Front/ModelLabel")
public class FrontModelLabelController {

    @Autowired
    private TModelOptimizeLabelService modelLabelService;

    /**
     * 创建或更新标注
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TModelOptimizeLabelDto CreateOrEdit(@RequestBody TModelOptimizeLabelDto input) {
        return modelLabelService.CreateOrEditForFront(input);
    }

    /**
     * 获取用户标注列表
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    public PagedResult<TModelOptimizeLabelDto> List(@RequestBody TModelOptimizeLabelPagedInput input) {
        return modelLabelService.ListForFront(input);
    }

    /**
     * 获取单条标注详情
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    public TModelOptimizeLabelDto Get(@RequestBody TModelOptimizeLabelPagedInput input) {
        return modelLabelService.GetForFront(input);
    }

    /**
     * 更新授权状态
     */
    @RequestMapping(value = "/UpdateAuth", method = RequestMethod.POST)
    public void UpdateAuth(@RequestBody TModelOptimizeLabelDto input) {
        modelLabelService.UpdateAuthForFront(input);
    }

    /**
     * 删除标注
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody TModelOptimizeLabelPagedInput input) {
        modelLabelService.DeleteForFront(input);
    }

    /**
     * 获取用户检测记录列表（用于选择关联）
     */
    @RequestMapping(value = "/DetectList", method = RequestMethod.POST)
    public PagedResult<TAudioScreenRecordDto> DetectList(@RequestBody TModelOptimizeLabelPagedInput input) {
        return modelLabelService.GetUserDetectListForFront(input);
    }
}
