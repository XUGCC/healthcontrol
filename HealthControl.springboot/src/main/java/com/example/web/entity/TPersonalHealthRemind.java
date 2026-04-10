package com.example.web.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.poi.hpsf.Decimal;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.sql.Date;
import java.sql.Timestamp;
import lombok.Data;
import java.time.LocalDateTime;
import com.example.web.dto.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
/**
 * 个性化健康提醒表
 */
@Data
@TableName("`TPersonalHealthRemind`")
public class TPersonalHealthRemind extends BaseEntity {

      
    /**
     * 推送时间更新
     */  
    @JsonProperty("UpdateTime")
    @TableField(value="UpdateTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;             
      
    /**
     * 关联用户表主键t_user.
     */  
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;          
      
    /**
     * 提醒类型：0=定期自查，1=护喉饮食，2=发音训练，3=饮水，4=随访
     * 注意：数据库为 TINYINT(1)，这里使用 Integer 与前端枚举保持一致
     */  
    @JsonProperty("RemindType")
    @TableField(value="RemindType",updateStrategy = FieldStrategy.ALWAYS)
    private Integer RemindType;          
      
  	  /**
     * 提醒时间：如08:00
     */  
    @JsonProperty("RemindTime")
    @TableField(value="RemindTime",updateStrategy = FieldStrategy.ALWAYS)
    private String RemindTime;
      
    /**
     * 重复频率：0=每天，1=每周一至五，2=每周一次，3=每30天一次
     */  
    @JsonProperty("RepeatFrequency")
    @TableField(value="RepeatFrequency",updateStrategy = FieldStrategy.ALWAYS)
    private Integer RepeatFrequency;          
      
    /**
     * 提醒状态：0=关闭，1=开启
     */  
    @JsonProperty("RemindStatus")
    @TableField(value="RemindStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Integer RemindStatus;          
      
    /**
     * 最后推送时间
     */  
    @JsonProperty("LastPushTime")
    @TableField(value="LastPushTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime LastPushTime;             
      
  	  /**
     * 提醒内容
     */  
    @JsonProperty("RemindContent")
    @TableField(value="RemindContent",updateStrategy = FieldStrategy.ALWAYS)
    private String RemindContent;
      
    /**
     * 软删除标记：0=未删除，1=已删除
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Integer IsDelete;          
  
    /**
     * 把个性化健康提醒实体转换成个性化健康提醒传输模型
     */
    public TPersonalHealthRemindDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TPersonalHealthRemindDto TPersonalHealthRemindDto = new TPersonalHealthRemindDto();
       
        BeanUtils.copyProperties(TPersonalHealthRemindDto,this);
       
        return TPersonalHealthRemindDto;
    }

}
