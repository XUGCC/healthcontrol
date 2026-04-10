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
 * 用户就医推荐记录表
 */
@Data
@TableName("`TUserMedicalRecommend`")
public class TUserMedicalRecommend extends BaseEntity {

      
    /**
     * 查看状态更新时间
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
     * 关联音频自查记录表主键
     */  
    @JsonProperty("DetectId")
    @TableField(value="DetectId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer DetectId;          
      
    /**
     * 推荐依据风险等级
     */  
    @JsonProperty("RiskLevel")
    @TableField(value="RiskLevel",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean RiskLevel;          
      
    /**
     * 关联医院医生表主键
     */  
    @JsonProperty("RecommendHospitalId")
    @TableField(value="RecommendHospitalId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer RecommendHospitalId;          
      
    /**
     * 系统推荐时间
     */  
    @JsonProperty("RecommendTime")
    @TableField(value="RecommendTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime RecommendTime;             
      
    /**
     * 查看状态
     */  
    @JsonProperty("ViewStatus")
    @TableField(value="ViewStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean ViewStatus;          
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把用户就医推荐记录实体转换成用户就医推荐记录传输模型
     */
    public TUserMedicalRecommendDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TUserMedicalRecommendDto TUserMedicalRecommendDto = new TUserMedicalRecommendDto();
       
        BeanUtils.copyProperties(TUserMedicalRecommendDto,this);
       
        return TUserMedicalRecommendDto;
    }

}
