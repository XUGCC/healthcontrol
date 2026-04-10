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
 * 模型优化标注表
 */
@Data
@TableName("`TModelOptimizeLabel`")
public class TModelOptimizeLabel extends BaseEntity {

      
    /**
     * 授权状态更新时间
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
     * 关联音频自查记录表主键
     */  
    @JsonProperty("DetectId")
    @TableField(value="DetectId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer DetectId;          
      
  	  /**
     * 医院确诊结果
     */  
    @JsonProperty("HospitalDiagnoseResult")
    @TableField(value="HospitalDiagnoseResult",updateStrategy = FieldStrategy.ALWAYS)
    private String HospitalDiagnoseResult;
      
    /**
     * 标注类型
     */  
    @JsonProperty("LabelType")
    @TableField(value="LabelType",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean LabelType;          
      
  	  /**
     * 标注说明
     */  
    @JsonProperty("LabelDesc")
    @TableField(value="LabelDesc",updateStrategy = FieldStrategy.ALWAYS)
    private String LabelDesc;
      
    /**
     * 授权状态
     */  
    @JsonProperty("AuthStatus")
    @TableField(value="AuthStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean AuthStatus;          
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把模型优化标注实体转换成模型优化标注传输模型
     */
    public TModelOptimizeLabelDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TModelOptimizeLabelDto TModelOptimizeLabelDto = new TModelOptimizeLabelDto();
       
        // 注意：BeanUtils.copyProperties的第一个参数是目标对象，第二个参数是源对象
        // 将 this (Entity) 的属性复制到 TModelOptimizeLabelDto (DTO)
        BeanUtils.copyProperties(TModelOptimizeLabelDto, this);
       
        return TModelOptimizeLabelDto;
    }

}
