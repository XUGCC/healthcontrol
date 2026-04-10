package com.example.web.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.example.web.dto.TUserQuestionnaireAnswerDto;
/**
 * 用户问卷答题表
 */
@Data
@TableName("`TUserQuestionnaireAnswer`")
public class TUserQuestionnaireAnswer extends BaseEntity {

      
    /**
     * 关联用户表主键t_user.
     */  
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;          
      
    /**
     * 关联问卷表主键
     */  
    @JsonProperty("QuestionnaireId")
    @TableField(value="QuestionnaireId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer QuestionnaireId;          
      
    /**
     * 答题提交时间
     */  
    @JsonProperty("AnswerTime")
    @TableField(value="AnswerTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime AnswerTime;             
      
    /**
     * 风险评估得分
     */  
    @JsonProperty("RiskAssessmentScore")
    @TableField(value="RiskAssessmentScore",updateStrategy = FieldStrategy.ALWAYS)
    private Double RiskAssessmentScore;      

    /**
     * 用户答案 JSON（可配置问卷：题目Id -> 选项/文本）
     */
    @JsonProperty("AnswerJson")
    @TableField(value = "AnswerJson", updateStrategy = FieldStrategy.ALWAYS)
    private String AnswerJson;

    /**
     * 计分明细 JSON
     */
    @JsonProperty("ScoreDetailJson")
    @TableField(value = "ScoreDetailJson", updateStrategy = FieldStrategy.ALWAYS)
    private String ScoreDetailJson;

    /**
     * 风险等级：0=低，1=中，2=高
     */
    @JsonProperty("RiskLevel")
    @TableField(value = "RiskLevel", updateStrategy = FieldStrategy.ALWAYS)
    private Integer RiskLevel;

    /**
     * 结果摘要
     */
    @JsonProperty("ResultSummary")
    @TableField(value = "ResultSummary", updateStrategy = FieldStrategy.ALWAYS)
    private String ResultSummary;

    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把用户问卷答题实体转换成用户问卷答题传输模型
     */
    public TUserQuestionnaireAnswerDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TUserQuestionnaireAnswerDto dto = new TUserQuestionnaireAnswerDto();
        org.springframework.beans.BeanUtils.copyProperties(this, dto);
        return dto;
    }

}
