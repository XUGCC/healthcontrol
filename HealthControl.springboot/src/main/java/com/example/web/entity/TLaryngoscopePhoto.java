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
 * 喉镜照片记录表
 */
@Data
@TableName("`TLaryngoscopePhoto`")
public class TLaryngoscopePhoto extends BaseEntity {

      
    /**
     * 审核更新时间
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
     * 关联音频自查记录表主键t
     */  
    @JsonProperty("DetectId")
    @TableField(value="DetectId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer DetectId;          
      
  	  /**
     * 喉镜照片URL
     */  
    @JsonProperty("LaryngoscopePhotoUrl")
    @TableField(value="LaryngoscopePhotoUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String LaryngoscopePhotoUrl;
      
  	  /**
     * 照片描述
     */  
    @JsonProperty("PhotoDesc")
    @TableField(value="PhotoDesc",updateStrategy = FieldStrategy.ALWAYS)
    private String PhotoDesc;
      
    /**
     * 照片上传时间
     */  
    @JsonProperty("UploadTime")
    @TableField(value="UploadTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime UploadTime;             
      
    /**
     * 实际检查时间（区别于上传时间）
     */
    @JsonProperty("CheckTime")
    @TableField(value="CheckTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime CheckTime;

    /**
     * 检查医院名称
     */
    @JsonProperty("HospitalName")
    @TableField(value="HospitalName",updateStrategy = FieldStrategy.ALWAYS)
    private String HospitalName;

    /**
     * 检查方式，例如 电子喉镜/纤维喉镜
     */
    @JsonProperty("CheckType")
    @TableField(value="CheckType",updateStrategy = FieldStrategy.ALWAYS)
    private String CheckType;

    /**
     * 视角/体位，例如 正视/左侧位/右侧位
     */
    @JsonProperty("ViewPosition")
    @TableField(value="ViewPosition",updateStrategy = FieldStrategy.ALWAYS)
    private String ViewPosition;

    /**
     * 病变侧别：左侧/右侧/双侧/不明确
     */
    @JsonProperty("LesionSide")
    @TableField(value="LesionSide",updateStrategy = FieldStrategy.ALWAYS)
    private String LesionSide;
      
    /**
     * 审核状态
     */  
    @JsonProperty("AuditStatus")
    @TableField(value="AuditStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean AuditStatus;          
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把喉镜照片记录实体转换成喉镜照片记录传输模型
     */
    public TLaryngoscopePhotoDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TLaryngoscopePhotoDto TLaryngoscopePhotoDto = new TLaryngoscopePhotoDto();
       
        BeanUtils.copyProperties(TLaryngoscopePhotoDto,this);
       
        return TLaryngoscopePhotoDto;
    }

}
