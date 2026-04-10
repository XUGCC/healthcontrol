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
 * 用户饮食记录类
 */
@Data
public class TUserDietRecordDto extends BaseDto
{

    
     
    /**
     * 关联用户表主键t_user.
     */ 
    @JsonProperty("UserId")
    private Integer UserId;          
    
     
    /**
     * 关联食物库表主键
     */ 
    @JsonProperty("FoodId")
    private Integer FoodId;          
    
     
    /**
     * 饮食摄入时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("IntakeTime")
    private LocalDateTime IntakeTime;             
    
     
    /**
     * 当日摄入频次
     */ 
    @JsonProperty("IntakeFrequency")
    private Integer IntakeFrequency;          
    
     
    /**
     * 食用感受
     */ 
    @JsonProperty("EatFeeling")
    private String EatFeeling;
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
 	 /**
     * 把用户饮食记录传输模型转换成用户饮食记录实体
     */
    public TUserDietRecord MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TUserDietRecord TUserDietRecord= new TUserDietRecord();
     
         BeanUtils.copyProperties(TUserDietRecord,this);
        
        return TUserDietRecord;
    }

}
