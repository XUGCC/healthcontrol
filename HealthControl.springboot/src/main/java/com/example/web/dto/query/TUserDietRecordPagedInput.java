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
 * 用户饮食记录查询模型
 */
@NoArgsConstructor
@Data
public class TUserDietRecordPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 食用感受模糊查询条件
     */
  	 @JsonProperty("EatFeeling")
    private String EatFeeling;
     /**
     * 关联用户表主键t_user.
     */
  	 @JsonProperty("UserId")
    private Integer UserId;
    /**
     * 饮食摄入时间时间范围
     */
    @JsonProperty("IntakeTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> IntakeTimeRange;
     /**
     * 软删除标记
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
