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
 * 模型接口调用日志类
 */
@Data
public class TModelInterfaceCallLogDto extends BaseDto
{

    
     
    /**
     * 结果更新时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("UpdateTime")
    private LocalDateTime UpdateTime;             
    
     
    /**
     * 关联音频自查记录表主键
     */ 
    @JsonProperty("DetectId")
    private Integer DetectId;          
    
     
    /**
     * 调用环节
     */ 
    @JsonProperty("CallLink")
    private Boolean CallLink;          
    
     
    /**
     * 模型接口类型
     */ 
    @JsonProperty("ModelInterfaceType")
    private Boolean ModelInterfaceType;          

    /**
     * 模型版本号，例如 v1.0.3
     */
    @JsonProperty("ModelVersion")
    private String ModelVersion;

    /**
     * 内部服务名称/标识，例如 audio_classifier 或 preprocessor_v2
     */
    @JsonProperty("ServiceName")
    private String ServiceName;
     
    /**
     * 输入图谱URL
     */ 
    @JsonProperty("InputSpectrumUrl")
    private String InputSpectrumUrl;
    
     
    /**
     * 调用参数
     */ 
    @JsonProperty("InputParams")
    private String InputParams;
    
     
    /**
     * 调用耗时
     */ 
    @JsonProperty("CallCost")
    private Integer CallCost;          

    /**
     * 服务质量等级：0=正常,1=轻微异常,2=严重异常
     */
    @JsonProperty("QualityLevel")
    private Integer QualityLevel;
     
    /**
     * 返回结果摘要
     */ 
    @JsonProperty("ResultSummary")
    private String ResultSummary;
    
     
    /**
     * 返回完整结果
     */ 
    @JsonProperty("ResultComplete")
    private String ResultComplete;
    
     
    /**
     * 调用状态：0=失败，1=成功
     */ 
    @JsonProperty("CallStatus")
    private Boolean CallStatus;          
    
     
    /**
     * 失败错误码
     */ 
    @JsonProperty("FailErrorCode")
    private String FailErrorCode;
    
     
    /**
     * 失败详细信息
     */ 
    @JsonProperty("FailDetailInfo")
    private String FailDetailInfo;
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("DetectDto") 
    private TAudioScreenRecordDto DetectDto;                        
   
 	 /**
     * 把模型接口调用日志传输模型转换成模型接口调用日志实体
     */
    public TModelInterfaceCallLog MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TModelInterfaceCallLog TModelInterfaceCallLog= new TModelInterfaceCallLog();
     
         BeanUtils.copyProperties(TModelInterfaceCallLog,this);
        
        return TModelInterfaceCallLog;
    }

}
