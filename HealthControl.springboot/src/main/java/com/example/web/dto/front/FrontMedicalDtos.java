package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 就医辅助模块前台 DTO 汇总
 * 为了简化文件数量，这里放在一个文件中多个内部类，均为 public。
 */
public class FrontMedicalDtos {

    // ======================
    // 首页聚合
    // ======================

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class FrontMedicalHomeOutput {
        /**
         * 当前风险等级：0=低，1=中，2=高；为空表示未知/待评定
         */
        @JsonProperty("RiskLevel")
        private Integer RiskLevel;

        /**
         * 最近一次检测摘要
         */
        @JsonProperty("LatestDetect")
        private FrontMedicalLatestDetectBrief LatestDetect;

        /**
         * 最近症状摘要
         */
        @JsonProperty("LatestSymptom")
        private FrontMedicalLatestSymptomBrief LatestSymptom;

        /**
         * 最新一条就医推荐摘要
         */
        @JsonProperty("LatestRecommend")
        private FrontMedicalRecommendBrief LatestRecommend;

        /**
         * 最新一份就诊准备报告摘要
         */
        @JsonProperty("LatestReport")
        private FrontMedicalReportBrief LatestReport;

        /**
         * 最近一条就诊计划摘要（当前实现中可为空，预留）
         */
        @JsonProperty("LatestVisitPlan")
        private FrontMedicalVisitPlanBrief LatestVisitPlan;

        /**
         * 对当前风险/检测/症状的整体文字说明
         */
        @JsonProperty("RiskSummaryText")
        private String RiskSummaryText;
    }

    @Data
    public static class FrontMedicalLatestDetectBrief {
        /**
         * 摘要文案，如“2026-03-07 检测结果：良性倾向，置信度 92.3%”
         */
        @JsonProperty("SummaryText")
        private String SummaryText;
    }

    @Data
    public static class FrontMedicalLatestSymptomBrief {
        /**
         * 症状摘要，如“咽干（中度，持续 3 天）”
         */
        @JsonProperty("SymptomSummary")
        private String SymptomSummary;
    }

    @Data
    public static class FrontMedicalRecommendBrief {
        @JsonProperty("RecommendId")
        private Integer RecommendId;

        @JsonProperty("Title")
        private String Title;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("RecommendTime")
        private LocalDateTime RecommendTime;

        @JsonProperty("RecommendReason")
        private String RecommendReason;

        @JsonProperty("ViewStatus")
        private Boolean ViewStatus;
    }

    @Data
    public static class FrontMedicalReportBrief {
        @JsonProperty("ReportId")
        private Integer ReportId;

        @JsonProperty("ReportTitle")
        private String ReportTitle;

        /**
         * 生成状态：0=待生成，1=生成中，2=已完成，3=生成失败
         */
        @JsonProperty("GenerateStatus")
        private Integer GenerateStatus;

        @JsonProperty("GenerateStatusText")
        private String GenerateStatusText;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("CreationTime")
        private LocalDateTime CreationTime;
    }

    @Data
    public static class FrontMedicalVisitPlanBrief {
        @JsonProperty("PlanVisitDate")
        private String PlanVisitDate;

        @JsonProperty("DoctorName")
        private String DoctorName;

        @JsonProperty("PlanChannel")
        private String PlanChannel;
    }

    // ======================
    // 医院 / 医生列表与详情
    // ======================

    @Data
    public static class FrontMedicalDoctorListInput {
        @JsonProperty("Page")
        private Long Page;

        @JsonProperty("Limit")
        private Long Limit;

        @JsonProperty("Region")
        private String Region;

        /**
         * 科室类型：0=耳鼻喉科，1=咽喉科，2=头颈外科；为空表示全部
         */
        @JsonProperty("DepartmentType")
        private Integer DepartmentType;

        /**
         * 关键字：模糊匹配医院/医生
         */
        @JsonProperty("Keyword")
        private String Keyword;
    }

    @Data
    public static class FrontMedicalDoctorListItem {
        @JsonProperty("Id")
        private Integer Id;

        @JsonProperty("HospitalName")
        private String HospitalName;

        @JsonProperty("DoctorName")
        private String DoctorName;

        /**
         * 0=耳鼻喉科，1=咽喉科，2=头颈外科
         */
        @JsonProperty("DepartmentType")
        private Integer DepartmentType;

        @JsonProperty("Region")
        private String Region;

        @JsonProperty("Address")
        private String Address;

        @JsonProperty("ContactPhone")
        private String ContactPhone;

        @JsonProperty("AppointmentUrl")
        private String AppointmentUrl;

        /**
         * 以下字段为增强展示用，当前实体中可能为空
         */
        @JsonProperty("PicUrl")
        private String PicUrl;

        @JsonProperty("DoctorTitle")
        private String DoctorTitle;

        @JsonProperty("SpecialtyDesc")
        private String SpecialtyDesc;

        @JsonProperty("HospitalLevel")
        private String HospitalLevel;
    }

    @Data
    public static class FrontMedicalDoctorDetailInput {
        @JsonProperty("DoctorId")
        private Integer DoctorId;
    }

    @Data
    public static class FrontMedicalDoctorDetail extends FrontMedicalDoctorListItem {

        @JsonProperty("OutpatientTime")
        private String OutpatientTime;

        @JsonProperty("Longitude")
        private Double Longitude;

        @JsonProperty("Latitude")
        private Double Latitude;

        @JsonProperty("Tags")
        private String Tags;
    }

    // ======================
    // 就医推荐列表 / 详情
    // ======================

    @Data
    public static class FrontMedicalRecommendListInput {
        @JsonProperty("Page")
        private Long Page;

        @JsonProperty("Limit")
        private Long Limit;

        /**
         * 查看状态：0=未查看，1=已查看；为空表示全部
         */
        @JsonProperty("ViewStatus")
        private Integer ViewStatus;
    }

    @Data
    public static class FrontMedicalRecommendListItem {
        @JsonProperty("Id")
        private Integer Id;

        @JsonProperty("RiskLevel")
        private Integer RiskLevel;

        @JsonProperty("Title")
        private String Title;

        @JsonProperty("RecommendReason")
        private String RecommendReason;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("RecommendTime")
        private LocalDateTime RecommendTime;

        @JsonProperty("ViewStatus")
        private Boolean ViewStatus;
    }

    @Data
    public static class FrontMedicalRecommendDetailInput {
        @JsonProperty("RecommendId")
        private Integer RecommendId;
    }

    @Data
    public static class FrontMedicalDoctorBrief {
        @JsonProperty("Id")
        private Integer Id;

        @JsonProperty("HospitalName")
        private String HospitalName;

        @JsonProperty("DoctorName")
        private String DoctorName;

        @JsonProperty("DepartmentType")
        private Integer DepartmentType;

        @JsonProperty("Region")
        private String Region;

        @JsonProperty("ContactPhone")
        private String ContactPhone;

        @JsonProperty("AppointmentUrl")
        private String AppointmentUrl;

        @JsonProperty("PicUrl")
        private String PicUrl;

        @JsonProperty("DoctorTitle")
        private String DoctorTitle;
    }

    @Data
    public static class FrontMedicalRecommendDetailOutput {
        @JsonProperty("Id")
        private Integer Id;

        /**
         * 关联的检测记录 Id（用于“去标注”等跳转）
         */
        @JsonProperty("DetectId")
        private Integer DetectId;

        @JsonProperty("RiskLevel")
        private Integer RiskLevel;

        @JsonProperty("Title")
        private String Title;

        @JsonProperty("RecommendReason")
        private String RecommendReason;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("RecommendTime")
        private LocalDateTime RecommendTime;

        @JsonProperty("ViewStatus")
        private Boolean ViewStatus;

        /**
         * 最近检测/症状等简要说明
         */
        @JsonProperty("DetectSummary")
        private String DetectSummary;

        @JsonProperty("SymptomSummary")
        private String SymptomSummary;

        /**
         * 推荐就诊医生/医院（可为空）
         */
        @JsonProperty("DoctorInfo")
        private FrontMedicalDoctorBrief DoctorInfo;
    }

    @Data
    public static class FrontMedicalRecommendMarkViewedInput {
        @JsonProperty("RecommendId")
        private Integer RecommendId;
    }

    @Data
    public static class FrontMedicalRecommendFeedbackInput {
        @JsonProperty("RecommendId")
        private Integer RecommendId;

        /**
         * 反馈类型：0=未反馈，1=已就医，2=暂不需要，3=信息不准等
         */
        @JsonProperty("FeedbackType")
        private Integer FeedbackType;

        @JsonProperty("Remark")
        private String Remark;
    }

    // ======================
    // 就诊准备报告
    // ======================

    @Data
    public static class FrontMedicalPrepareReportCreateInput {
        /**
         * 模板类型：0=简版，1=详版
         */
        @JsonProperty("TemplateType")
        private Integer TemplateType;

        /**
         * 关联的检测 Id 列表（可为空，后端可按“最近 N 次”兜底）
         */
        @JsonProperty("DetectIdList")
        private List<Integer> DetectIdList;

        /**
         * 内容范围约定字符串（可选）
         */
        @JsonProperty("ContentScope")
        private String ContentScope;
    }

    @Data
    public static class FrontMedicalPrepareReportListInput {
        @JsonProperty("Page")
        private Long Page;

        @JsonProperty("Limit")
        private Long Limit;
    }

    @Data
    public static class FrontMedicalPrepareReportListItem {
        @JsonProperty("Id")
        private Integer Id;

        @JsonProperty("ReportTitle")
        private String ReportTitle;

        @JsonProperty("GenerateStatus")
        private Integer GenerateStatus;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("CreationTime")
        private LocalDateTime CreationTime;

        @JsonProperty("ViewCount")
        private Integer ViewCount;
    }

    @Data
    public static class FrontMedicalPrepareReportDetailInput {
        @JsonProperty("ReportId")
        private Integer ReportId;
    }

    @Data
    public static class FrontMedicalPrepareReportDetailOutput {
        @JsonProperty("Id")
        private Integer Id;

        @JsonProperty("ReportTitle")
        private String ReportTitle;

        @JsonProperty("GenerateStatus")
        private Integer GenerateStatus;

        @JsonProperty("GenerateFailReason")
        private String GenerateFailReason;

        @JsonProperty("StandardDocUrl")
        private String StandardDocUrl;

        @JsonProperty("HtmlDocUrl")
        private String HtmlDocUrl;

        @JsonProperty("PdfDocUrl")
        private String PdfDocUrl;

        @JsonProperty("CoverImageUrl")
        private String CoverImageUrl;
    }

    // ======================
    // 导出记录
    // ======================

    @Data
    public static class FrontMedicalExportRecordCreateInput {
        @JsonProperty("DetectId")
        private Integer DetectId;

        @JsonProperty("ReportId")
        private Integer ReportId;

        @JsonProperty("ExportFormat")
        private String ExportFormat;

        @JsonProperty("ExportPurpose")
        private String ExportPurpose;

        @JsonProperty("ExportFileUrl")
        private String ExportFileUrl;
    }

    @Data
    public static class FrontMedicalExportRecordListInput {
        @JsonProperty("Page")
        private Long Page;

        @JsonProperty("Limit")
        private Long Limit;
    }

    @Data
    public static class FrontMedicalExportRecordListItem {
        @JsonProperty("Id")
        private Integer Id;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("ExportTime")
        private LocalDateTime ExportTime;

        @JsonProperty("ExportFormat")
        private String ExportFormat;

        @JsonProperty("ExportPurpose")
        private String ExportPurpose;

        @JsonProperty("ExportPurposeText")
        private String ExportPurposeText;

        @JsonProperty("ExportFileUrl")
        private String ExportFileUrl;
    }
}

