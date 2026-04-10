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
 * 科普点赞查询模型
 */
@NoArgsConstructor
@Data
public class TScienceLikePagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
     /**
     * 关联用户表主键
     */
  	 @JsonProperty("UserId")
    private Integer UserId;
     /**
     * 关联科普表主键
     */
  	 @JsonProperty("ScienceId")
    private Integer ScienceId;
    /**
     * 取消点赞时间时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;
    /**
     * 点赞操作时间时间范围
     */
    @JsonProperty("LikeTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> LikeTimeRange;
    /**
     * 取消点赞操作时间时间范围
     */
    @JsonProperty("CancelLikeTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> CancelLikeTimeRange;
     /**
     * 软删除标记
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
