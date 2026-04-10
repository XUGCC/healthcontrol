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
 * 科普收藏类
 */
@Data
public class TScienceCollectDto extends BaseDto
{

    
     
    /**
     * 取消收藏时间
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
     * 关联科普表主键
     */ 
    @JsonProperty("ScienceId")
    private Integer ScienceId;          
    
     
    /**
     * 收藏操作时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("CollectTime")
    private LocalDateTime CollectTime;             
    
     
    /**
     * 取消收藏操作时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("CancelCollectTime")
    private LocalDateTime CancelCollectTime;             
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
     @JsonProperty("ScienceDto") 
    private THealthScienceDto ScienceDto;                        
   
 	 /**
     * 把科普收藏传输模型转换成科普收藏实体
     */
    public TScienceCollect MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TScienceCollect TScienceCollect= new TScienceCollect();
     
         BeanUtils.copyProperties(TScienceCollect,this);
        
        return TScienceCollect;
    }

}
