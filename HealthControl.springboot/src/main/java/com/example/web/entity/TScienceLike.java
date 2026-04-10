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
 * 科普点赞表
 */
@Data
@TableName("`TScienceLike`")
public class TScienceLike extends BaseEntity {

      
    /**
     * 取消点赞时间
     */  
    @JsonProperty("UpdateTime")
    @TableField(value="UpdateTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime UpdateTime;             
      
    /**
     * 关联用户表主键
     */  
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;          
      
    /**
     * 关联科普表主键
     */  
    @JsonProperty("ScienceId")
    @TableField(value="ScienceId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer ScienceId;          
      
    /**
     * 点赞操作时间
     */  
    @JsonProperty("LikeTime")
    @TableField(value="LikeTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime LikeTime;             
      
    /**
     * 取消点赞操作时间
     */  
    @JsonProperty("CancelLikeTime")
    @TableField(value="CancelLikeTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime CancelLikeTime;             
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把科普点赞实体转换成科普点赞传输模型
     */
    public TScienceLikeDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TScienceLikeDto TScienceLikeDto = new TScienceLikeDto();
       
        BeanUtils.copyProperties(TScienceLikeDto,this);
       
        return TScienceLikeDto;
    }

}
