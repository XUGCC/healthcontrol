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
 * 症状日志表
 */
@Data
@TableName("`TSymptomLog`")
public class TSymptomLog extends BaseEntity {

      
    /**
     * 关联用户表主键t_user.
     */  
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;          
      
    /**
     * 关联音频自查记录表主键
     */  
    @JsonProperty("DetectId")
    @TableField(
        value = "DetectId",
        updateStrategy = FieldStrategy.ALWAYS,
        insertStrategy = FieldStrategy.IGNORED  // 允许插入时为空，不写入该列
    )
    private Integer DetectId;          
      
    /**
     * 症状类型：如嘶哑、疼痛、吞咽困难等
     */  
    @JsonProperty("SymptomType")
    @TableField(value="SymptomType",updateStrategy = FieldStrategy.ALWAYS)
    private String SymptomType;          
      
    /**
     * 症状轻重：1=轻度，2=中度，3=重度
     */  
    @JsonProperty("SymptomLevel")
    @TableField(value="SymptomLevel",updateStrategy = FieldStrategy.ALWAYS)
    private Integer SymptomLevel;          
      
  	  /**
     * 症状持续时长
     */  
    @JsonProperty("SymptomDuration")
    @TableField(value="SymptomDuration",updateStrategy = FieldStrategy.ALWAYS)
    private String SymptomDuration;
      
    /**
     * 症状首次出现时间
     */  
    @JsonProperty("SymptomOccurTime")
    @TableField(value="SymptomOccurTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime SymptomOccurTime;             
      
  	  /**
     * 症状备注
     */  
    @JsonProperty("Remark")
    @TableField(value="Remark",updateStrategy = FieldStrategy.ALWAYS)
    private String Remark;
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把症状日志实体转换成症状日志传输模型
     */
    public TSymptomLogDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TSymptomLogDto TSymptomLogDto = new TSymptomLogDto();
       
        BeanUtils.copyProperties(TSymptomLogDto,this);
       
        return TSymptomLogDto;
    }

}
