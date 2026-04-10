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
 * 个人喉部健康档案类
 */
@Data
public class TPersonalLaryngealHealthRecordDto extends BaseDto
{

    
     
    /**
     * 关联用户表主键t_user.
     */ 
    @JsonProperty("UserId")
    private Integer UserId;          
    
     
    /**
     * 近期主要不适症状
     */ 
    @JsonProperty("RecentSymptom")
    private String RecentSymptom;
    
     
    /**
     * 症状持续时间：如1周/1月
     */ 
    @JsonProperty("SymptomDuration")
    private String SymptomDuration;
    
     
    /**
     * 日常护嗓习惯
     */ 
    @JsonProperty("DailyVoiceCare")
    private String DailyVoiceCare;
    
     
    /**
     * 末次自查时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("LastScreenTime")
    private LocalDateTime LastScreenTime;             
    
     
    /**
     * 健康趋势标签：如好转/恶化/稳定
     */ 
    @JsonProperty("HealthTrendTag")
    private String HealthTrendTag;
    
     
    /**
     * 关联最新检测
     */ 
    @JsonProperty("LatestDetectId")
    private Integer LatestDetectId;          
    
     
    /**
     * 风险评估等级：0=低，1=中，2=高
     */ 
    @JsonProperty("RiskAssessmentLevel")
    private Integer RiskAssessmentLevel;          
    
     
    /**
     * 软删除标记：0=未删除，1=已删除
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
     @JsonProperty("LatestDetectDto") 
    private TAudioScreenRecordDto LatestDetectDto;                        
   
 	 /**
     * 把个人喉部健康档案传输模型转换成个人喉部健康档案实体
     */
    public TPersonalLaryngealHealthRecord MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TPersonalLaryngealHealthRecord TPersonalLaryngealHealthRecord= new TPersonalLaryngealHealthRecord();
     
         BeanUtils.copyProperties(TPersonalLaryngealHealthRecord,this);
        
        return TPersonalLaryngealHealthRecord;
    }

}
