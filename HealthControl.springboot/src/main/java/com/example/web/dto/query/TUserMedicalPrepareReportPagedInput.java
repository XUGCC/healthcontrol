package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * 用户就诊准备报告查询模型
 */
@NoArgsConstructor
@Data
public class TUserMedicalPrepareReportPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 关联检测模糊查询条件
     */
  	 @JsonProperty("DetectIdList")
    private String DetectIdList;
    /**
     * 文档内容范围：如筛查记录+症状日志+饮食记录模糊查询条件
     */
  	 @JsonProperty("ContentScope")
    private String ContentScope;
    /**
     * 生成失败原因模糊查询条件
     */
  	 @JsonProperty("GenerateFailReason")
    private String GenerateFailReason;
     /**
     * 关联用户表主键t_user.
     */
  	 @JsonProperty("UserId")
    private Integer UserId;
     /**
     * 关联健康档案表主键t_personal_laryngeal_health_record.
     */
  	 @JsonProperty("HealthRecordId")
    private Integer HealthRecordId;
     /**
     * 文档模板类型：0=简版，1=详版
     */
  	 @JsonProperty("TemplateType")
    private Boolean TemplateType;
     /**
     * 生成状态：0=待生成，1=生成中，2=已完成，3=生成失败
     */
  	 @JsonProperty("GenerateStatus")
    private Boolean GenerateStatus;
     /**
     * 软删除标记：0=未删除，1=已删除
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
