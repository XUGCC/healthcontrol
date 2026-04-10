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
 * 模型接口调用日志表
 */
@Data
@TableName("`TModelInterfaceCallLog`")
public class TModelInterfaceCallLog extends BaseEntity {

      
    /**
     * 结果更新时间
     */  
    @JsonProperty("UpdateTime")
    @TableField(value="UpdateTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;             
      
    /**
     * 关联音频自查记录表主键
     */  
    @JsonProperty("DetectId")
    @TableField(value="DetectId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer DetectId;          
      
    /**
     * 调用环节
     */  
    @JsonProperty("CallLink")
    @TableField(value="CallLink",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean CallLink;          
      
    /**
     * 模型接口类型
     */  
    @JsonProperty("ModelInterfaceType")
    @TableField(value="ModelInterfaceType",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean ModelInterfaceType;          
      
    /**
     * 模型版本号，例如 v1.0.3
     */
    @JsonProperty("ModelVersion")
    @TableField(value="ModelVersion",updateStrategy = FieldStrategy.ALWAYS)
    private String ModelVersion;

    /**
     * 内部服务名称/标识，例如 audio_classifier 或 preprocessor_v2
     */
    @JsonProperty("ServiceName")
    @TableField(value="ServiceName",updateStrategy = FieldStrategy.ALWAYS)
    private String ServiceName;

  	  /**
     * 输入图谱URL
     */  
    @JsonProperty("InputSpectrumUrl")
    @TableField(value="InputSpectrumUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String InputSpectrumUrl;
      
  	  /**
     * 调用参数
     */  
    @JsonProperty("InputParams")
    @TableField(value="InputParams",updateStrategy = FieldStrategy.ALWAYS)
    private String InputParams;
      
    /**
     * 调用耗时
     */  
    @JsonProperty("CallCost")
    @TableField(value="CallCost",updateStrategy = FieldStrategy.ALWAYS)
    private Integer CallCost;          

    /**
     * 服务质量等级：0=正常,1=轻微异常,2=严重异常
     */
    @JsonProperty("QualityLevel")
    @TableField(value="QualityLevel",updateStrategy = FieldStrategy.ALWAYS)
    private Integer QualityLevel;
      
  	  /**
     * 返回结果摘要
     */  
    @JsonProperty("ResultSummary")
    @TableField(value="ResultSummary",updateStrategy = FieldStrategy.ALWAYS)
    private String ResultSummary;
      
  	  /**
     * 返回完整结果
     */  
    @JsonProperty("ResultComplete")
    @TableField(value="ResultComplete",updateStrategy = FieldStrategy.ALWAYS)
    private String ResultComplete;
      
    /**
     * 调用状态：0=失败，1=成功
     */  
    @JsonProperty("CallStatus")
    @TableField(value="CallStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean CallStatus;          
      
  	  /**
     * 失败错误码
     */  
    @JsonProperty("FailErrorCode")
    @TableField(value="FailErrorCode",updateStrategy = FieldStrategy.ALWAYS)
    private String FailErrorCode;
      
  	  /**
     * 失败详细信息
     */  
    @JsonProperty("FailDetailInfo")
    @TableField(value="FailDetailInfo",updateStrategy = FieldStrategy.ALWAYS)
    private String FailDetailInfo;
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把模型接口调用日志实体转换成模型接口调用日志传输模型
     */
    public TModelInterfaceCallLogDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TModelInterfaceCallLogDto TModelInterfaceCallLogDto = new TModelInterfaceCallLogDto();
       
        BeanUtils.copyProperties(TModelInterfaceCallLogDto,this);
       
        return TModelInterfaceCallLogDto;
    }

}
