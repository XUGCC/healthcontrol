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
 * 科普收藏查询模型
 */
@NoArgsConstructor
@Data
public class TScienceCollectPagedInput extends PagedInput {
    
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
     * 关联科普表主键
     */
  	 @JsonProperty("ScienceId")
    private Integer ScienceId;
    /**
     * 取消收藏时间时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;
    /**
     * 收藏操作时间时间范围
     */
    @JsonProperty("CollectTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> CollectTimeRange;
    /**
     * 取消收藏操作时间时间范围
     */
    @JsonProperty("CancelCollectTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> CancelCollectTimeRange;
     /**
     * 软删除标记
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
