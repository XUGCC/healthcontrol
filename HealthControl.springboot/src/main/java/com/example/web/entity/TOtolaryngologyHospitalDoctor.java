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
 * 耳鼻喉医院医生表
 */
@Data
@TableName("`TOtolaryngologyHospitalDoctor`")
public class TOtolaryngologyHospitalDoctor extends BaseEntity {

      
  	  /**
     * 医院名称
     */  
    @JsonProperty("HospitalName")
    @TableField(value="HospitalName",updateStrategy = FieldStrategy.ALWAYS)
    private String HospitalName;
      
  	  /**
     * 医生姓名
     */  
    @JsonProperty("DoctorName")
    @TableField(value="DoctorName",updateStrategy = FieldStrategy.ALWAYS)
    private String DoctorName;
      
    /**
     * 科室类型：0=耳鼻喉科，1=咽喉科，2=头颈外科
     */  
    @JsonProperty("DepartmentType")
    @TableField(value="DepartmentType",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean DepartmentType;          
      
  	  /**
     * 所在地区：如北京市海淀区
     */  
    @JsonProperty("Region")
    @TableField(value="Region",updateStrategy = FieldStrategy.ALWAYS)
    private String Region;
      
  	  /**
     * 详细地址
     */  
    @JsonProperty("Address")
    @TableField(value="Address",updateStrategy = FieldStrategy.ALWAYS)
    private String Address;
      
  	  /**
     * 联系电话
     */  
    @JsonProperty("ContactPhone")
    @TableField(value="ContactPhone",updateStrategy = FieldStrategy.ALWAYS)
    private String ContactPhone;
      
  	  /**
     * 预约链接
     */  
    @JsonProperty("AppointmentUrl")
    @TableField(value="AppointmentUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String AppointmentUrl;
      
    /**
     * 展示状态：0=隐藏，1=展示
     */  
    @JsonProperty("ShowStatus")
    @TableField(value="ShowStatus",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean ShowStatus;          
      
    /**
     * 软删除标记：0=未删除，1=已删除
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
      
    /**
     * 创建人ID
     */  
    @JsonProperty("CreatorId")
    @TableField(value="CreatorId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer CreatorId;          

    /**
     * 医院等级/类型：三甲/专科等
     */
    @JsonProperty("HospitalLevel")
    @TableField(value = "HospitalLevel", updateStrategy = FieldStrategy.ALWAYS)
    private String HospitalLevel;

    /**
     * 标签：如嗓音门诊,肿瘤方向（逗号分隔）
     */
    @JsonProperty("Tags")
    @TableField(value = "Tags", updateStrategy = FieldStrategy.ALWAYS)
    private String Tags;

    /**
     * 擅长方向简介
     */
    @JsonProperty("SpecialtyDesc")
    @TableField(value = "SpecialtyDesc", updateStrategy = FieldStrategy.ALWAYS)
    private String SpecialtyDesc;

    /**
     * 经度
     */
    @JsonProperty("Longitude")
    @TableField(value = "Longitude", updateStrategy = FieldStrategy.ALWAYS)
    private Double Longitude;

    /**
     * 纬度
     */
    @JsonProperty("Latitude")
    @TableField(value = "Latitude", updateStrategy = FieldStrategy.ALWAYS)
    private Double Latitude;

    /**
     * 门诊时间说明
     */
    @JsonProperty("OutpatientTime")
    @TableField(value = "OutpatientTime", updateStrategy = FieldStrategy.ALWAYS)
    private String OutpatientTime;

    /**
     * 职称，如主任医师
     */
    @JsonProperty("DoctorTitle")
    @TableField(value = "DoctorTitle", updateStrategy = FieldStrategy.ALWAYS)
    private String DoctorTitle;

    /**
     * 头像/门诊配图
     */
    @JsonProperty("PicUrl")
    @TableField(value = "PicUrl", updateStrategy = FieldStrategy.ALWAYS)
    private String PicUrl;
  
    /**
     * 把耳鼻喉医院医生实体转换成耳鼻喉医院医生传输模型
     */
    public TOtolaryngologyHospitalDoctorDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TOtolaryngologyHospitalDoctorDto TOtolaryngologyHospitalDoctorDto = new TOtolaryngologyHospitalDoctorDto();
       
        BeanUtils.copyProperties(TOtolaryngologyHospitalDoctorDto,this);
       
        return TOtolaryngologyHospitalDoctorDto;
    }

}
