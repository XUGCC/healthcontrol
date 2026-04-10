package com.example.web.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.example.web.dto.TUserMedicalPrepareReportDto;
/**
 * 用户就诊准备报告表
 */
@Data
@TableName("`TUserMedicalPrepareReport`")
public class TUserMedicalPrepareReport extends BaseEntity {

    /**
     * 主关联检测 Id（用于导出、聚合）
     */
    @JsonProperty("PrimaryDetectId")
    @TableField(value="PrimaryDetectId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer PrimaryDetectId;

    /**
     * 更新时间/生成完成时间
     */
    @JsonProperty("UpdateTime")
    @TableField(value="UpdateTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;

    /**
     * 关联用户表主键t_user.
     */  
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;          
      
    /**
     * 关联健康档案表主键t_personal_laryngeal_health_record.
     */  
    @JsonProperty("HealthRecordId")
    @TableField(value="HealthRecordId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer HealthRecordId;          
      
  	  /**
     * 关联检测
     */  
    @JsonProperty("DetectIdList")
    @TableField(value="DetectIdList",updateStrategy = FieldStrategy.ALWAYS)
    private String DetectIdList;
      
    /**
     * 文档模板类型：0=简版，1=详版
     */
    @JsonProperty("TemplateType")
    @TableField(value="TemplateType",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean TemplateType;          
      
  	  /**
     * 文档内容范围：如筛查记录+症状日志+饮食记录
     */  
    @JsonProperty("ContentScope")
    @TableField(value="ContentScope",updateStrategy = FieldStrategy.ALWAYS)
    private String ContentScope;
      
    /**
     * 报告标题
     */
    @JsonProperty("ReportTitle")
    @TableField(value="ReportTitle",updateStrategy = FieldStrategy.ALWAYS)
    private String ReportTitle;

    /**
     * 报告编号
     */
    @JsonProperty("ReportNo")
    @TableField(value="ReportNo",updateStrategy = FieldStrategy.ALWAYS)
    private String ReportNo;

    /**
     * 标准化文档类型：HTML/PDF/IMAGE
     */
    @JsonProperty("StandardDocType")
    @TableField(value="StandardDocType",updateStrategy = FieldStrategy.ALWAYS)
    private String StandardDocType;

    /**
     * 报告内容快照 JSON
     */
    @JsonProperty("ReportContentJson")
    @TableField(value="ReportContentJson",updateStrategy = FieldStrategy.ALWAYS)
    private String ReportContentJson;

    /**
     * 标准化文档URL（通常为 HTML）
     */
    @JsonProperty("StandardDocUrl")
    @TableField(value="StandardDocUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String StandardDocUrl;

    /**
     * HTML 文档 URL
     */
    @JsonProperty("HtmlDocUrl")
    @TableField(value="HtmlDocUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String HtmlDocUrl;

    /**
     * PDF 文档 URL
     */
    @JsonProperty("PdfDocUrl")
    @TableField(value="PdfDocUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String PdfDocUrl;

    /**
     * 报告封面图 URL（可选）
     */
    @JsonProperty("CoverImageUrl")
    @TableField(value="CoverImageUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String CoverImageUrl;

    /**
     * 生成状态：0=待生成，1=生成中，2=已完成，3=生成失败
     * 目前数据库仍为 Boolean，为兼容保留原字段
     */
    @JsonProperty("GenerateStatus")
    @TableField(value="GenerateStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean GenerateStatus;          
      
  	  /**
     * 生成失败原因
     */  
    @JsonProperty("GenerateFailReason")
    @TableField(value="GenerateFailReason",updateStrategy = FieldStrategy.ALWAYS)
    private String GenerateFailReason;
      
    /**
     * 累计查看次数
     */  
    @JsonProperty("ViewCount")
    @TableField(value="ViewCount",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ViewCount;          
      
    /**
     * 累计下载次数
     */  
    @JsonProperty("DownloadCount")
    @TableField(value="DownloadCount",updateStrategy = FieldStrategy.ALWAYS)
    private Integer DownloadCount;          
      
    /**
     * 累计分享次数
     */  
    @JsonProperty("ShareCount")
    @TableField(value="ShareCount",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ShareCount;          
      
    /**
     * 软删除标记：0=未删除，1=已删除
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把用户就诊准备报告实体转换成用户就诊准备报告传输模型
     */
    public TUserMedicalPrepareReportDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TUserMedicalPrepareReportDto TUserMedicalPrepareReportDto = new TUserMedicalPrepareReportDto();
       
        BeanUtils.copyProperties(TUserMedicalPrepareReportDto,this);
       
        return TUserMedicalPrepareReportDto;
    }

}
