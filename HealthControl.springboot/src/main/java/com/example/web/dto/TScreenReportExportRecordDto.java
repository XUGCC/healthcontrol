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
 * 自查报告导出记录类
 */
@Data
public class TScreenReportExportRecordDto extends BaseDto
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
     * 报告导出时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("ExportTime")
    private LocalDateTime ExportTime;             
    
     
    /**
     * 导出格式
     */ 
    @JsonProperty("ExportFormat")
    private Boolean ExportFormat;          
    
     
    /**
     * 导出文件URL
     */ 
    @JsonProperty("ExportFileUrl")
    private String ExportFileUrl;
    
     
    /**
     * 导出用途
     */ 
    @JsonProperty("ExportPurpose")
    private Boolean ExportPurpose;          
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("DetectDto") 
    private TAudioScreenRecordDto DetectDto;                        
   
     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
 	 /**
     * 把自查报告导出记录传输模型转换成自查报告导出记录实体
     */
    public TScreenReportExportRecord MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TScreenReportExportRecord TScreenReportExportRecord= new TScreenReportExportRecord();
     
         BeanUtils.copyProperties(TScreenReportExportRecord,this);
        
        return TScreenReportExportRecord;
    }

}
