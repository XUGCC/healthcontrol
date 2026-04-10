package com.example.web.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import com.example.web.dto.*;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import lombok.EqualsAndHashCode;
/**
 * 音频自查记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`TAudioScreenRecord`")
public class TAudioScreenRecord extends BaseEntity {

      
    /**
     * 结果更新时间
     */  
    @JsonProperty("UpdateTime")
    @TableField(value="UpdateTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;             
      
    /**
     * 关联用户表主键t_user.
     */  
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;          
      
  	  /**
     * 音频文件URL
     */  
    @JsonProperty("AudioUrl")
    @TableField(value="AudioUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String AudioUrl;
      
  	  /**
     * 音频格式：mp3wav等
     */  
    @JsonProperty("AudioFormat")
    @TableField(value="AudioFormat",updateStrategy = FieldStrategy.ALWAYS)
    private String AudioFormat;
      
    /**
     * 音频时长
     */  
    @JsonProperty("AudioDuration")
    @TableField(value="AudioDuration",updateStrategy = FieldStrategy.ALWAYS)
    private Integer AudioDuration;          
      
    /**
     * 音频采样率
     */  
    @JsonProperty("AudioSamplingRate")
    @TableField(value="AudioSamplingRate",updateStrategy = FieldStrategy.ALWAYS)
    private Integer AudioSamplingRate;          
      
    /**
     * 音频文件大小
     */  
    @JsonProperty("AudioFileSize")
    @TableField(value="AudioFileSize",updateStrategy = FieldStrategy.ALWAYS)
    private Integer AudioFileSize;          
      
    /**
     * 发音引导类型
     */  
    @JsonProperty("PronunciationGuideType")
    @TableField(value="PronunciationGuideType",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean PronunciationGuideType;          
      
  	  /**
     * 波形图URL
     */  
    @JsonProperty("WaveformUrl")
    @TableField(value="WaveformUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String WaveformUrl;
      
  	  /**
     * MFCC图谱URL
     */  
    @JsonProperty("MfccSpectrumUrl")
    @TableField(value="MfccSpectrumUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String MfccSpectrumUrl;
      
  	  /**
     * MFCC图谱分辨率：如256*256
     */  
    @JsonProperty("MfccSpectrumResolution")
    @TableField(value="MfccSpectrumResolution",updateStrategy = FieldStrategy.ALWAYS)
    private String MfccSpectrumResolution;
      
  	  /**
     * mel图谱URL
     */  
    @JsonProperty("MelSpectrumUrl")
    @TableField(value="MelSpectrumUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String MelSpectrumUrl;
      
  	  /**
     * mel图谱分辨率：如256*256
     */  
    @JsonProperty("MelSpectrumResolution")
    @TableField(value="MelSpectrumResolution",updateStrategy = FieldStrategy.ALWAYS)
    private String MelSpectrumResolution;
      
  	  /**
     * 孪生Densenet模型版本
     */  
    @JsonProperty("DensenetModelVersion")
    @TableField(value="DensenetModelVersion",updateStrategy = FieldStrategy.ALWAYS)
    private String DensenetModelVersion;
      
    /**
     * 初筛结果：0=良性，1=恶性
     */  
    @JsonProperty("PrimaryScreenResult")
    @TableField(value="PrimaryScreenResult",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean PrimaryScreenResult;          
      
    /**
     * 初筛置信度
     */  
    @JsonProperty("PrimaryScreenConfidence")
    @TableField(value="PrimaryScreenConfidence",updateStrategy = FieldStrategy.ALWAYS)
    private Double PrimaryScreenConfidence;      

    /**
     * 语音性别：女性/男性（来源于 Siamese 多任务模型）
     */
    @JsonProperty("VoiceGender")
    @TableField(value="VoiceGender",updateStrategy = FieldStrategy.ALWAYS)
    private String VoiceGender;

    /**
     * 语音性别编码：0=女性，1=男性
     */
    @JsonProperty("VoiceGenderCode")
    @TableField(value="VoiceGenderCode",updateStrategy = FieldStrategy.ALWAYS)
    private Integer VoiceGenderCode;

    /**
     * 语音性别置信度（0–1）
     */
    @JsonProperty("VoiceGenderConfidence")
    @TableField(value="VoiceGenderConfidence",updateStrategy = FieldStrategy.ALWAYS)
    private Double VoiceGenderConfidence;
      
    /**
     * 基频jitter
     */  
    @JsonProperty("Jitter")
    @TableField(value="Jitter",updateStrategy = FieldStrategy.ALWAYS)
    private Double Jitter;      
      
    /**
     * 基频shimmer
     */  
    @JsonProperty("Shimmer")
    @TableField(value="Shimmer",updateStrategy = FieldStrategy.ALWAYS)
    private Double Shimmer;      
      
    /**
     * 谐噪比HNR
     */  
    @JsonProperty("Hnr")
    @TableField(value="Hnr",updateStrategy = FieldStrategy.ALWAYS)
    private Double Hnr;      
      
    /**
     * 最长发声时间MPT
     */  
    @JsonProperty("Mpt")
    @TableField(value="Mpt",updateStrategy = FieldStrategy.ALWAYS)
    private Integer Mpt;          
      
    // 已移除 Deepseek 相关字段（改用 taudio_ai_report 存储 AI 解读报告）
      
    /**
     * 检测总状态
     */  
    @JsonProperty("DetectTotalStatus")
    @TableField(value="DetectTotalStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean DetectTotalStatus;          
      
    /**
     * 检测子状态
     */  
    @JsonProperty("DetectSubStatus")
    @TableField(value="DetectSubStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean DetectSubStatus;          
      
  	  /**
     * 图谱生成失败原因
     */  
    @JsonProperty("SpectrumFailReason")
    @TableField(value="SpectrumFailReason",updateStrategy = FieldStrategy.ALWAYS)
    private String SpectrumFailReason;
      
  	  /**
     * 模型调用失败原因
     */  
    @JsonProperty("ModelFailReason")
    @TableField(value="ModelFailReason",updateStrategy = FieldStrategy.ALWAYS)
    private String ModelFailReason;
      
  	  /**
     * 整体失败原因
     */  
    @JsonProperty("TotalFailReason")
    @TableField(value="TotalFailReason",updateStrategy = FieldStrategy.ALWAYS)
    private String TotalFailReason;
      
    /**
     * 离线状态：0=在线，1=离线
     */  
    @JsonProperty("OfflineStatus")
    @TableField(value="OfflineStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean OfflineStatus;          
      
    /**
     * 关联喉镜照片表主键t_laryngoscope_photo.
     */  
    @JsonProperty("LaryngoscopePhotoId")
    @TableField(value="LaryngoscopePhotoId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer LaryngoscopePhotoId;          
      
    /**
     * 软删除标记：0=未删除，1=已删除
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把音频自查记录实体转换成音频自查记录传输模型
     */
    public TAudioScreenRecordDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TAudioScreenRecordDto TAudioScreenRecordDto = new TAudioScreenRecordDto();
       
        BeanUtils.copyProperties(TAudioScreenRecordDto,this);
       
        return TAudioScreenRecordDto;
    }

}
