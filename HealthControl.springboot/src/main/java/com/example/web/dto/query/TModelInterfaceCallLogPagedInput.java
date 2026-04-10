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
 * 模型接口调用日志查询模型
 */
@NoArgsConstructor
@Data
public class TModelInterfaceCallLogPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 调用参数模糊查询条件
     */
  	 @JsonProperty("InputParams")
    private String InputParams;
    /**
     * 返回结果摘要模糊查询条件
     */
  	 @JsonProperty("ResultSummary")
    private String ResultSummary;
    /**
     * 失败错误码模糊查询条件
     */
  	 @JsonProperty("FailErrorCode")
    private String FailErrorCode;
    /**
     * 失败详细信息模糊查询条件
     */
  	 @JsonProperty("FailDetailInfo")
    private String FailDetailInfo;
     /**
     * 关联音频自查记录表主键
     */
  	 @JsonProperty("DetectId")
    private Integer DetectId;
    /**
     * 结果更新时间时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;
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
     * 模型版本号模糊查询
     */
    @JsonProperty("ModelVersion")
    private String ModelVersion;
    /**
     * 内部服务名称/标识模糊查询
     */
    @JsonProperty("ServiceName")
    private String ServiceName;
     /**
     * 调用状态：0=失败，1=成功
     */
  	 @JsonProperty("CallStatus")
    private Boolean CallStatus;
     /**
     * 软删除标记
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
