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
 * 自查报告导出记录查询模型
 */
@NoArgsConstructor
@Data
public class TScreenReportExportRecordPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
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
     * 报告导出时间时间范围
     */
    @JsonProperty("ExportTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> ExportTimeRange;
     /**
     * 导出格式
     */
  	 @JsonProperty("ExportFormat")
    private Boolean ExportFormat;
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

}
