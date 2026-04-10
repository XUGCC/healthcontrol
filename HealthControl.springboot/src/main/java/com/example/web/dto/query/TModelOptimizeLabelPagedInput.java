package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * 模型优化标注查询模型
 */
@NoArgsConstructor
@Data
public class TModelOptimizeLabelPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 医院确诊结果模糊查询条件
     */
  	 @JsonProperty("HospitalDiagnoseResult")
    private String HospitalDiagnoseResult;
    /**
     * 标注说明模糊查询条件
     */
  	 @JsonProperty("LabelDesc")
    private String LabelDesc;
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
     * 授权状态更新时间时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;
     /**
     * 标注类型
     */
  	 @JsonProperty("LabelType")
    private Boolean LabelType;
     /**
     * 授权状态
     */
  	 @JsonProperty("AuthStatus")
    private Boolean AuthStatus;
     /**
     * 软删除标记
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
