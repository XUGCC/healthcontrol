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
 * 个性化健康提醒类
 */
@Data
public class TPersonalHealthRemindDto extends BaseDto
{

    
     
    /**
     * 推送时间更新
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
     * 提醒类型：0=定期自查，1=护喉饮食，2=发音训练，3=饮水，4=随访
     */ 
    @JsonProperty("RemindType")
    private Integer RemindType;          
    
     
    /**
     * 提醒时间：如08:00
     */ 
    @JsonProperty("RemindTime")
    private String RemindTime;
    
     
    /**
     * 重复频率：0=每天，1=每周一至五，2=每周一次，3=每30天一次
     */ 
    @JsonProperty("RepeatFrequency")
    private Integer RepeatFrequency;          
    
     
    /**
     * 提醒状态：0=关闭，1=开启
     */ 
    @JsonProperty("RemindStatus")
    private Integer RemindStatus;          
    
     
    /**
     * 最后推送时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("LastPushTime")
    private LocalDateTime LastPushTime;             
    
     
    /**
     * 提醒内容
     */ 
    @JsonProperty("RemindContent")
    private String RemindContent;
    
     
    /**
     * 软删除标记：0=未删除，1=已删除
     */ 
    @JsonProperty("IsDelete")
    private Integer IsDelete;          

     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
 	 /**
     * 把个性化健康提醒传输模型转换成个性化健康提醒实体
     */
    public TPersonalHealthRemind MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TPersonalHealthRemind TPersonalHealthRemind= new TPersonalHealthRemind();
     
         BeanUtils.copyProperties(TPersonalHealthRemind,this);
        
        return TPersonalHealthRemind;
    }

}
