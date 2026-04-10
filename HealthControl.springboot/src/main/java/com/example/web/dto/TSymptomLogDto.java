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
 * 症状日志类
 */
@Data
public class TSymptomLogDto extends BaseDto
{

    
     
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
     * 症状类型：如嘶哑、疼痛、吞咽困难等
     */ 
    @JsonProperty("SymptomType")
    private String SymptomType;          
    
     
    /**
     * 症状轻重：1=轻度，2=中度，3=重度
     */ 
    @JsonProperty("SymptomLevel")
    private Integer SymptomLevel;          
    
     
    /**
     * 症状持续时长
     */ 
    @JsonProperty("SymptomDuration")
    private String SymptomDuration;
    
     
    /**
     * 症状首次出现时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("SymptomOccurTime")
    private LocalDateTime SymptomOccurTime;             
    
     
    /**
     * 症状备注
     */ 
    @JsonProperty("Remark")
    private String Remark;
    
     
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
     * 把症状日志传输模型转换成症状日志实体
     */
    public TSymptomLog MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TSymptomLog TSymptomLog= new TSymptomLog();
     
         BeanUtils.copyProperties(TSymptomLog,this);
        
        return TSymptomLog;
    }

}
