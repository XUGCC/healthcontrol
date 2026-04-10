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
 * 风险评估问卷表
 */
@Data
@TableName("`TRiskAssessmentQuestionnaire`")
public class TRiskAssessmentQuestionnaire extends BaseEntity {

      
    /**
     * 更新时间
     */  
    @JsonProperty("UpdateTime")
    @TableField(value="UpdateTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;             
      
    /**
     * 创建人ID
     */  
    @JsonProperty("CreatorId")
    @TableField(value="CreatorId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer CreatorId;          
      
  	  /**
     * 问卷标题
     */  
    @JsonProperty("QuestionnaireTitle")
    @TableField(value="QuestionnaireTitle",updateStrategy = FieldStrategy.ALWAYS)
    private String QuestionnaireTitle;
      
  	  /**
     * 问卷描述
     */  
    @JsonProperty("QuestionnaireDesc")
    @TableField(value="QuestionnaireDesc",updateStrategy = FieldStrategy.ALWAYS)
    private String QuestionnaireDesc;
      
    /**
     * 问题数量
     */  
    @JsonProperty("QuestionCount")
    @TableField(value="QuestionCount",updateStrategy = FieldStrategy.ALWAYS)
    private Integer QuestionCount;          
      
    /**
     * 展示状态
     */  
    @JsonProperty("ShowStatus")
    @TableField(value="ShowStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ShowStatus;          
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsDelete;          
  
    /**
     * 把风险评估问卷实体转换成风险评估问卷传输模型
     */
    public TRiskAssessmentQuestionnaireDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TRiskAssessmentQuestionnaireDto TRiskAssessmentQuestionnaireDto = new TRiskAssessmentQuestionnaireDto();
       
        BeanUtils.copyProperties(TRiskAssessmentQuestionnaireDto,this);
       
        return TRiskAssessmentQuestionnaireDto;
    }

}
