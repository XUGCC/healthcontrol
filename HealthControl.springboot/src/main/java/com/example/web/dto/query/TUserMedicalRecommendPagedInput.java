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
 * 用户就医推荐记录查询模型
 */
@NoArgsConstructor
@Data
public class TUserMedicalRecommendPagedInput extends PagedInput {
    
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
     * 关联医院医生表主键
     */
  	 @JsonProperty("RecommendHospitalId")
    private Integer RecommendHospitalId;
    /**
     * 查看状态更新时间时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;
    /**
     * 系统推荐时间时间范围
     */
    @JsonProperty("RecommendTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> RecommendTimeRange;
     /**
     * 推荐依据风险等级
     */
  	 @JsonProperty("RiskLevel")
    private Boolean RiskLevel;
     /**
     * 查看状态
     */
  	 @JsonProperty("ViewStatus")
    private Boolean ViewStatus;
     /**
     * 软删除标记
     */
  	 @JsonProperty("IsDelete")
    private Boolean IsDelete;

}
