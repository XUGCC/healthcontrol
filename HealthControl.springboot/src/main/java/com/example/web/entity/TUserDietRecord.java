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
 * 用户饮食记录表
 */
@Data
@TableName("`TUserDietRecord`")
public class TUserDietRecord extends BaseEntity {

      
    /**
     * 关联用户表主键t_user.
     */  
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;          
      
    /**
     * 关联食物库表主键
     */  
    @JsonProperty("FoodId")
    @TableField(value="FoodId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer FoodId;          
      
    /**
     * 饮食摄入时间
     */  
    @JsonProperty("IntakeTime")
    @TableField(value="IntakeTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime IntakeTime;             
      
    /**
     * 当日摄入频次
     */  
    @JsonProperty("IntakeFrequency")
    @TableField(value="IntakeFrequency",updateStrategy = FieldStrategy.ALWAYS)
    private Integer IntakeFrequency;          
      
  	  /**
     * 食用感受
     */  
    @JsonProperty("EatFeeling")
    @TableField(value="EatFeeling",updateStrategy = FieldStrategy.ALWAYS)
    private String EatFeeling;
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把用户饮食记录实体转换成用户饮食记录传输模型
     */
    public TUserDietRecordDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TUserDietRecordDto TUserDietRecordDto = new TUserDietRecordDto();
       
        BeanUtils.copyProperties(TUserDietRecordDto,this);
       
        return TUserDietRecordDto;
    }

}
