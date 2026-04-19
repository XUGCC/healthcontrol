package com.example.web.controller;

import com.example.web.tools.dto.IdInput;
import com.example.web.dto.TLaryngoscopePhotoDto;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.LocalPredictInput;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.LocalPredictionOutput;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.QwenAnalysisOutput;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.QwenAnalyzeInput;
import com.example.web.dto.query.TLaryngoscopePhotoPagedInput;
import com.example.web.service.TLaryngoscopePhotoService;
import com.example.web.service.front.FrontLaryngoscopeAnalysisService;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台喉镜照片相关接口（小程序端）
 */
@RestController
@RequestMapping("/Front/Laryngoscope")
public class FrontLaryngoscopeController {

    @Autowired
    private TLaryngoscopePhotoService laryngoscopePhotoService;

    @Autowired
    private FrontLaryngoscopeAnalysisService laryngoscopeAnalysisService;

    /**
     * 当前登录用户的喉镜照片列表（分页）
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    public PagedResult<TLaryngoscopePhotoDto> List(@RequestBody TLaryngoscopePhotoPagedInput input) {
        if (input == null) {
            input = new TLaryngoscopePhotoPagedInput();
        }
        Integer userId = BaseContext.getCurrentUserId();
        input.setUserId(userId);
        return laryngoscopePhotoService.List(input);
    }

    /**
     * 创建或编辑喉镜照片记录（UserId 由后端自动填充为当前用户）
     */
    @RequestMapping(value = "/SavePhoto", method = RequestMethod.POST)
    public TLaryngoscopePhotoDto SavePhoto(@RequestBody TLaryngoscopePhotoDto input) throws Exception {
        Integer userId = BaseContext.getCurrentUserId();
        input.setUserId(userId);
        return laryngoscopePhotoService.CreateOrEdit(input);
    }

    /**
     * 获取当前用户单条喉镜照片记录详情（用于前台编辑回显）
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    public TLaryngoscopePhotoDto Get(@RequestBody TLaryngoscopePhotoPagedInput input) {
        if (input == null) {
            input = new TLaryngoscopePhotoPagedInput();
        }
        Integer userId = BaseContext.getCurrentUserId();
        input.setUserId(userId);
        return laryngoscopePhotoService.Get(input);
    }

    /**
     * 删除喉镜照片记录（软删除）
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input) {
        laryngoscopePhotoService.Delete(input);
    }

    @RequestMapping(value = "/LocalPredict", method = RequestMethod.POST)
    public LocalPredictionOutput LocalPredict(@RequestBody LocalPredictInput input) {
        return laryngoscopeAnalysisService.localPredict(input);
    }

    @RequestMapping(value = "/GetLocalPrediction", method = RequestMethod.POST)
    public LocalPredictionOutput GetLocalPrediction(@RequestBody IdInput input) {
        return laryngoscopeAnalysisService.getLocalPrediction(input);
    }

    @RequestMapping(value = "/QwenAnalyze", method = RequestMethod.POST)
    public QwenAnalysisOutput QwenAnalyze(@RequestBody QwenAnalyzeInput input) {
        return laryngoscopeAnalysisService.qwenAnalyze(input);
    }

    @RequestMapping(value = "/GetQwenAnalysis", method = RequestMethod.POST)
    public QwenAnalysisOutput GetQwenAnalysis(@RequestBody IdInput input) {
        return laryngoscopeAnalysisService.getQwenAnalysis(input);
    }
}

