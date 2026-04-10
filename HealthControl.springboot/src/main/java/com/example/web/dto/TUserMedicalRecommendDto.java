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
 * 用户就医推荐记录类
 */
@Data
public class TUserMedicalRecommendDto extends BaseDto
{

    
     
    /**
     * 查看状态更新时间
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
     * 推荐依据风险等级
     */ 
    @JsonProperty("RiskLevel")
    private Boolean RiskLevel;          
    
     
    /**
     * 关联医院医生表主键
     */ 
    @JsonProperty("RecommendHospitalId")
    private Integer RecommendHospitalId;          
    
     
    /**
     * 系统推荐时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("RecommendTime")
    private LocalDateTime RecommendTime;             
    
     
    /**
     * 查看状态
     */ 
    @JsonProperty("ViewStatus")
    private Boolean ViewStatus;          
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("DetectDto") 
    private TAudioScreenRecordDto DetectDto;                        
   
     @JsonProperty("RecommendHospitalDto") 
    private TOtolaryngologyHospitalDoctorDto RecommendHospitalDto;                        
   
     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
 	 /**
     * 把用户就医推荐记录传输模型转换成用户就医推荐记录实体
     */
    public TUserMedicalRecommend MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TUserMedicalRecommend TUserMedicalRecommend= new TUserMedicalRecommend();
     
         BeanUtils.copyProperties(TUserMedicalRecommend,this);
        
        return TUserMedicalRecommend;
    }

}
