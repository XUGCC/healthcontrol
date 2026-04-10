package com.example.web.controller;

import com.example.web.dto.front.FrontQuestionnaireDetailDto;
import com.example.web.dto.front.FrontQuestionnaireDetailInput;
import com.example.web.dto.front.FrontPagedInput;
import com.example.web.dto.front.FrontLatestQuestionnaireResultDto;
import com.example.web.dto.front.FrontQuestionnaireSubmitInput;
import com.example.web.dto.front.FrontQuestionnaireSubmitResultDto;
import com.example.web.dto.front.FrontQuestionnaireHistoryItemDto;
import com.example.web.entity.TRiskAssessmentQuestionnaire;
import com.example.web.service.front.FrontQuestionnaireService;
import com.example.web.tools.dto.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台问卷业务接口（小程序端）
 */
@RestController
@RequestMapping("/Front/Questionnaire")
public class FrontQuestionnaireController {

    @Autowired
    private FrontQuestionnaireService frontQuestionnaireService;

    @RequestMapping(value = "/List", method = RequestMethod.POST)
    public PagedResult<TRiskAssessmentQuestionnaire> List() {
        return frontQuestionnaireService.list();
    }

    @RequestMapping(value = "/Detail", method = RequestMethod.POST)
    public FrontQuestionnaireDetailDto Detail(@RequestBody FrontQuestionnaireDetailInput input) {
        return frontQuestionnaireService.detail(input);
    }

    @RequestMapping(value = "/Submit", method = RequestMethod.POST)
    public FrontQuestionnaireSubmitResultDto Submit(@RequestBody FrontQuestionnaireSubmitInput input) {
        return frontQuestionnaireService.submit(input);
    }

    @RequestMapping(value = "/MyHistory", method = RequestMethod.POST)
    public PagedResult<FrontQuestionnaireHistoryItemDto> MyHistory(@RequestBody(required = false) FrontPagedInput input) {
        return frontQuestionnaireService.myHistory(input);
    }

    @RequestMapping(value = "/LatestResult", method = RequestMethod.POST)
    public FrontLatestQuestionnaireResultDto LatestResult() {
        return frontQuestionnaireService.latestResult();
    }
}

