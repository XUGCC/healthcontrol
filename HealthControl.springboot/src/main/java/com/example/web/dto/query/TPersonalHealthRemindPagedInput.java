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
 * 个性化健康提醒查询模型
 */
@NoArgsConstructor
@Data
public class TPersonalHealthRemindPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 提醒内容模糊查询条件
     */
  	 @JsonProperty("RemindContent")
    private String RemindContent;
     /**
     * 关联用户表主键t_user.
     */
  	 @JsonProperty("UserId")
    private Integer UserId;
    /**
     * 推送时间更新时间范围
     */
    @JsonProperty("UpdateTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> UpdateTimeRange;
    /**
     * 提醒时间：如08:00时间范围
     */
    @JsonProperty("RemindTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> RemindTimeRange;
    /**
     * 最后推送时间时间范围
     */
    @JsonProperty("LastPushTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> LastPushTimeRange;
    /**
     * 提醒类型：0=定期自查，1=护喉饮食，2=发音训练，3=饮水，4=随访
     * 与表结构保持一致：tinyint(1) → Integer
     */
    @JsonProperty("RemindType")
    private Integer RemindType;
    /**
     * 重复频率：0=每天，1=每周一至五，2=每周一次，3=每30天一次
     */
    @JsonProperty("RepeatFrequency")
    private Integer RepeatFrequency;
    /**
     * 提醒状态：0=关闭，1=开启
     */
    @JsonProperty("RemindStatus")
    private Integer RemindStatus;
    /**
     * 软删除标记：0=未删除，1=已删除
     */
    @JsonProperty("IsDelete")
    private Integer IsDelete;

}
