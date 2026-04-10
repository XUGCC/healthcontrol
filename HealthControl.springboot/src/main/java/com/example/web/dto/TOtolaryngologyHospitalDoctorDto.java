package com.example.web.dto;
import com.example.web.enums.*;
import com.example.web.tools.dto.BaseDto;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.sql.Date;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.example.web.entity.*;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
/**
 * 耳鼻喉医院医生类
 */
@Data
public class TOtolaryngologyHospitalDoctorDto extends BaseDto
{

    
     
    /**
     * 医院名称
     */ 
    @JsonProperty("HospitalName")
    private String HospitalName;
    
     
    /**
     * 医生姓名
     */ 
    @JsonProperty("DoctorName")
    private String DoctorName;
    
     
    /**
     * 科室类型：0=耳鼻喉科，1=咽喉科，2=头颈外科
     */ 
    @JsonProperty("DepartmentType")
    private Boolean DepartmentType;          
    
     
    /**
     * 所在地区：如北京市海淀区
     */ 
    @JsonProperty("Region")
    private String Region;
    
     
    /**
     * 详细地址
     */ 
    @JsonProperty("Address")
    private String Address;
    
     
    /**
     * 联系电话
     */ 
    @JsonProperty("ContactPhone")
    private String ContactPhone;
    
     
    /**
     * 预约链接
     */ 
    @JsonProperty("AppointmentUrl")
    private String AppointmentUrl;
    
     
    /**
     * 展示状态：0=隐藏，1=展示
     */ 
    @JsonProperty("ShowStatus")
    private Boolean ShowStatus;          
    
     
    /**
     * 软删除标记：0=未删除，1=已删除
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          
    
     
    /**
     * 创建人ID
     */ 
    @JsonProperty("CreatorId")
    private Integer CreatorId;          

     @JsonProperty("CreatorDto") 
    private AppUserDto CreatorDto;                        
   
 	 /**
     * 把耳鼻喉医院医生传输模型转换成耳鼻喉医院医生实体
     */
    public TOtolaryngologyHospitalDoctor MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TOtolaryngologyHospitalDoctor TOtolaryngologyHospitalDoctor= new TOtolaryngologyHospitalDoctor();
     
         BeanUtils.copyProperties(TOtolaryngologyHospitalDoctor,this);
        
        return TOtolaryngologyHospitalDoctor;
    }

}
