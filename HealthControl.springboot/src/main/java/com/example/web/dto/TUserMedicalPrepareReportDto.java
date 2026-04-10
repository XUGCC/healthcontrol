package com.example.web.dto;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.example.web.entity.TUserMedicalPrepareReport;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * 用户就诊准备报告类
 */
@Data
public class TUserMedicalPrepareReportDto extends BaseDto
{

    @JsonProperty("PrimaryDetectId")
    private Integer PrimaryDetectId;

    @JsonProperty("UpdateTime")
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;
     
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
     * 关联检测
     */ 
    @JsonProperty("DetectIdList")
    private String DetectIdList;
    
     
    /**
     * 文档模板类型：0=简版，1=详版
     */ 
    @JsonProperty("TemplateType")
    private Boolean TemplateType;          
    
     
    /**
     * 文档内容范围：如筛查记录+症状日志+饮食记录
     */ 
    @JsonProperty("ContentScope")
    private String ContentScope;
    
     
    @JsonProperty("ReportTitle")
    private String ReportTitle;

    @JsonProperty("ReportNo")
    private String ReportNo;

    @JsonProperty("StandardDocType")
    private String StandardDocType;

    @JsonProperty("ReportContentJson")
    private String ReportContentJson;

    @JsonProperty("StandardDocUrl")
    private String StandardDocUrl;

    @JsonProperty("HtmlDocUrl")
    private String HtmlDocUrl;

    @JsonProperty("PdfDocUrl")
    private String PdfDocUrl;

    @JsonProperty("CoverImageUrl")
    private String CoverImageUrl;

    /**
     * 生成状态：0=待生成，1=生成中，2=已完成，3=生成失败（当前底层为 Boolean，前台通过映射使用）
     */ 
    @JsonProperty("GenerateStatus")
    private Boolean GenerateStatus;          
    
     
    /**
     * 生成失败原因
     */ 
    @JsonProperty("GenerateFailReason")
    private String GenerateFailReason;
    
     
    /**
     * 累计查看次数
     */ 
    @JsonProperty("ViewCount")
    private Integer ViewCount;          
    
     
    /**
     * 累计下载次数
     */ 
    @JsonProperty("DownloadCount")
    private Integer DownloadCount;          
    
     
    /**
     * 累计分享次数
     */ 
    @JsonProperty("ShareCount")
    private Integer ShareCount;          
    
     
    /**
     * 软删除标记：0=未删除，1=已删除
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("HealthRecordDto") 
    private TPersonalLaryngealHealthRecordDto HealthRecordDto;                        
   
     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
 	 /**
     * 把用户就诊准备报告传输模型转换成用户就诊准备报告实体
     */
    public TUserMedicalPrepareReport MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TUserMedicalPrepareReport TUserMedicalPrepareReport= new TUserMedicalPrepareReport();
     
         BeanUtils.copyProperties(TUserMedicalPrepareReport,this);
        
        return TUserMedicalPrepareReport;
    }

}
