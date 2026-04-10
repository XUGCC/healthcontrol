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
 * 喉镜照片记录类
 */
@Data
public class TLaryngoscopePhotoDto extends BaseDto
{

    
     
    /**
     * 审核更新时间
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
     * 关联音频自查记录表主键t
     */ 
    @JsonProperty("DetectId")
    private Integer DetectId;          
    
     
    /**
     * 喉镜照片URL
     */ 
    @JsonProperty("LaryngoscopePhotoUrl")
    private String LaryngoscopePhotoUrl;
    
     
    /**
     * 照片描述
     */ 
    @JsonProperty("PhotoDesc")
    private String PhotoDesc;
    
     
    /**
     * 照片上传时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("UploadTime")
    private LocalDateTime UploadTime;             
    
    /**
     * 实际检查时间（区别于上传时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("CheckTime")
    private LocalDateTime CheckTime;

    /**
     * 检查医院名称
     */
    @JsonProperty("HospitalName")
    private String HospitalName;

    /**
     * 检查方式，例如 电子喉镜/纤维喉镜
     */
    @JsonProperty("CheckType")
    private String CheckType;

    /**
     * 视角/体位，例如 正视/左侧位/右侧位
     */
    @JsonProperty("ViewPosition")
    private String ViewPosition;

    /**
     * 病变侧别：左侧/右侧/双侧/不明确
     */
    @JsonProperty("LesionSide")
    private String LesionSide;
    
     
    /**
     * 审核状态
     */ 
    @JsonProperty("AuditStatus")
    private Boolean AuditStatus;          
    
     
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
     * 把喉镜照片记录传输模型转换成喉镜照片记录实体
     */
    public TLaryngoscopePhoto MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TLaryngoscopePhoto TLaryngoscopePhoto= new TLaryngoscopePhoto();
     
         BeanUtils.copyProperties(TLaryngoscopePhoto,this);
        
        return TLaryngoscopePhoto;
    }

}
