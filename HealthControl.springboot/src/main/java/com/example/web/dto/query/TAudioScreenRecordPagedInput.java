package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 音频自查记录查询模型
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class TAudioScreenRecordPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;

    /**
     * 关联用户表主键 t_user（用于前端按用户过滤自己的检测记录）
     */
    @JsonProperty("UserId")
    private Integer UserId;
    /**
     * MFCC图谱分辨率：如256*256模糊查询条件
     */
  	 @JsonProperty("MfccSpectrumResolution")
    private String MfccSpectrumResolution;
    /**
     * mel图谱分辨率：如256*256模糊查询条件
     */
  	 @JsonProperty("MelSpectrumResolution")
    private String MelSpectrumResolution;
    /**
     * 孪生Densenet模型版本模糊查询条件
     */
  	 @JsonProperty("DensenetModelVersion")
    private String DensenetModelVersion;
    // 已移除 Deepseek 相关字段（改用 taudio_ai_report 存储 AI 解读报告）
    /**
     * 图谱生成失败原因模糊查询条件
     */
  	 @JsonProperty("SpectrumFailReason")
    private String SpectrumFailReason;
    /**
     * 模型调用失败原因模糊查询条件
     */
  	 @JsonProperty("ModelFailReason")
    private String ModelFailReason;
    /**
     * 整体失败原因模糊查询条件
     */
  	 @JsonProperty("TotalFailReason")
    private String TotalFailReason;
    /**
     * 结果更新时间时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;
     /**
     * 发音引导类型
     */
  	 @JsonProperty("PronunciationGuideType")
    private Boolean PronunciationGuideType;
     /**
     * 初筛结果：0=良性，1=恶性
     */
  	 @JsonProperty("PrimaryScreenResult")
    private Boolean PrimaryScreenResult;
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
     * 离线状态：0=在线，1=离线
     */
  	 @JsonProperty("OfflineStatus")
    private Boolean OfflineStatus;
     /**
     * 软删除标记：0=未删除，1=已删除
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
