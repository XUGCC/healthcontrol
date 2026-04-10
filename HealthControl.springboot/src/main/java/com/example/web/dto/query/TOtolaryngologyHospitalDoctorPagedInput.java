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
 * 耳鼻喉医院医生查询模型
 */
@NoArgsConstructor
@Data
public class TOtolaryngologyHospitalDoctorPagedInput extends PagedInput {
    
    /**
     * Id主键
     */
    @JsonProperty("Id")
    private Integer Id;
    /**
     * 医院名称模糊查询条件
     */
  	 @JsonProperty("HospitalName")
    private String HospitalName;
    /**
     * 医生姓名模糊查询条件
     */
  	 @JsonProperty("DoctorName")
    private String DoctorName;
    /**
     * 所在地区：如北京市海淀区模糊查询条件
     */
  	 @JsonProperty("Region")
    private String Region;
    /**
     * 详细地址模糊查询条件
     */
  	 @JsonProperty("Address")
    private String Address;
    /**
     * 联系电话模糊查询条件
     */
  	 @JsonProperty("ContactPhone")
    private String ContactPhone;
     /**
     * 创建人ID
     */
  	 @JsonProperty("CreatorId")
    private Integer CreatorId;
     /**
     * 科室类型：0=耳鼻喉科，1=咽喉科，2=头颈外科
     */
  	 @JsonProperty("DepartmentType")
    private Boolean DepartmentType;
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

}
