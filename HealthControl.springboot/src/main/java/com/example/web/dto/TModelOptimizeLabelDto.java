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
 * 模型优化标注类
 */
@Data
public class TModelOptimizeLabelDto extends BaseDto
{

    
     
    /**
     * 授权状态更新时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("UpdateTime")
    private LocalDateTime UpdateTime;             
    
     
    /**
     * 关联用户表主键t_user.
     */ 
    @JsonProperty("UserId")
    private Integer UserId;          
    
     
    /**
     * 关联音频自查记录表主键
     */ 
    @JsonProperty("DetectId")
    private Integer DetectId;          
    
     
    /**
     * 医院确诊结果
     */ 
    @JsonProperty("HospitalDiagnoseResult")
    private String HospitalDiagnoseResult;
    
     
    /**
     * 标注类型
     */ 
    @JsonProperty("LabelType")
    private Boolean LabelType;          
    
     
    /**
     * 标注说明
     */ 
    @JsonProperty("LabelDesc")
    private String LabelDesc;
    
     
    /**
     * 授权状态
     */ 
    @JsonProperty("AuthStatus")
    private Boolean AuthStatus;          
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
     @JsonProperty("DetectDto") 
    private TAudioScreenRecordDto DetectDto;                        
   
 	 /**
     * 把模型优化标注传输模型转换成模型优化标注实体
     */
    public TModelOptimizeLabel MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TModelOptimizeLabel TModelOptimizeLabel= new TModelOptimizeLabel();
     
         // 注意：BeanUtils.copyProperties的第一个参数是目标对象，第二个参数是源对象
         // 将 this (DTO) 的属性复制到 TModelOptimizeLabel (Entity)
         BeanUtils.copyProperties(TModelOptimizeLabel, this);
        
        return TModelOptimizeLabel;
    }

}
