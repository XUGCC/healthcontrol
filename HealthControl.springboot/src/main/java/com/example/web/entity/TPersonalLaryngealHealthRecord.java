package com.example.web.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.poi.hpsf.Decimal;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.sql.Date;
import java.sql.Timestamp;
import lombok.Data;
import java.time.LocalDateTime;
import com.example.web.dto.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
/**
 * 个人喉部健康档案表
 */
@Data
@TableName("`TPersonalLaryngealHealthRecord`")
public class TPersonalLaryngealHealthRecord extends BaseEntity {

      
    /**
     * 关联用户表主键t_user.
     */  
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;          
      
  	  /**
     * 近期主要不适症状
     */  
    @JsonProperty("RecentSymptom")
    @TableField(value="RecentSymptom",updateStrategy = FieldStrategy.ALWAYS)
    private String RecentSymptom;
      
  	  /**
     * 症状持续时间：如1周/1月
     */  
    @JsonProperty("SymptomDuration")
    @TableField(value="SymptomDuration",updateStrategy = FieldStrategy.ALWAYS)
    private String SymptomDuration;
      
  	  /**
     * 日常护嗓习惯
     */  
    @JsonProperty("DailyVoiceCare")
    @TableField(value="DailyVoiceCare",updateStrategy = FieldStrategy.ALWAYS)
    private String DailyVoiceCare;
      
    /**
     * 末次自查时间
     */  
    @JsonProperty("LastScreenTime")
    @TableField(value="LastScreenTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime LastScreenTime;             
      
  	  /**
     * 健康趋势标签：如好转/恶化/稳定
     */  
    @JsonProperty("HealthTrendTag")
    @TableField(value="HealthTrendTag",updateStrategy = FieldStrategy.ALWAYS)
    private String HealthTrendTag;
      
    /**
     * 关联最新检测
     */  
    @JsonProperty("LatestDetectId")
    @TableField(value="LatestDetectId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer LatestDetectId;          
      
    /**
     * 风险评估等级：0=低，1=中，2=高
     */  
    @JsonProperty("RiskAssessmentLevel")
    @TableField(value="RiskAssessmentLevel",updateStrategy = FieldStrategy.ALWAYS)
    private Integer RiskAssessmentLevel;          
      
    /**
     * 软删除标记：0=未删除，1=已删除
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把个人喉部健康档案实体转换成个人喉部健康档案传输模型
     */
    public TPersonalLaryngealHealthRecordDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TPersonalLaryngealHealthRecordDto TPersonalLaryngealHealthRecordDto = new TPersonalLaryngealHealthRecordDto();
       
        BeanUtils.copyProperties(TPersonalLaryngealHealthRecordDto,this);
       
        return TPersonalLaryngealHealthRecordDto;
    }

}
