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
 * 用户问卷答题类
 */
@Data
public class TUserQuestionnaireAnswerDto extends BaseDto
{

    
     
    /**
     * 关联用户表主键t_user.
     */ 
    @JsonProperty("UserId")
    private Integer UserId;          
    
     
    /**
     * 关联问卷表主键
     */ 
    @JsonProperty("QuestionnaireId")
    private Integer QuestionnaireId;          
    
     
    /**
     * 答题提交时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("AnswerTime")
    private LocalDateTime AnswerTime;             
    
     
    /**
     * 吸烟史
     */ 
    @JsonProperty("SmokingHistory")
    private Boolean SmokingHistory;          
    
     
    /**
     * 饮酒史
     */ 
    @JsonProperty("DrinkingHistory")
    private Boolean DrinkingHistory;          
    
     
    /**
     * 职业用嗓
     */ 
    @JsonProperty("OccupationalVoice")
    private Boolean OccupationalVoice;          
    
     
    /**
     * 反酸症状
     */ 
    @JsonProperty("AcidReflexSymptom")
    private Boolean AcidReflexSymptom;          
    
     
    /**
     * 其他风险因素
     */ 
    @JsonProperty("OtherRiskFactor")
    private String OtherRiskFactor;
    
     
    /**
     * 风险评估得分
     */ 
    @JsonProperty("RiskAssessmentScore")
    private Double RiskAssessmentScore;      
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
 	 /**
     * 把用户问卷答题传输模型转换成用户问卷答题实体
     */
    public TUserQuestionnaireAnswer MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TUserQuestionnaireAnswer TUserQuestionnaireAnswer= new TUserQuestionnaireAnswer();
     
         BeanUtils.copyProperties(TUserQuestionnaireAnswer,this);
        
        return TUserQuestionnaireAnswer;
    }

}
