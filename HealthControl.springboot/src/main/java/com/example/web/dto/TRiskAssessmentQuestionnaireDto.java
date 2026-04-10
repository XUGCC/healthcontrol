package com.example.web.dto;
import com.example.web.enums.*;
import com.example.web.tools.dto.BaseDto;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.sql.Date;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.example.web.entity.*;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
/**
 * 风险评估问卷类
 */
@Data
public class TRiskAssessmentQuestionnaireDto extends BaseDto
{

    
     
    /**
     * 更新时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("UpdateTime")
    private LocalDateTime UpdateTime;             
    
     
    /**
     * 创建人ID
     */ 
    @JsonProperty("CreatorId")
    private Integer CreatorId;          
    
     
    /**
     * 问卷标题
     */ 
    @JsonProperty("QuestionnaireTitle")
    private String QuestionnaireTitle;
    
     
    /**
     * 问卷描述
     */ 
    @JsonProperty("QuestionnaireDesc")
    private String QuestionnaireDesc;
    
     
    /**
     * 问题数量
     */ 
    @JsonProperty("QuestionCount")
    private Integer QuestionCount;          
    
     
    /**
     * 展示状态
     */ 
    @JsonProperty("ShowStatus")
    private Integer ShowStatus;          
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Integer IsDelete;          

     @JsonProperty("CreatorDto") 
    private AppUserDto CreatorDto;                        
   
 	 /**
     * 把风险评估问卷传输模型转换成风险评估问卷实体
     */
    public TRiskAssessmentQuestionnaire MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TRiskAssessmentQuestionnaire TRiskAssessmentQuestionnaire= new TRiskAssessmentQuestionnaire();
     
         BeanUtils.copyProperties(TRiskAssessmentQuestionnaire,this);
        
        return TRiskAssessmentQuestionnaire;
    }

}
