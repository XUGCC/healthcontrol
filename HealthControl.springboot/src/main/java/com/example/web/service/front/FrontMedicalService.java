package com.example.web.service.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.web.dto.front.FrontMedicalDtos.*;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.tools.BaseContext;
import com.example.web.tools.Extension;
import com.example.web.tools.dto.PagedResult;
import com.example.web.tools.exception.CustomException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 前台就医辅助模块聚合服务（小程序端）
 */
@Service
public class FrontMedicalService {

    @Autowired
    private TPersonalLaryngealHealthRecordMapper healthRecordMapper;

    @Autowired
    private TAudioScreenRecordMapper audioScreenRecordMapper;

    @Autowired
    private TSymptomLogMapper symptomLogMapper;

    @Autowired
    private TUserMedicalRecommendMapper userMedicalRecommendMapper;

    @Autowired
    private TOtolaryngologyHospitalDoctorMapper doctorMapper;

    @Autowired
    private TUserMedicalPrepareReportMapper prepareReportMapper;

    @Autowired
    private TScreenReportExportRecordMapper exportRecordMapper;

    @Autowired
    private TUserDietRecordMapper userDietRecordMapper;

    @Autowired
    private TLaryngealFoodMapper foodMapper;

    @Autowired
    private TLaryngoscopePhotoMapper laryngoscopePhotoMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 报告内容快照（存入 ReportContentJson）
     * 说明：ReportContentJson 字段名历史原因保留，这里实际存放 JSON。
     */
    public static class ReportSnapshot {
        public Integer ReportId;
        public Integer UserId;
        public Integer TemplateType; // 0/1
        public Integer HealthRecordId;
        public Integer RiskLevel; // 0/1/2 or null
        public String RiskLevelText;
        public List<Integer> DetectIds;
        public List<Integer> SymptomIds;
        public List<Integer> DietRecordIds;
        public String ContentScope;
        public String CreatedTime;
    }

    /**
     * 就医辅助首页聚合数据
     */
    public FrontMedicalHomeOutput home() {
        Integer userId = getCurrentUserId();
        FrontMedicalHomeOutput out = new FrontMedicalHomeOutput();

        // 未登录用户只返回文案提示
        if (userId == null || userId <= 0) {
            out.setRiskLevel(null);
            out.setRiskSummaryText("未登录用户仅可浏览医院/医生库。登录后，系统将结合您的检测记录与健康档案给出个性化就医建议与就诊准备报告。");
            return out;
        }

        // 最近健康档案
        TPersonalLaryngealHealthRecord record = healthRecordMapper.selectOne(
                Wrappers.<TPersonalLaryngealHealthRecord>lambdaQuery()
                        .eq(TPersonalLaryngealHealthRecord::getUserId, userId)
                        .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete)
                                .or().ne(TPersonalLaryngealHealthRecord::getIsDelete, true))
                        .orderByDesc(TPersonalLaryngealHealthRecord::getCreationTime)
                        .last("LIMIT 1")
        );
        Integer riskLevel = record == null ? null : record.getRiskAssessmentLevel();
        out.setRiskLevel(riskLevel);

        String riskText;
        if (riskLevel == null) riskText = "未知";
        else if (riskLevel == 0) riskText = "低风险";
        else if (riskLevel == 1) riskText = "中风险";
        else if (riskLevel == 2) riskText = "高风险";
        else riskText = "未知";

        // 最近检测
        TAudioScreenRecord audio = audioScreenRecordMapper.selectOne(
                Wrappers.<TAudioScreenRecord>lambdaQuery()
                        .eq(TAudioScreenRecord::getUserId, userId)
                        .and(w -> w.isNull(TAudioScreenRecord::getIsDelete)
                                .or().ne(TAudioScreenRecord::getIsDelete, true))
                        .orderByDesc(TAudioScreenRecord::getCreationTime)
                        .last("LIMIT 1")
        );
        if (audio != null) {
            FrontMedicalLatestDetectBrief latestDetect = new FrontMedicalLatestDetectBrief();
            StringBuilder sb = new StringBuilder();
            if (audio.getCreationTime() != null) {
                sb.append(audio.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(" ");
            }
            Boolean r = audio.getPrimaryScreenResult();
            String resultText = "未知";
            if (r != null) {
                resultText = Boolean.TRUE.equals(r) ? "恶性倾向" : "良性倾向";
            }
            sb.append("检测结果：").append(resultText);
            if (audio.getPrimaryScreenConfidence() != null) {
                sb.append("，置信度 ")
                        .append(String.format("%.1f%%", audio.getPrimaryScreenConfidence() * 100));
            }
            latestDetect.setSummaryText(sb.toString());
            out.setLatestDetect(latestDetect);
        }

        // 最近症状
        TSymptomLog symptom = symptomLogMapper.selectOne(
                Wrappers.<TSymptomLog>lambdaQuery()
                        .eq(TSymptomLog::getUserId, userId)
                        .and(w -> w.isNull(TSymptomLog::getIsDelete)
                                .or().ne(TSymptomLog::getIsDelete, true))
                        .orderByDesc(TSymptomLog::getCreationTime)
                        .last("LIMIT 1")
        );
        if (symptom != null) {
            FrontMedicalLatestSymptomBrief latestSymptom = new FrontMedicalLatestSymptomBrief();
            StringBuilder sb = new StringBuilder();
            if (Extension.isNotNullOrEmpty(symptom.getSymptomType())) {
                sb.append(symptom.getSymptomType());
            }
            if (symptom.getSymptomLevel() != null) {
                String levelText;
                if (symptom.getSymptomLevel() == 1) levelText = "轻度";
                else if (symptom.getSymptomLevel() == 2) levelText = "中度";
                else if (symptom.getSymptomLevel() == 3) levelText = "重度";
                else levelText = "不明";
                if (sb.length() > 0) sb.append("（").append(levelText).append("）");
                else sb.append(levelText);
            }
            latestSymptom.setSymptomSummary(sb.toString());
            out.setLatestSymptom(latestSymptom);
        }

        // 最新就医推荐：
        // - 仅当当前风险等级为中/高风险（>=1）时，首页才展示“本次系统就医建议”
        // - 历史就医建议仍可在“查看全部”列表页中查看
        // - 每次最新健康档案的风险等级重新变为中/高风险时，为该次评估生成一条新的系统就医建议
        TUserMedicalRecommend latestRecommendEntity = null;
        if (riskLevel != null && riskLevel >= 1) {
            latestRecommendEntity = userMedicalRecommendMapper.selectOne(
                    Wrappers.<TUserMedicalRecommend>lambdaQuery()
                            .eq(TUserMedicalRecommend::getUserId, userId)
                            .and(w -> w.isNull(TUserMedicalRecommend::getIsDelete)
                                    .or().ne(TUserMedicalRecommend::getIsDelete, true))
                            .orderByDesc(TUserMedicalRecommend::getRecommendTime)
                            .orderByDesc(TUserMedicalRecommend::getCreationTime)
                            .last("LIMIT 1")
            );

            // 判断是否需要为当前这次风险评估生成新的系统就医建议：
            // 1. 不存在任何就医建议记录
            // 2. 或者最新就医建议的风险等级与当前风险不一致（中<->高切换）
            // 3. 或者存在最新健康档案记录，且最新就医建议的推荐时间早于该健康档案的创建时间
            boolean needCreateRecommend = false;
            if (latestRecommendEntity == null) {
                needCreateRecommend = true;
            } else {
                // 条件 2：风险等级不一致时，为新的风险等级生成一条新的建议
                Boolean lastRiskBool = latestRecommendEntity.getRiskLevel();
                boolean currentRiskBool = (riskLevel == 2); // true=高风险，false=中风险
                if (lastRiskBool == null || !lastRiskBool.equals(currentRiskBool)) {
                    needCreateRecommend = true;
                }

                // 条件 3：若有健康档案且推荐时间早于该档案创建时间，说明本次评估尚未生成对应的建议
                if (!needCreateRecommend && record != null
                        && latestRecommendEntity.getRecommendTime() != null
                        && record.getCreationTime() != null
                        && latestRecommendEntity.getRecommendTime().isBefore(record.getCreationTime())) {
                    needCreateRecommend = true;
                }
            }

            if (needCreateRecommend) {
                TUserMedicalRecommend rec = new TUserMedicalRecommend();
                rec.setUserId(userId);
                rec.setDetectId(audio != null ? audio.getId() : null);
                // RiskLevel 字段为 Boolean：false→中风险，true→高风险
                rec.setRiskLevel(riskLevel == 2);
                rec.setRecommendHospitalId(null);
                LocalDateTime now = LocalDateTime.now();
                rec.setRecommendTime(now);
                rec.setUpdateTime(now);
                rec.setViewStatus(false);
                rec.setIsDelete(false);
                userMedicalRecommendMapper.insert(rec);
                latestRecommendEntity = rec;
            }
        }

        if (latestRecommendEntity != null) {
            FrontMedicalRecommendBrief brief = new FrontMedicalRecommendBrief();
            brief.setRecommendId(latestRecommendEntity.getId());

            String title;
            if (riskLevel != null) {
                if (riskLevel == 2) {
                    title = "系统就医建议（高风险）";
                } else if (riskLevel == 1) {
                    title = "系统就医建议（中风险）";
                } else {
                    title = "系统就医建议";
                }
            } else if (!"未知".equals(riskText)) {
                title = "就医建议（当前风险：" + riskText + "）";
            } else {
                title = "就医建议";
            }
            brief.setTitle(title);

            brief.setRecommendTime(latestRecommendEntity.getRecommendTime());
            brief.setViewStatus(latestRecommendEntity.getViewStatus());

            String defaultReason;
            if (riskLevel != null && riskLevel == 2) {
                defaultReason = "当前喉部总体评估为高风险，建议尽快前往具备咽喉/嗓音专病门诊的耳鼻喉相关专科就诊，由专科医生进行喉镜检查和进一步评估。";
            } else if (riskLevel != null && riskLevel == 1) {
                defaultReason = "当前喉部总体评估为中风险，建议在近期安排一次线下耳鼻喉相关专科门诊，向医生说明近期声音变化与不适情况，必要时完善进一步检查。";
            } else {
                defaultReason = "综合您的风险等级和最近一次检测结果，系统建议您关注线下耳鼻喉相关科室就诊评估。";
            }
            brief.setRecommendReason(defaultReason);

            out.setLatestRecommend(brief);
        }

        // 最新就诊准备报告
        TUserMedicalPrepareReport latestReportEntity = prepareReportMapper.selectOne(
                Wrappers.<TUserMedicalPrepareReport>lambdaQuery()
                        .eq(TUserMedicalPrepareReport::getUserId, userId)
                        .and(w -> w.isNull(TUserMedicalPrepareReport::getIsDelete)
                                .or().ne(TUserMedicalPrepareReport::getIsDelete, true))
                        .orderByDesc(TUserMedicalPrepareReport::getCreationTime)
                        .last("LIMIT 1")
        );
        if (latestReportEntity != null) {
            FrontMedicalReportBrief reportBrief = new FrontMedicalReportBrief();
            reportBrief.setReportId(latestReportEntity.getId());
            String title = latestReportEntity.getReportTitle();
            if (title == null || title.isEmpty()) {
                title = latestReportEntity.getTemplateType() != null && latestReportEntity.getTemplateType()
                        ? "就诊准备报告（详版）"
                        : "就诊准备报告（简版）";
            }
            reportBrief.setReportTitle(title);
            reportBrief.setCreationTime(latestReportEntity.getCreationTime());
            Integer status = mapGenerateStatus(latestReportEntity.getGenerateStatus(), latestReportEntity.getStandardDocUrl());
            reportBrief.setGenerateStatus(status);
            reportBrief.setGenerateStatusText(mapGenerateStatusText(status));
            out.setLatestReport(reportBrief);
        }

        // 当前版本暂不实现就诊计划表，这里预留字段为空即可
        out.setLatestVisitPlan(null);

        // 综合说明
        StringBuilder summary = new StringBuilder();
        summary.append("当前喉部总体风险评估：").append(riskText).append("。");
        if (audio != null) {
            Boolean r = audio.getPrimaryScreenResult();
            if (r != null) {
                summary.append("最近一次自查结果提示：")
                        .append(Boolean.TRUE.equals(r) ? "存在恶性倾向信号" : "更偏向良性。");
            }
        }
        if (symptom != null && Extension.isNotNullOrEmpty(symptom.getSymptomType())) {
            summary.append("近期主要不适为：").append(symptom.getSymptomType()).append("。");
        }
        summary.append("如持续出现明显不适或症状加重，建议及时线下就诊，由耳鼻喉相关专科医生做进一步评估。");
        out.setRiskSummaryText(summary.toString());

        return out;
    }

    /**
     * 医院/医生列表
     */
    public PagedResult<FrontMedicalDoctorListItem> doctorList(FrontMedicalDoctorListInput input) {
        if (input == null) input = new FrontMedicalDoctorListInput();
        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        LambdaQueryWrapper<TOtolaryngologyHospitalDoctor> qw = Wrappers.<TOtolaryngologyHospitalDoctor>lambdaQuery()
                .eq(TOtolaryngologyHospitalDoctor::getShowStatus, true)
                .and(w -> w.isNull(TOtolaryngologyHospitalDoctor::getIsDelete)
                        .or().ne(TOtolaryngologyHospitalDoctor::getIsDelete, true));

        if (Extension.isNotNullOrEmpty(input.getRegion())) {
            qw.like(TOtolaryngologyHospitalDoctor::getRegion, input.getRegion().trim());
        }
        if (input.getDepartmentType() != null) {
            // 实体字段是 Boolean：0/false=耳鼻喉科，1/true=咽喉科/头颈外科；这里按 0/1/2 做最小兼容映射：
            // 0 -> false, 1/2 -> true（前端展示文案再区分）
            boolean depBool = input.getDepartmentType() != 0;
            qw.eq(TOtolaryngologyHospitalDoctor::getDepartmentType, depBool);
        }
        if (Extension.isNotNullOrEmpty(input.getKeyword())) {
            String kw = input.getKeyword().trim();
            if (!kw.isEmpty()) {
                qw.and(w -> w.like(TOtolaryngologyHospitalDoctor::getHospitalName, kw)
                        .or().like(TOtolaryngologyHospitalDoctor::getDoctorName, kw));
            }
        }

        qw.orderByAsc(TOtolaryngologyHospitalDoctor::getHospitalName)
                .orderByAsc(TOtolaryngologyHospitalDoctor::getDoctorName);

        Page<TOtolaryngologyHospitalDoctor> mpPage = new Page<>(page, limit);
        Page<TOtolaryngologyHospitalDoctor> result = doctorMapper.selectPage(mpPage, qw);
        long total = doctorMapper.selectCount(qw);

        List<TOtolaryngologyHospitalDoctor> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return PagedResult.GetEmptyInstance();
        }

        List<FrontMedicalDoctorListItem> items = new ArrayList<>();
        for (TOtolaryngologyHospitalDoctor d : records) {
            FrontMedicalDoctorListItem dto = new FrontMedicalDoctorListItem();
            dto.setId(d.getId());
            dto.setHospitalName(d.getHospitalName());
            dto.setDoctorName(d.getDoctorName());
            // DepartmentType 为 Boolean 时，简单映射：false->0，true->1
            if (d.getDepartmentType() != null) {
                dto.setDepartmentType(Boolean.TRUE.equals(d.getDepartmentType()) ? 1 : 0);
            }
            dto.setRegion(d.getRegion());
            dto.setAddress(d.getAddress());
            dto.setContactPhone(d.getContactPhone());
            dto.setAppointmentUrl(d.getAppointmentUrl());
            items.add(dto);
        }

        return PagedResult.GetInstance(items, total);
    }

    /**
     * 医生详情
     */
    public FrontMedicalDoctorDetail doctorDetail(FrontMedicalDoctorDetailInput input) {
        if (input == null || input.getDoctorId() == null || input.getDoctorId() <= 0) {
            throw new CustomException("DoctorId不能为空");
        }
        TOtolaryngologyHospitalDoctor d = doctorMapper.selectOne(
                Wrappers.<TOtolaryngologyHospitalDoctor>lambdaQuery()
                        .eq(TOtolaryngologyHospitalDoctor::getId, input.getDoctorId())
                        .eq(TOtolaryngologyHospitalDoctor::getShowStatus, true)
                        .and(w -> w.isNull(TOtolaryngologyHospitalDoctor::getIsDelete)
                                .or().ne(TOtolaryngologyHospitalDoctor::getIsDelete, true))
                        .last("LIMIT 1")
        );
        if (d == null) {
            throw new CustomException("医生不存在或已下架");
        }
        FrontMedicalDoctorDetail dto = new FrontMedicalDoctorDetail();
        dto.setId(d.getId());
        dto.setHospitalName(d.getHospitalName());
        dto.setDoctorName(d.getDoctorName());
        if (d.getDepartmentType() != null) {
            dto.setDepartmentType(Boolean.TRUE.equals(d.getDepartmentType()) ? 1 : 0);
        }
        dto.setRegion(d.getRegion());
        dto.setAddress(d.getAddress());
        dto.setContactPhone(d.getContactPhone());
        dto.setAppointmentUrl(d.getAppointmentUrl());
        // 新增：把医院等级、标签、擅长方向、门诊时间、经纬度、职称、头像等字段完整映射到前端
        dto.setHospitalLevel(d.getHospitalLevel());
        dto.setTags(d.getTags());
        dto.setSpecialtyDesc(d.getSpecialtyDesc());
        dto.setOutpatientTime(d.getOutpatientTime());
        dto.setLongitude(d.getLongitude());
        dto.setLatitude(d.getLatitude());
        dto.setDoctorTitle(d.getDoctorTitle());
        dto.setPicUrl(d.getPicUrl());
        return dto;
    }

    /**
     * 就医推荐列表
     */
    public PagedResult<FrontMedicalRecommendListItem> recommendList(FrontMedicalRecommendListInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后查看就医建议");
        }
        // 若当前为低风险或未评估，则不返回历史就医建议，避免产生不必要的紧张感
        TPersonalLaryngealHealthRecord record = healthRecordMapper.selectOne(
                Wrappers.<TPersonalLaryngealHealthRecord>lambdaQuery()
                        .eq(TPersonalLaryngealHealthRecord::getUserId, userId)
                        .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete)
                                .or().ne(TPersonalLaryngealHealthRecord::getIsDelete, true))
                        .orderByDesc(TPersonalLaryngealHealthRecord::getCreationTime)
                        .last("LIMIT 1")
        );
        Integer riskLevel = record == null ? null : record.getRiskAssessmentLevel();
        if (riskLevel == null || riskLevel <= 0) {
            return PagedResult.GetEmptyInstance();
        }
        if (input == null) input = new FrontMedicalRecommendListInput();
        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        LambdaQueryWrapper<TUserMedicalRecommend> qw = Wrappers.<TUserMedicalRecommend>lambdaQuery()
                .eq(TUserMedicalRecommend::getUserId, userId)
                .and(w -> w.isNull(TUserMedicalRecommend::getIsDelete)
                        .or().ne(TUserMedicalRecommend::getIsDelete, true));

        if (input.getViewStatus() != null && (input.getViewStatus() == 0 || input.getViewStatus() == 1)) {
            boolean viewed = input.getViewStatus() == 1;
            qw.eq(TUserMedicalRecommend::getViewStatus, viewed);
        }

        qw.orderByDesc(TUserMedicalRecommend::getRecommendTime)
                .orderByDesc(TUserMedicalRecommend::getCreationTime);

        Page<TUserMedicalRecommend> mpPage = new Page<>(page, limit);
        Page<TUserMedicalRecommend> result = userMedicalRecommendMapper.selectPage(mpPage, qw);
        long total = userMedicalRecommendMapper.selectCount(qw);

        List<TUserMedicalRecommend> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return PagedResult.GetEmptyInstance();
        }

        List<FrontMedicalRecommendListItem> items = new ArrayList<>();
        for (TUserMedicalRecommend r : records) {
            FrontMedicalRecommendListItem dto = new FrontMedicalRecommendListItem();
            dto.setId(r.getId());
            // 当前 RiskLevel 字段为 Boolean，这里简单映射为：false->1(中风险), true->2(高风险)，null->null
            if (r.getRiskLevel() != null) {
                dto.setRiskLevel(Boolean.TRUE.equals(r.getRiskLevel()) ? 2 : 1);
            }
            String title = "就医建议";
            Integer rl = dto.getRiskLevel();
            if (rl != null) {
                if (rl == 2) title = "就医建议（高风险）";
                else if (rl == 1) title = "就医建议（中风险）";
            }
            dto.setTitle(title);
            dto.setRecommendTime(r.getRecommendTime());
            dto.setViewStatus(r.getViewStatus());

            String defaultReason = "综合近期检测与风险评估，建议您线下就诊由耳鼻喉相关专科医生进行进一步评估。";
            dto.setRecommendReason(defaultReason);

            items.add(dto);
        }

        return PagedResult.GetInstance(items, total);
    }

    /**
     * 就医推荐详情
     */
    public FrontMedicalRecommendDetailOutput recommendDetail(FrontMedicalRecommendDetailInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后查看就医建议");
        }
        if (input == null || input.getRecommendId() == null || input.getRecommendId() <= 0) {
            throw new CustomException("RecommendId不能为空");
        }
        TUserMedicalRecommend r = userMedicalRecommendMapper.selectOne(
                Wrappers.<TUserMedicalRecommend>lambdaQuery()
                        .eq(TUserMedicalRecommend::getId, input.getRecommendId())
                        .eq(TUserMedicalRecommend::getUserId, userId)
                        .and(w -> w.isNull(TUserMedicalRecommend::getIsDelete)
                                .or().ne(TUserMedicalRecommend::getIsDelete, true))
                        .last("LIMIT 1")
        );
        if (r == null) {
            throw new CustomException("就医推荐记录不存在或已删除");
        }

        FrontMedicalRecommendDetailOutput dto = new FrontMedicalRecommendDetailOutput();
        dto.setId(r.getId());
        dto.setDetectId(r.getDetectId());
        if (r.getRiskLevel() != null) {
            dto.setRiskLevel(Boolean.TRUE.equals(r.getRiskLevel()) ? 2 : 1);
        }
        dto.setRecommendTime(r.getRecommendTime());
        dto.setViewStatus(r.getViewStatus());

        String title = "就医建议";
        if (dto.getRiskLevel() != null) {
            if (dto.getRiskLevel() == 2) title = "就医建议（高风险）";
            else if (dto.getRiskLevel() == 1) title = "就医建议（中风险）";
        }
        dto.setTitle(title);

        String reason = "结合您的风险等级、最近检测结果以及症状情况，系统建议您尽快到耳鼻喉或咽喉相关科室就诊，由专科医生进行进一步评估。";
        dto.setRecommendReason(reason);

        // 检测与症状摘要（尽量从最新记录构造）
        TAudioScreenRecord audio = null;
        if (r.getDetectId() != null) {
            audio = audioScreenRecordMapper.selectById(r.getDetectId());
        }
        if (audio == null) {
            audio = audioScreenRecordMapper.selectOne(
                    Wrappers.<TAudioScreenRecord>lambdaQuery()
                            .eq(TAudioScreenRecord::getUserId, userId)
                            .and(w -> w.isNull(TAudioScreenRecord::getIsDelete)
                                    .or().ne(TAudioScreenRecord::getIsDelete, true))
                            .orderByDesc(TAudioScreenRecord::getCreationTime)
                            .last("LIMIT 1")
            );
        }
        if (audio != null) {
            // 确保 DetectId 与实际使用的检测记录保持一致，方便前端“去标注”
            dto.setDetectId(audio.getId());
            StringBuilder sb = new StringBuilder();
            if (audio.getCreationTime() != null) {
                sb.append(audio.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(" ");
            }
            Boolean rr = audio.getPrimaryScreenResult();
            if (rr != null) {
                sb.append("检测结果：").append(Boolean.TRUE.equals(rr) ? "恶性倾向" : "良性倾向");
            }
            if (audio.getPrimaryScreenConfidence() != null) {
                sb.append("，置信度 ")
                        .append(String.format("%.1f%%", audio.getPrimaryScreenConfidence() * 100));
            }
            dto.setDetectSummary(sb.toString());
        }

        TSymptomLog symptom = symptomLogMapper.selectOne(
                Wrappers.<TSymptomLog>lambdaQuery()
                        .eq(TSymptomLog::getUserId, userId)
                        .and(w -> w.isNull(TSymptomLog::getIsDelete)
                                .or().ne(TSymptomLog::getIsDelete, true))
                        .orderByDesc(TSymptomLog::getCreationTime)
                        .last("LIMIT 1")
        );
        if (symptom != null && Extension.isNotNullOrEmpty(symptom.getSymptomType())) {
            dto.setSymptomSummary(symptom.getSymptomType());
        }

        // 推荐医院/医生信息（如有）
        if (r.getRecommendHospitalId() != null) {
            TOtolaryngologyHospitalDoctor d = doctorMapper.selectById(r.getRecommendHospitalId());
            if (d != null && Boolean.TRUE.equals(d.getShowStatus())
                    && (d.getIsDelete() == null || !d.getIsDelete())) {
                FrontMedicalDoctorBrief doctorBrief = new FrontMedicalDoctorBrief();
                doctorBrief.setId(d.getId());
                doctorBrief.setHospitalName(d.getHospitalName());
                doctorBrief.setDoctorName(d.getDoctorName());
                if (d.getDepartmentType() != null) {
                    doctorBrief.setDepartmentType(Boolean.TRUE.equals(d.getDepartmentType()) ? 1 : 0);
                }
                doctorBrief.setRegion(d.getRegion());
                doctorBrief.setContactPhone(d.getContactPhone());
                doctorBrief.setAppointmentUrl(d.getAppointmentUrl());
                dto.setDoctorInfo(doctorBrief);
            }
        }

        return dto;
    }

    /**
     * 标记就医推荐为已查看
     */
    public void recommendMarkViewed(FrontMedicalRecommendMarkViewedInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后操作");
        }
        if (input == null || input.getRecommendId() == null || input.getRecommendId() <= 0) {
            throw new CustomException("RecommendId不能为空");
        }
        TUserMedicalRecommend r = userMedicalRecommendMapper.selectOne(
            Wrappers.<TUserMedicalRecommend>lambdaQuery()
                    .eq(TUserMedicalRecommend::getId, input.getRecommendId())
                    .eq(TUserMedicalRecommend::getUserId, userId)
                    .and(w -> w.isNull(TUserMedicalRecommend::getIsDelete)
                            .or().ne(TUserMedicalRecommend::getIsDelete, true))
                    .last("LIMIT 1")
        );
        if (r == null) {
            throw new CustomException("就医推荐记录不存在或已删除");
        }
        r.setViewStatus(true);
        r.setUpdateTime(LocalDateTime.now());
        userMedicalRecommendMapper.updateById(r);
    }

    /**
     * 用户对就医推荐的简单反馈（当前实现仅落库，不做复杂策略联动）
     */
    public void recommendFeedback(FrontMedicalRecommendFeedbackInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后操作");
        }
        if (input == null || input.getRecommendId() == null || input.getRecommendId() <= 0) {
            throw new CustomException("RecommendId不能为空");
        }
        // 当前实体尚未包含 Feedback 字段，本方法预留，暂不具体实现
        // 未来当 TUserMedicalRecommend 增加 FeedbackType / FeedbackRemark 字段后，可在此处更新对应字段。
    }

    /**
     * 创建就诊准备报告：生成一条记录，并立即构造 HTML 内容与预览链接（同步完成）
     */
    public void prepareReportCreate(FrontMedicalPrepareReportCreateInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后生成报告");
        }
        if (input == null) input = new FrontMedicalPrepareReportCreateInput();
        TUserMedicalPrepareReport entity = new TUserMedicalPrepareReport();
        entity.setUserId(userId);

        // 关联最近一条健康档案（如有）
        TPersonalLaryngealHealthRecord record = healthRecordMapper.selectOne(
                Wrappers.<TPersonalLaryngealHealthRecord>lambdaQuery()
                        .eq(TPersonalLaryngealHealthRecord::getUserId, userId)
                        .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete)
                                .or().ne(TPersonalLaryngealHealthRecord::getIsDelete, true))
                        .orderByDesc(TPersonalLaryngealHealthRecord::getCreationTime)
                        .last("LIMIT 1")
        );
        if (record != null) {
            entity.setHealthRecordId(record.getId());
        }

        // 选取关联检测/症状/饮食记录（若不传则按最近 N 条兜底）
        List<Integer> detectIds = selectDetectIdsForReport(userId, input.getDetectIdList(), 5);
        List<Integer> symptomIds = selectLatestSymptomIds(userId, 10);
        List<Integer> dietRecordIds = selectLatestDietRecordIds(userId, 20);

        // PrimaryDetectId / DetectIdList
        if (!detectIds.isEmpty()) {
            entity.setPrimaryDetectId(detectIds.get(0));
            entity.setDetectIdList(joinIds(detectIds));
        } else {
            entity.setPrimaryDetectId(null);
            entity.setDetectIdList(null);
        }

        entity.setTemplateType(input.getTemplateType() != null && input.getTemplateType() == 1);
        entity.setContentScope(input.getContentScope());

        // 报告标题 & 编号
        String title = entity.getTemplateType() != null && entity.getTemplateType()
                ? "就诊准备报告（详版）"
                : "就诊准备报告（简版）";
        entity.setReportTitle(title);
        entity.setReportNo("MR-" + System.currentTimeMillis());

        // 标准文档类型：HTML 为主
        entity.setStandardDocType("HTML");

        // 先插入获取主键
        entity.setGenerateStatus(false);
        entity.setGenerateFailReason(null);
        entity.setViewCount(0);
        entity.setDownloadCount(0);
        entity.setShareCount(0);
        entity.setIsDelete(false);
        entity.setCreationTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        prepareReportMapper.insert(entity);

        // 写入快照 JSON（ReportContentJson）
        ReportSnapshot snapshot = new ReportSnapshot();
        snapshot.ReportId = entity.getId();
        snapshot.UserId = userId;
        snapshot.TemplateType = entity.getTemplateType() != null && entity.getTemplateType() ? 1 : 0;
        snapshot.HealthRecordId = entity.getHealthRecordId();
        snapshot.ContentScope = input.getContentScope();
        snapshot.DetectIds = detectIds;
        snapshot.SymptomIds = symptomIds;
        snapshot.DietRecordIds = dietRecordIds;
        snapshot.CreatedTime = entity.getCreationTime() != null
                ? entity.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;

        // 风险等级快照
        Integer riskLevel = record == null ? null : record.getRiskAssessmentLevel();
        snapshot.RiskLevel = riskLevel;
        snapshot.RiskLevelText = riskLevelToText(riskLevel);

        try {
            entity.setReportContentJson(objectMapper.writeValueAsString(snapshot));
        } catch (Exception ex) {
            // 若序列化失败，则写入空，HTML 端会兜底重建
            entity.setReportContentJson(null);
        }

        // 生成 HTML / PDF 访问 URL（前端通过 web-view / download 使用）
        String idParam = String.valueOf(entity.getId());
        String htmlUrl = "/Front/Medical/PrepareReportHtml?reportId=" + idParam;
        String pdfUrl = "/Front/Medical/PrepareReportPdf?reportId=" + idParam;
        entity.setStandardDocUrl(htmlUrl);
        entity.setHtmlDocUrl(htmlUrl);
        entity.setPdfDocUrl(pdfUrl);

        // 简单认为生成完成
        entity.setGenerateStatus(true);
        entity.setUpdateTime(LocalDateTime.now());
        prepareReportMapper.updateById(entity);
    }

    /**
     * 报告列表
     */
    public PagedResult<FrontMedicalPrepareReportListItem> prepareReportList(FrontMedicalPrepareReportListInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后查看报告列表");
        }
        if (input == null) input = new FrontMedicalPrepareReportListInput();
        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        LambdaQueryWrapper<TUserMedicalPrepareReport> qw = Wrappers.<TUserMedicalPrepareReport>lambdaQuery()
                .eq(TUserMedicalPrepareReport::getUserId, userId)
                .and(w -> w.isNull(TUserMedicalPrepareReport::getIsDelete)
                        .or().ne(TUserMedicalPrepareReport::getIsDelete, true))
                .orderByDesc(TUserMedicalPrepareReport::getCreationTime);

        Page<TUserMedicalPrepareReport> mpPage = new Page<>(page, limit);
        Page<TUserMedicalPrepareReport> result = prepareReportMapper.selectPage(mpPage, qw);
        long total = prepareReportMapper.selectCount(qw);
        List<TUserMedicalPrepareReport> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return PagedResult.GetEmptyInstance();
        }

        List<FrontMedicalPrepareReportListItem> items = new ArrayList<>();
        for (TUserMedicalPrepareReport r : records) {
            FrontMedicalPrepareReportListItem dto = new FrontMedicalPrepareReportListItem();
            dto.setId(r.getId());
            String title = r.getReportTitle();
            if (title == null || title.isEmpty()) {
                title = r.getTemplateType() != null && r.getTemplateType()
                        ? "就诊准备报告（详版）"
                        : "就诊准备报告（简版）";
            }
            dto.setReportTitle(title);
            dto.setCreationTime(r.getCreationTime());
            Integer status = mapGenerateStatus(r.getGenerateStatus(), r.getStandardDocUrl());
            dto.setGenerateStatus(status);
            dto.setViewCount(r.getViewCount());
            items.add(dto);
        }

        return PagedResult.GetInstance(items, total);
    }

    /**
     * 报告详情
     */
    public FrontMedicalPrepareReportDetailOutput prepareReportDetail(FrontMedicalPrepareReportDetailInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后查看报告");
        }
        if (input == null || input.getReportId() == null || input.getReportId() <= 0) {
            throw new CustomException("ReportId不能为空");
        }
        TUserMedicalPrepareReport r = prepareReportMapper.selectOne(
                Wrappers.<TUserMedicalPrepareReport>lambdaQuery()
                        .eq(TUserMedicalPrepareReport::getId, input.getReportId())
                        .eq(TUserMedicalPrepareReport::getUserId, userId)
                        .and(w -> w.isNull(TUserMedicalPrepareReport::getIsDelete)
                                .or().ne(TUserMedicalPrepareReport::getIsDelete, true))
                        .last("LIMIT 1")
        );
        if (r == null) {
            throw new CustomException("报告不存在或已删除");
        }

        FrontMedicalPrepareReportDetailOutput dto = new FrontMedicalPrepareReportDetailOutput();
        dto.setId(r.getId());
        String title = r.getReportTitle();
        if (title == null || title.isEmpty()) {
            title = r.getTemplateType() != null && r.getTemplateType()
                    ? "就诊准备报告（详版）"
                    : "就诊准备报告（简版）";
        }
        dto.setReportTitle(title);
        Integer status = mapGenerateStatus(r.getGenerateStatus(), r.getStandardDocUrl());
        dto.setGenerateStatus(status);
        dto.setGenerateFailReason(r.getGenerateFailReason());
        dto.setStandardDocUrl(r.getStandardDocUrl());
        dto.setHtmlDocUrl(r.getHtmlDocUrl());
        dto.setPdfDocUrl(r.getPdfDocUrl());
        dto.setCoverImageUrl(r.getCoverImageUrl());

        // 简单统计：查看次数 +1
        if (r.getViewCount() == null) {
            r.setViewCount(1);
        } else {
            r.setViewCount(r.getViewCount() + 1);
        }
        prepareReportMapper.updateById(r);

        return dto;
    }

    /**
     * HTML 报告内容（用于 web-view）
     */
    public String prepareReportHtml(Integer reportId) {
        // 说明：HTML/PDF 报告通过 web-view / 浏览器直接访问，无法自动携带登录态 Header，
        // 因此这里仅根据 reportId + IsDelete 进行校验，不再强依赖 BaseContext 中的 UserId。
        if (reportId == null || reportId <= 0) throw new CustomException("ReportId不能为空");

        TUserMedicalPrepareReport r = prepareReportMapper.selectOne(
                Wrappers.<TUserMedicalPrepareReport>lambdaQuery()
                        .eq(TUserMedicalPrepareReport::getId, reportId)
                        .and(w -> w.isNull(TUserMedicalPrepareReport::getIsDelete)
                                .or().ne(TUserMedicalPrepareReport::getIsDelete, true))
                        .last("LIMIT 1")
        );
        if (r == null) throw new CustomException("报告不存在或已删除");

        String content = r.getReportContentJson();
        if (content != null && content.trim().startsWith("<")) {
            // 兼容历史：ReportContentJson 存放 HTML
            return content;
        }

        ReportSnapshot snapshot = null;
        if (content != null && !content.trim().isEmpty()) {
            try {
                snapshot = objectMapper.readValue(content, new TypeReference<ReportSnapshot>() {});
            } catch (Exception ignore) {
                snapshot = null;
            }
        }
        if (snapshot == null) {
            // 若快照缺失，现场构造一个最小快照并写回
            snapshot = new ReportSnapshot();
            snapshot.ReportId = r.getId();
            snapshot.UserId = r.getUserId();
            snapshot.TemplateType = r.getTemplateType() != null && r.getTemplateType() ? 1 : 0;
            snapshot.HealthRecordId = r.getHealthRecordId();
            snapshot.DetectIds = parseIds(r.getDetectIdList());
            if (snapshot.DetectIds == null || snapshot.DetectIds.isEmpty()) {
                snapshot.DetectIds = selectDetectIdsForReport(r.getUserId(), null, 5);
            }
            snapshot.SymptomIds = selectLatestSymptomIds(r.getUserId(), 10);
            snapshot.DietRecordIds = selectLatestDietRecordIds(r.getUserId(), 20);
            snapshot.CreatedTime = r.getCreationTime() != null
                    ? r.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : null;
            try {
                r.setReportContentJson(objectMapper.writeValueAsString(snapshot));
                prepareReportMapper.updateById(r);
            } catch (Exception ignore) {
            }
        }

        return renderReportHtml(r, snapshot);
    }

    /**
     * PDF 报告（后端直接生成，返回 byte[]）
     */
    public byte[] prepareReportPdf(Integer reportId) {
        String html = prepareReportHtml(reportId);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            // 尝试为中文内容配置可用字体，避免 PDF 中中文被渲染为“#”
            configurePdfFonts(builder);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new CustomException("PDF 生成失败：" + ex.getMessage());
        }
    }

    /**
     * 为 openhtmltopdf 配置中文字体，避免 PDF 中中文字符变成“#”
     * 说明：这里尝试加载几种常见系统字体，若不存在则忽略，不影响整体生成流程。
     */
    private void configurePdfFonts(PdfRendererBuilder builder) {
        // Windows 11 下优先尝试系统自带字体；Linux 服务器则使用 Noto CJK
        Object[][] fonts = new Object[][]{
                {"C:/Windows/Fonts/msyh.ttc", "Microsoft YaHei"},
                {"C:/Windows/Fonts/msyh.ttc", "微软雅黑"},
                {"C:/Windows/Fonts/simsun.ttc", "SimSun"},
                {"C:/Windows/Fonts/simsun.ttc", "宋体"},
                {"/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc", "Noto Sans CJK SC"},
                {"/usr/share/fonts/truetype/noto/NotoSansCJKsc-Regular.otf", "Noto Sans CJK SC"}
        };

        for (Object[] fInfo : fonts) {
            String path = (String) fInfo[0];
            String family = (String) fInfo[1];
            try {
                File f = new File(path);
                if (f.exists()) {
                    // family 名称要与 CSS 中的 font-family 保持一致，这样中文才能命中该字体
                    builder.useFont(
                            f,
                            family,
                            400,
                            com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle.NORMAL,
                            true
                    );
                }
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 创建导出记录
     */
    public void exportRecordCreate(FrontMedicalExportRecordCreateInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后导出报告");
        }
        if (input == null) {
            throw new CustomException("请求体不能为空");
        }
        TScreenReportExportRecord e = new TScreenReportExportRecord();
        e.setUserId(userId);
        e.setDetectId(input.getDetectId());
        e.setExportTime(LocalDateTime.now());
        // 目前实体中 ExportFormat / ExportPurpose 为 Boolean，这里简单全部置为 true，实际语义可后续通过新增字段补充
        e.setExportFormat(true);
        e.setExportPurpose(true);
        e.setExportFileUrl(input.getExportFileUrl());
        e.setIsDelete(false);
        exportRecordMapper.insert(e);
    }

    /**
     * 导出记录列表
     */
    public PagedResult<FrontMedicalExportRecordListItem> exportRecordList(FrontMedicalExportRecordListInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后查看导出记录");
        }
        if (input == null) input = new FrontMedicalExportRecordListInput();
        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        LambdaQueryWrapper<TScreenReportExportRecord> qw = Wrappers.<TScreenReportExportRecord>lambdaQuery()
                .eq(TScreenReportExportRecord::getUserId, userId)
                .and(w -> w.isNull(TScreenReportExportRecord::getIsDelete)
                        .or().ne(TScreenReportExportRecord::getIsDelete, true))
                .orderByDesc(TScreenReportExportRecord::getExportTime)
                .orderByDesc(TScreenReportExportRecord::getCreationTime);

        Page<TScreenReportExportRecord> mpPage = new Page<>(page, limit);
        Page<TScreenReportExportRecord> result = exportRecordMapper.selectPage(mpPage, qw);
        long total = exportRecordMapper.selectCount(qw);
        List<TScreenReportExportRecord> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return PagedResult.GetEmptyInstance();
        }

        List<FrontMedicalExportRecordListItem> items = new ArrayList<>();
        for (TScreenReportExportRecord e : records) {
            FrontMedicalExportRecordListItem dto = new FrontMedicalExportRecordListItem();
            dto.setId(e.getId());
            dto.setExportTime(e.getExportTime());
            // 当前格式/用途仅有 Boolean 字段，这里简单映射为 VIEW / UNKNOWN
            dto.setExportFormat(Boolean.TRUE.equals(e.getExportFormat()) ? "VIEW" : "UNKNOWN");
            dto.setExportPurpose(Boolean.TRUE.equals(e.getExportPurpose()) ? "VIEW" : "UNKNOWN");
            dto.setExportPurposeText(null);
            dto.setExportFileUrl(e.getExportFileUrl());
            items.add(dto);
        }

        return PagedResult.GetInstance(items, total);
    }

    // ======================
    // 辅助方法
    // ======================

    private Integer getCurrentUserId() {
        if (BaseContext.getCurrentUserDto() == null || BaseContext.getCurrentUserDto().getUserId() == null) {
            return 0;
        }
        return BaseContext.getCurrentUserDto().getUserId();
    }

    private String renderReportHtml(TUserMedicalPrepareReport report, ReportSnapshot snapshot) {
        // 关联健康档案（风险等级等）
        TPersonalLaryngealHealthRecord record = null;
        if (report.getHealthRecordId() != null) {
            record = healthRecordMapper.selectById(report.getHealthRecordId());
        }
        if (record == null) {
            record = healthRecordMapper.selectOne(
                    Wrappers.<TPersonalLaryngealHealthRecord>lambdaQuery()
                            .eq(TPersonalLaryngealHealthRecord::getUserId, report.getUserId())
                            .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete)
                                    .or().ne(TPersonalLaryngealHealthRecord::getIsDelete, true))
                            .orderByDesc(TPersonalLaryngealHealthRecord::getCreationTime)
                            .last("LIMIT 1")
            );
        }

        List<Integer> detectIds = snapshot.DetectIds == null ? new ArrayList<>() : snapshot.DetectIds;
        List<Integer> symptomIds = snapshot.SymptomIds == null ? new ArrayList<>() : snapshot.SymptomIds;
        List<Integer> dietRecordIds = snapshot.DietRecordIds == null ? new ArrayList<>() : snapshot.DietRecordIds;

        Map<Integer, TAudioScreenRecord> detectMap = new HashMap<>();
        if (!detectIds.isEmpty()) {
            List<TAudioScreenRecord> ds = audioScreenRecordMapper.selectBatchIds(detectIds);
            for (TAudioScreenRecord d : ds) {
                detectMap.put(d.getId(), d);
            }
        }

        Map<Integer, TSymptomLog> symptomMap = new HashMap<>();
        if (!symptomIds.isEmpty()) {
            List<TSymptomLog> ss = symptomLogMapper.selectBatchIds(symptomIds);
            for (TSymptomLog s : ss) {
                symptomMap.put(s.getId(), s);
            }
        }

        Map<Integer, TUserDietRecord> dietMap = new HashMap<>();
        if (!dietRecordIds.isEmpty()) {
            List<TUserDietRecord> rs = userDietRecordMapper.selectBatchIds(dietRecordIds);
            for (TUserDietRecord r : rs) {
                dietMap.put(r.getId(), r);
            }
        }

        // 食物名映射
        Map<Integer, TLaryngealFood> foodMap = new HashMap<>();
        List<Integer> foodIds = dietMap.values().stream()
                .map(TUserDietRecord::getFoodId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!foodIds.isEmpty()) {
            List<TLaryngealFood> foods = foodMapper.selectBatchIds(foodIds);
            for (TLaryngealFood f : foods) {
                foodMap.put(f.getId(), f);
            }
        }

        String title = safe(report.getReportTitle(), "就诊准备报告");
        String created = report.getCreationTime() != null
                ? report.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : safe(snapshot.CreatedTime, "");
        Integer riskLevel = record == null ? snapshot.RiskLevel : record.getRiskAssessmentLevel();
        String riskText = snapshot.RiskLevelText != null ? snapshot.RiskLevelText : riskLevelToText(riskLevel);
        boolean isDetail = (report.getTemplateType() != null && report.getTemplateType())
                || (snapshot.TemplateType != null && snapshot.TemplateType == 1);

        // 最近喉镜照片（仅作就诊记录参考，不参与当前版本模型自动判断）
        List<TLaryngoscopePhoto> laryngoscopePhotos = laryngoscopePhotoMapper.selectList(
                Wrappers.<TLaryngoscopePhoto>lambdaQuery()
                        .eq(TLaryngoscopePhoto::getUserId, report.getUserId())
                        .and(w -> w.isNull(TLaryngoscopePhoto::getIsDelete)
                                .or().ne(TLaryngoscopePhoto::getIsDelete, true))
                        .orderByDesc(TLaryngoscopePhoto::getUploadTime)
                        .orderByDesc(TLaryngoscopePhoto::getCreationTime)
                        .last("LIMIT " + (isDetail ? 4 : 1))
        );

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"/>")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>")
                .append("<title>").append(escapeHtml(title)).append("</title>")
                .append("<style>")
                // 增加顶部内边距，避免在小程序 web-view 中被导航栏轻微遮挡；
                // 同时在字体栈中加入常见中文字体，保证浏览器和 PDF 中中文展示更清晰
                .append("body{font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Arial,")
                .append("\"Microsoft YaHei\",\"SimSun\",\"PingFang SC\",\"Noto Sans CJK SC\",sans-serif;")
                // 顶部多留一些空间，避免被小程序导航条遮挡
                .append("margin:0;padding:64px 16px 24px;background:#f5f7fa;}")
                .append(".card{background:#fff;border-radius:10px;padding:16px;margin-bottom:12px;box-shadow:0 1px 3px rgba(0,0,0,0.08);}")
                .append(".title{font-size:20px;font-weight:700;margin-bottom:6px;color:#1f2937;}")
                .append(".sub{font-size:13px;color:#6b7280;margin-bottom:6px;}")
                .append(".pill{display:inline-block;padding:3px 10px;border-radius:999px;background:#eef2ff;color:#3730a3;font-size:12px;margin-right:8px;}")
                .append(".section-title{font-size:16px;font-weight:700;margin-bottom:10px;color:#111827;}")
                .append(".text{font-size:14px;color:#374151;line-height:1.65;}")
                .append("table{width:100%;border-collapse:collapse;font-size:13px;}")
                .append("th,td{border-bottom:1px solid #eef2f7;padding:8px 6px;vertical-align:top;text-align:left;}")
                .append("th{color:#6b7280;font-weight:600;}")
                .append(".muted{color:#9ca3af;}")
                .append(".img-section{display:flex;flex-direction:column;gap:8px;}")
                .append(".img-row{display:flex;flex-wrap:wrap;gap:8px;margin-top:4px;}")
                .append(".img-item{flex:1 1 calc(33.33% - 8px);max-width:100%;}")
                .append(".img-item img{width:100%;border-radius:6px;border:1px solid #e5e7eb;}")
                .append(".img-caption{font-size:12px;color:#6b7280;margin-top:2px;}")
                .append("</style></head><body>");

        sb.append("<div class=\"card\">")
                .append("<div class=\"title\">").append(escapeHtml(title)).append("</div>")
                .append("<div class=\"sub\"><span class=\"pill\">生成时间</span>").append(escapeHtml(created)).append("</div>")
                .append("<div class=\"sub\"><span class=\"pill\">风险等级</span>").append(escapeHtml(riskText)).append("</div>")
                .append("</div>");

        sb.append("<div class=\"card\">")
                .append("<div class=\"section-title\">本报告用途说明</div>")
                .append("<div class=\"text\">")
                .append("本报告用于帮助您在线下就诊时，向耳鼻喉相关专科医生更清晰地说明既往自查、症状变化及生活习惯。")
                .append("本报告不构成任何诊断结论，最终诊断与处置方案请以线下专科医生意见为准。")
                .append("</div></div>");

        // 检测记录
        sb.append("<div class=\"card\">")
                .append("<div class=\"section-title\">关联检测记录（音频自查）</div>");
        if (detectIds.isEmpty()) {
            sb.append("<div class=\"text muted\">暂无关联检测记录。</div>");
        } else {
            sb.append("<table><thead><tr>")
                    .append("<th>时间</th><th>结果</th><th>置信度</th><th>模型版本</th>")
                    .append("</tr></thead><tbody>");
            int shownDetect = 0;
            for (Integer id : detectIds) {
                TAudioScreenRecord d = detectMap.get(id);
                if (d == null) continue;
                String time = d.getCreationTime() == null ? "-" : d.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String result = "未知";
                if (d.getPrimaryScreenResult() != null) result = Boolean.TRUE.equals(d.getPrimaryScreenResult()) ? "恶性倾向" : "良性倾向";
                String conf = d.getPrimaryScreenConfidence() == null ? "-" : String.format("%.1f%%", d.getPrimaryScreenConfidence() * 100);
                String ver = safe(d.getDensenetModelVersion(), "-");
                sb.append("<tr>")
                        .append("<td>").append(escapeHtml(time)).append("</td>")
                        .append("<td>").append(escapeHtml(result)).append("</td>")
                        .append("<td>").append(escapeHtml(conf)).append("</td>")
                        .append("<td>").append(escapeHtml(ver)).append("</td>")
                        .append("</tr>");
                shownDetect++;
                // 简版仅展示最近 1 条检测记录；详版展示全部
                if (!isDetail && shownDetect >= 1) {
                    break;
                }
            }
            sb.append("</tbody></table>");
        }
        sb.append("</div>");

        // 最近一至两次检测的图谱预览（波形 / MFCC / Mel）
        sb.append("<div class=\"card\">")
                .append("<div class=\"section-title\">音频图谱预览（最近两次检测）</div>");
        if (detectIds.isEmpty()) {
            sb.append("<div class=\"text muted\">暂无可用的检测图谱。</div>");
        } else {
            // 简版：展示最近 1 次图谱；详版：展示最近 2 次图谱（如仅 1 次则展示 1 次）
            int limit = isDetail ? 2 : 1;
            int maxCharts = Math.min(limit, detectIds.size());
            for (int i = 0; i < maxCharts; i++) {
                Integer id = detectIds.get(i);
                TAudioScreenRecord d = detectMap.get(id);
                if (d == null) continue;
                String time = d.getCreationTime() == null ? "-" : d.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String result = "未知";
                if (d.getPrimaryScreenResult() != null) {
                    result = Boolean.TRUE.equals(d.getPrimaryScreenResult()) ? "恶性倾向" : "良性倾向";
                }
                sb.append("<div class=\"img-section\">")
                        .append("<div class=\"text\"><span class=\"pill\">检测时间</span>")
                        .append(escapeHtml(time))
                        .append("　<span class=\"pill\">结果</span>")
                        .append(escapeHtml(result))
                        .append("</div>")
                        .append("<div class=\"img-row\">");

                if (Extension.isNotNullOrEmpty(d.getWaveformUrl())) {
                    sb.append("<div class=\"img-item\">")
                            .append("<img src=\"").append(escapeHtml(d.getWaveformUrl())).append("\" alt=\"波形图\"/>")
                            .append("<div class=\"img-caption\">波形图</div>")
                            .append("</div>");
                }
                if (Extension.isNotNullOrEmpty(d.getMfccSpectrumUrl())) {
                    sb.append("<div class=\"img-item\">")
                            .append("<img src=\"").append(escapeHtml(d.getMfccSpectrumUrl())).append("\" alt=\"MFCC 图谱\"/>")
                            .append("<div class=\"img-caption\">MFCC 图谱</div>")
                            .append("</div>");
                }
                if (Extension.isNotNullOrEmpty(d.getMelSpectrumUrl())) {
                    sb.append("<div class=\"img-item\">")
                            .append("<img src=\"").append(escapeHtml(d.getMelSpectrumUrl())).append("\" alt=\"Mel 图谱\"/>")
                            .append("<div class=\"img-caption\">Mel 图谱</div>")
                            .append("</div>");
                }

                sb.append("</div>")  // .img-row
                        .append("</div>"); // .img-section
            }
        }
        sb.append("</div>");

        // 症状记录
        sb.append("<div class=\"card\">")
                .append("<div class=\"section-title\">关联症状记录</div>");
        if (symptomIds.isEmpty()) {
            sb.append("<div class=\"text muted\">暂无症状记录。</div>");
        } else {
            sb.append("<table><thead><tr>")
                    .append("<th>时间</th><th>症状</th><th>程度</th><th>持续</th><th>备注</th>")
                    .append("</tr></thead><tbody>");
            for (Integer id : symptomIds) {
                TSymptomLog s = symptomMap.get(id);
                if (s == null) continue;
                String time = s.getCreationTime() == null ? "-" : s.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String type = safe(s.getSymptomType(), "-");
                String level = symptomLevelText(s.getSymptomLevel());
                String dur = safe(s.getSymptomDuration(), "-");
                String remark = safe(s.getRemark(), "");
                sb.append("<tr>")
                        .append("<td>").append(escapeHtml(time)).append("</td>")
                        .append("<td>").append(escapeHtml(type)).append("</td>")
                        .append("<td>").append(escapeHtml(level)).append("</td>")
                        .append("<td>").append(escapeHtml(dur)).append("</td>")
                        .append("<td>").append(escapeHtml(remark)).append("</td>")
                        .append("</tr>");
            }
            sb.append("</tbody></table>");
        }
        sb.append("</div>");

        // 饮食记录：仅在详版报告中展示详细表格，简版给出引导文案
        sb.append("<div class=\"card\">")
                .append("<div class=\"section-title\">关联饮食记录</div>");
        if (!isDetail) {
            sb.append("<div class=\"text muted\">当前为简版报告，仅展示核心检测/症状信息。如需查看近期开口饮食的详细记录，请生成“就诊准备报告（详版）”。</div>");
        } else {
        if (dietRecordIds.isEmpty()) {
            sb.append("<div class=\"text muted\">暂无饮食记录。</div>");
        } else {
            sb.append("<table><thead><tr>")
                    .append("<th>时间</th><th>食物</th><th>频次</th><th>感受</th>")
                    .append("</tr></thead><tbody>");
            for (Integer id : dietRecordIds) {
                TUserDietRecord dr = dietMap.get(id);
                if (dr == null) continue;
                String time = dr.getIntakeTime() == null ? "-" : dr.getIntakeTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                TLaryngealFood f = dr.getFoodId() == null ? null : foodMap.get(dr.getFoodId());
                String foodName = f == null ? (dr.getFoodId() == null ? "-" : ("FoodId=" + dr.getFoodId())) : safe(f.getFoodName(), "-");
                String freq = dr.getIntakeFrequency() == null ? "-" : String.valueOf(dr.getIntakeFrequency());
                String feeling = safe(dr.getEatFeeling(), "");
                sb.append("<tr>")
                        .append("<td>").append(escapeHtml(time)).append("</td>")
                        .append("<td>").append(escapeHtml(foodName)).append("</td>")
                        .append("<td>").append(escapeHtml(freq)).append("</td>")
                        .append("<td>").append(escapeHtml(feeling)).append("</td>")
                        .append("</tr>");
            }
            sb.append("</tbody></table>");
            }
        }
        sb.append("</div>");

        // 喉镜影像资料
        sb.append("<div class=\"card\">")
                .append("<div class=\"section-title\">喉镜检查影像（既往检查资料）</div>");
        if (laryngoscopePhotos == null || laryngoscopePhotos.isEmpty()) {
            sb.append("<div class=\"text muted\">当前暂无已记录的喉镜检查影像。</div>");
        } else {
            sb.append("<div class=\"text\">下列喉镜影像仅用于回顾既往检查情况，不参与当前版本模型的自动判断。</div>");
            sb.append("<div class=\"img-row\">");
            for (TLaryngoscopePhoto p : laryngoscopePhotos) {
                if (p.getLaryngoscopePhotoUrl() == null || p.getLaryngoscopePhotoUrl().trim().isEmpty()) {
                    continue;
                }
                String time = p.getUploadTime() == null
                        ? "-"
                        : p.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String desc = safe(p.getPhotoDesc(), "");
                sb.append("<div class=\"img-item\">")
                        .append("<img src=\"").append(escapeHtml(p.getLaryngoscopePhotoUrl())).append("\" alt=\"喉镜影像\"/>")
                        .append("<div class=\"img-caption\">")
                        .append(escapeHtml(time));
                if (!desc.isEmpty()) {
                    sb.append("｜").append(escapeHtml(desc));
                }
                sb.append("</div>")
                        .append("</div>");
                // 简版只展示 1 张；详版展示最多 4 张，上面 LIMIT 已控制
            }
            sb.append("</div>");
        }
        sb.append("</div>");

        sb.append("<div class=\"card\">")
                .append("<div class=\"section-title\">温馨提示</div>")
                .append("<div class=\"text\">")
                .append("如出现呼吸困难、持续咯血、吞咽明显困难等急重症状，请及时就近急诊或拨打急救电话。")
                .append("</div></div>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private List<Integer> selectDetectIdsForReport(Integer userId, List<Integer> inputIds, int defaultLimit) {
        if (inputIds != null) {
            List<Integer> ids = inputIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (!ids.isEmpty()) return ids;
        }
        List<TAudioScreenRecord> list = audioScreenRecordMapper.selectList(
                Wrappers.<TAudioScreenRecord>lambdaQuery()
                        .select(TAudioScreenRecord::getId)
                        .eq(TAudioScreenRecord::getUserId, userId)
                        .and(w -> w.isNull(TAudioScreenRecord::getIsDelete).or().ne(TAudioScreenRecord::getIsDelete, true))
                        .orderByDesc(TAudioScreenRecord::getCreationTime)
                        .last("LIMIT " + defaultLimit)
        );
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return list.stream().map(TAudioScreenRecord::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Integer> selectLatestSymptomIds(Integer userId, int limit) {
        List<TSymptomLog> list = symptomLogMapper.selectList(
                Wrappers.<TSymptomLog>lambdaQuery()
                        .select(TSymptomLog::getId)
                        .eq(TSymptomLog::getUserId, userId)
                        .and(w -> w.isNull(TSymptomLog::getIsDelete).or().ne(TSymptomLog::getIsDelete, true))
                        .orderByDesc(TSymptomLog::getCreationTime)
                        .last("LIMIT " + limit)
        );
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return list.stream().map(TSymptomLog::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Integer> selectLatestDietRecordIds(Integer userId, int limit) {
        List<TUserDietRecord> list = userDietRecordMapper.selectList(
                Wrappers.<TUserDietRecord>lambdaQuery()
                        .select(TUserDietRecord::getId)
                        .eq(TUserDietRecord::getUserId, userId)
                        .and(w -> w.isNull(TUserDietRecord::getIsDelete).or().ne(TUserDietRecord::getIsDelete, true))
                        .orderByDesc(TUserDietRecord::getIntakeTime)
                        .last("LIMIT " + limit)
        );
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return list.stream().map(TUserDietRecord::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Integer> parseIds(String ids) {
        if (ids == null || ids.trim().isEmpty()) return new ArrayList<>();
        String[] parts = ids.split(",");
        List<Integer> res = new ArrayList<>();
        for (String p : parts) {
            String t = p == null ? "" : p.trim();
            if (t.isEmpty()) continue;
            try {
                res.add(Integer.parseInt(t));
            } catch (Exception ignore) {
            }
        }
        return res;
    }

    private String joinIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return ids.stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(","));
    }

    private String symptomLevelText(Integer level) {
        if (level == null) return "未知";
        if (level == 1) return "轻度";
        if (level == 2) return "中度";
        if (level == 3) return "重度";
        return "未知";
    }

    private String riskLevelToText(Integer riskLevel) {
        if (riskLevel == null) return "未知";
        if (riskLevel == 0) return "低风险";
        if (riskLevel == 1) return "中风险";
        if (riskLevel == 2) return "高风险";
        return "未知";
    }

    private String safe(String s, String def) {
        if (s == null || s.trim().isEmpty()) return def;
        return s;
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * 将实体中的 GenerateStatus(Boolean) 与 StandardDocUrl 映射到前端期望的 0/1/2/3 状态
     */
    private Integer mapGenerateStatus(Boolean generateStatus, String standardDocUrl) {
        // 当前表结构中 GenerateStatus 为 Boolean，无法完整表达 0/1/2/3，这里做一个最小兼容映射：
        // 有可用 URL 时视为已完成(2)，否则视为待生成(0)
        if (standardDocUrl != null && !standardDocUrl.trim().isEmpty()) {
            return 2;
        }
        return 0;
    }

    private String mapGenerateStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0:
                return "待生成";
            case 1:
                return "生成中";
            case 2:
                return "已完成";
            case 3:
                return "生成失败";
            default:
                return "未知";
        }
    }
}

