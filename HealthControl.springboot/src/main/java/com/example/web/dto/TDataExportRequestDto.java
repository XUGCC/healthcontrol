package com.example.web.dto;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import com.example.web.entity.*;
import lombok.EqualsAndHashCode;

/**
 * 数据导出申请类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TDataExportRequestDto extends BaseDto {

    /**
     * 关联用户表主键
     */
    @JsonProperty("UserId")
    private Integer UserId;

    /**
     * 申请类型：Export=导出
     */
    @JsonProperty("RequestType")
    private String RequestType;

    /**
     * 数据类型（JSON数组）
     */
    @JsonProperty("DataType")
    private String DataType;

    /**
     * 导出格式：JSON/Excel/PDF
     */
    @JsonProperty("ExportFormat")
    private String ExportFormat;

    /**
     * 申请说明
     */
    @JsonProperty("RequestDesc")
    private String RequestDesc;

    /**
     * 状态：Pending/Processing/Completed/Failed
     */
    @JsonProperty("Status")
    private String Status;

    /**
     * 导出文件URL
     */
    @JsonProperty("FileUrl")
    private String FileUrl;

    /**
     * 文件大小（字节）
     */
    @JsonProperty("FileSize")
    private Long FileSize;

    /**
     * 下载链接过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("ExpireTime")
    private LocalDateTime ExpireTime;

    /**
     * 处理完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("ProcessTime")
    private LocalDateTime ProcessTime;

    /**
     * 错误信息（失败时）
     */
    @JsonProperty("ErrorMessage")
    private String ErrorMessage;

    /**
     * 软删除标记
     */
    @JsonProperty("IsDelete")
    private Boolean IsDelete;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("UpdateTime")
    private LocalDateTime UpdateTime;

    /**
     * 关联用户信息
     */
    @JsonProperty("UserDto")
    private AppUserDto UserDto;

    /**
     * 把数据导出申请传输模型转换成数据导出申请实体
     */
    public TDataExportRequest MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TDataExportRequest entity = new TDataExportRequest();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}
