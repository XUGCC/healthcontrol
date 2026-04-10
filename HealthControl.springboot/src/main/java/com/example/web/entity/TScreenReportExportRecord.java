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
 * 自查报告导出记录表
 */
@Data
@TableName("`TScreenReportExportRecord`")
public class TScreenReportExportRecord extends BaseEntity {

      
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
     * 报告导出时间
     */  
    @JsonProperty("ExportTime")
    @TableField(value="ExportTime",updateStrategy = FieldStrategy.ALWAYS)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    private LocalDateTime ExportTime;             
      
    /**
     * 导出格式
     */  
    @JsonProperty("ExportFormat")
    @TableField(value="ExportFormat",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean ExportFormat;          
      
  	  /**
     * 导出文件URL
     */  
    @JsonProperty("ExportFileUrl")
    @TableField(value="ExportFileUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String ExportFileUrl;
      
    /**
     * 导出用途
     */  
    @JsonProperty("ExportPurpose")
    @TableField(value="ExportPurpose",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean ExportPurpose;          
      
    /**
     * 软删除标记
     */  
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;          
  
    /**
     * 把自查报告导出记录实体转换成自查报告导出记录传输模型
     */
    public TScreenReportExportRecordDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TScreenReportExportRecordDto TScreenReportExportRecordDto = new TScreenReportExportRecordDto();
       
        BeanUtils.copyProperties(TScreenReportExportRecordDto,this);
       
        return TScreenReportExportRecordDto;
    }

}
