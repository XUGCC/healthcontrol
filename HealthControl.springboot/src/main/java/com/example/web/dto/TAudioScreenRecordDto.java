package com.example.web.dto;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.web.entity.*;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
/**
 * 音频自查记录类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TAudioScreenRecordDto extends BaseDto
{

    
     
    /**
     * 结果更新时间
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
     * 音频文件URL
     */ 
    @JsonProperty("AudioUrl")
    private String AudioUrl;
    
     
    /**
     * 音频格式：mp3wav等
     */ 
    @JsonProperty("AudioFormat")
    private String AudioFormat;
    
     
    /**
     * 音频时长
     */ 
    @JsonProperty("AudioDuration")
    private Integer AudioDuration;          
    
     
    /**
     * 音频采样率
     */ 
    @JsonProperty("AudioSamplingRate")
    private Integer AudioSamplingRate;          
    
     
    /**
     * 音频文件大小
     */ 
    @JsonProperty("AudioFileSize")
    private Integer AudioFileSize;          
    
     
    /**
     * 发音引导类型
     */ 
    @JsonProperty("PronunciationGuideType")
    private Boolean PronunciationGuideType;          
    
     
    /**
     * 波形图URL
     */
    @JsonProperty("WaveformUrl")
    private String WaveformUrl;
    
    /**
     * MFCC图谱URL
     */ 
    @JsonProperty("MfccSpectrumUrl")
    private String MfccSpectrumUrl;
    
     
    /**
     * MFCC图谱分辨率：如256*256
     */ 
    @JsonProperty("MfccSpectrumResolution")
    private String MfccSpectrumResolution;
    
     
    /**
     * mel图谱URL
     */ 
    @JsonProperty("MelSpectrumUrl")
    private String MelSpectrumUrl;
    
     
    /**
     * mel图谱分辨率：如256*256
     */ 
    @JsonProperty("MelSpectrumResolution")
    private String MelSpectrumResolution;
    
     
    /**
     * 孪生Densenet模型版本
     */ 
    @JsonProperty("DensenetModelVersion")
    private String DensenetModelVersion;
    
     
    /**
     * 初筛结果：0=良性，1=恶性
     */ 
    @JsonProperty("PrimaryScreenResult")
    private Boolean PrimaryScreenResult;          
    
     
    /**
     * 初筛置信度
     */ 
    @JsonProperty("PrimaryScreenConfidence")
    private Double PrimaryScreenConfidence;      

    /**
     * 语音性别：女性/男性（来源于 Siamese 多任务模型）
     */
    @JsonProperty("VoiceGender")
    private String VoiceGender;

    /**
     * 语音性别编码：0=女性，1=男性
     */
    @JsonProperty("VoiceGenderCode")
    private Integer VoiceGenderCode;

    /**
     * 语音性别置信度（0–1）
     */
    @JsonProperty("VoiceGenderConfidence")
    private Double VoiceGenderConfidence;
    
     
    /**
     * 基频jitter
     */ 
    @JsonProperty("Jitter")
    private Double Jitter;      
    
     
    /**
     * 基频shimmer
     */ 
    @JsonProperty("Shimmer")
    private Double Shimmer;      
    
     
    /**
     * 谐噪比HNR
     */ 
    @JsonProperty("Hnr")
    private Double Hnr;      
    
     
    /**
     * 最长发声时间MPT
     */ 
    @JsonProperty("Mpt")
    private Integer Mpt;          
    
     
    // 已移除 Deepseek 相关字段（改用 taudio_ai_report 存储 AI 解读报告）
    
     
    /**
     * 检测总状态
     */ 
    @JsonProperty("DetectTotalStatus")
    private Boolean DetectTotalStatus;          
    
     
    /**
     * 检测子状态
     */ 
    @JsonProperty("DetectSubStatus")
    private Boolean DetectSubStatus;          
    
     
    /**
     * 图谱生成失败原因
     */ 
    @JsonProperty("SpectrumFailReason")
    private String SpectrumFailReason;
    
     
    /**
     * 模型调用失败原因
     */ 
    @JsonProperty("ModelFailReason")
    private String ModelFailReason;
    
     
    /**
     * 整体失败原因
     */ 
    @JsonProperty("TotalFailReason")
    private String TotalFailReason;
    
     
    /**
     * 离线状态：0=在线，1=离线
     */ 
    @JsonProperty("OfflineStatus")
    private Boolean OfflineStatus;          
    
     
    /**
     * 关联喉镜照片表主键t_laryngoscope_photo.
     */ 
    @JsonProperty("LaryngoscopePhotoId")
    private Integer LaryngoscopePhotoId;          
    
     
    /**
     * 软删除标记：0=未删除，1=已删除
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("LaryngoscopePhotoDto") 
    private TLaryngoscopePhotoDto LaryngoscopePhotoDto;                        
   
 	 /**
     * 把音频自查记录传输模型转换成音频自查记录实体
     */
    public TAudioScreenRecord MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TAudioScreenRecord TAudioScreenRecord= new TAudioScreenRecord();
     
         BeanUtils.copyProperties(TAudioScreenRecord,this);
        
        return TAudioScreenRecord;
    }

}
