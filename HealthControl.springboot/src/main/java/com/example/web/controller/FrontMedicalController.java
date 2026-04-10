package com.example.web.controller;

import com.example.web.dto.front.FrontMedicalDtos.*;
import com.example.web.service.front.FrontMedicalService;
import com.example.web.tools.dto.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 前台就医辅助模块接口（小程序端）
 */
@RestController
@RequestMapping("/Front/Medical")
public class FrontMedicalController {

    @Autowired
    private FrontMedicalService frontMedicalService;

    /**
     * 就医辅助首页聚合数据
     */
    @RequestMapping(value = "/Home", method = RequestMethod.POST)
    public FrontMedicalHomeOutput Home() {
        return frontMedicalService.home();
    }

    /**
     * 医院 / 医生列表
     */
    @RequestMapping(value = "/DoctorList", method = RequestMethod.POST)
    public PagedResult<FrontMedicalDoctorListItem> DoctorList(@RequestBody FrontMedicalDoctorListInput input) {
        return frontMedicalService.doctorList(input);
    }

    /**
     * 医生详情
     */
    @RequestMapping(value = "/DoctorDetail", method = RequestMethod.POST)
    public FrontMedicalDoctorDetail DoctorDetail(@RequestBody FrontMedicalDoctorDetailInput input) {
        return frontMedicalService.doctorDetail(input);
    }

    /**
     * 就医推荐列表
     */
    @RequestMapping(value = "/RecommendList", method = RequestMethod.POST)
    public PagedResult<FrontMedicalRecommendListItem> RecommendList(@RequestBody FrontMedicalRecommendListInput input) {
        return frontMedicalService.recommendList(input);
    }

    /**
     * 就医推荐详情
     */
    @RequestMapping(value = "/RecommendDetail", method = RequestMethod.POST)
    public FrontMedicalRecommendDetailOutput RecommendDetail(@RequestBody FrontMedicalRecommendDetailInput input) {
        return frontMedicalService.recommendDetail(input);
    }

    /**
     * 标记就医推荐为已查看
     */
    @RequestMapping(value = "/RecommendMarkViewed", method = RequestMethod.POST)
    public void RecommendMarkViewed(@RequestBody FrontMedicalRecommendMarkViewedInput input) {
        frontMedicalService.recommendMarkViewed(input);
    }

    /**
     * 就医推荐反馈（预留接口，当前实现仅做结构兼容）
     */
    @RequestMapping(value = "/RecommendFeedback", method = RequestMethod.POST)
    public void RecommendFeedback(@RequestBody FrontMedicalRecommendFeedbackInput input) {
        frontMedicalService.recommendFeedback(input);
    }

    /**
     * 创建就诊准备报告
     */
    @RequestMapping(value = "/PrepareReportCreate", method = RequestMethod.POST)
    public void PrepareReportCreate(@RequestBody FrontMedicalPrepareReportCreateInput input) {
        frontMedicalService.prepareReportCreate(input);
    }

    /**
     * 报告列表
     */
    @RequestMapping(value = "/PrepareReportList", method = RequestMethod.POST)
    public PagedResult<FrontMedicalPrepareReportListItem> PrepareReportList(@RequestBody FrontMedicalPrepareReportListInput input) {
        return frontMedicalService.prepareReportList(input);
    }

    /**
     * 报告详情
     */
    @RequestMapping(value = "/PrepareReportDetail", method = RequestMethod.POST)
    public FrontMedicalPrepareReportDetailOutput PrepareReportDetail(@RequestBody FrontMedicalPrepareReportDetailInput input) {
        return frontMedicalService.prepareReportDetail(input);
    }

    /**
     * 创建导出记录
     */
    @RequestMapping(value = "/ExportRecordCreate", method = RequestMethod.POST)
    public void ExportRecordCreate(@RequestBody FrontMedicalExportRecordCreateInput input) {
        frontMedicalService.exportRecordCreate(input);
    }

    /**
     * 导出记录列表
     */
    @RequestMapping(value = "/ExportRecordList", method = RequestMethod.POST)
    public PagedResult<FrontMedicalExportRecordListItem> ExportRecordList(@RequestBody FrontMedicalExportRecordListInput input) {
        return frontMedicalService.exportRecordList(input);
    }

    /**
     * HTML 报告预览（用于 web-view）
     */
    @RequestMapping(value = "/PrepareReportHtml", method = RequestMethod.GET, produces = "text/html;charset=UTF-8")
    public @ResponseBody String PrepareReportHtml(@RequestParam("reportId") Integer reportId) {
        return frontMedicalService.prepareReportHtml(reportId);
    }

    /**
     * PDF 报告导出（用于下载或分享）
     * 当前实现为占位：返回简单的 PDF 文件内容
     */
    @RequestMapping(value = "/PrepareReportPdf", method = RequestMethod.GET)
    public void PrepareReportPdf(@RequestParam("reportId") Integer reportId,
                                 HttpServletResponse response) throws IOException {
        byte[] pdf = frontMedicalService.prepareReportPdf(reportId);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=medical-report-" + reportId + ".pdf");
        response.getOutputStream().write(pdf);
        response.flushBuffer();
    }
}

